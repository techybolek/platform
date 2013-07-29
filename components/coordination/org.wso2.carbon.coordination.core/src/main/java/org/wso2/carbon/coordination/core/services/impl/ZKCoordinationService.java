/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.coordination.core.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.wso2.carbon.coordination.common.CoordinationException;
import org.wso2.carbon.coordination.common.CoordinationException.ExceptionCode;
import org.wso2.carbon.coordination.core.CoordinationConfiguration;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.coordination.core.sync.*;
import org.wso2.carbon.coordination.core.sync.Queue;
import org.wso2.carbon.coordination.core.sync.impl.*;
import org.wso2.carbon.coordination.core.utils.CoordinationUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Coordination service implementation class.
 */
public class ZKCoordinationService implements CoordinationService, Watcher {

	public static final int MAX_ZK_MESSAGE_SIZE = 1024 * 800;
	
	public static final int MAX_SCHEDULER_THREADS = 10;
	
	public static final int ZNODE_CLEANUP_DELAY = 1000 * 60 * 2;
	
	public static final int ZNODE_CLEANUP_TASK_INTERVAL = 1000 * 30;

    public static final int SESSION_TIME_OUT_WAITING_DELAY = 2000;
	
	private static final Log log = LogFactory.getLog(ZKCoordinationService.class);
	
	private ZooKeeper zooKeeper;
	
	private boolean enabled;
	
	private boolean closed;

    private boolean expired;

    private boolean cleanUpDone;
	
	private static ScheduledExecutorService scheduler;
	
	private static List<ZNodeDeletionEntry> znodeTimerDeletionList;
	
	private static List<String> znodeOnCloseDeletionList;

    private Set<ZKSyncPrimitive> syncPrimitives;

    private CoordinationConfiguration coordinationConf;

   	public ZKCoordinationService(CoordinationConfiguration conf) throws CoordinationException {
		this.closed = false;
		this.enabled = conf.isEnabled();
        this.coordinationConf = conf;
        this.syncPrimitives = new HashSet<ZKSyncPrimitive>();
		if (this.isEnabled()) {
		    try {
			    this.zooKeeper = new ZooKeeper(conf.getConnectionString(),
			    		conf.getSessionTimeout(), this);
			    if (znodeOnCloseDeletionList == null) {
			    	znodeOnCloseDeletionList = new Vector<String>();
			    }
			    if (scheduler == null || scheduler.isShutdown()) {
			    	znodeTimerDeletionList = new Vector<ZKCoordinationService.ZNodeDeletionEntry>();
			        scheduler = Executors.newScheduledThreadPool(MAX_SCHEDULER_THREADS);
			        scheduler.scheduleWithFixedDelay(new ZNodeDeletionTask(), 
			        		ZNODE_CLEANUP_TASK_INTERVAL, ZNODE_CLEANUP_TASK_INTERVAL,
			        		TimeUnit.MILLISECONDS);
			    }
			    log.debug("Coordination service connection established with ZooKeeper.");
		    } catch (IOException e) {
			    new CoordinationException(ExceptionCode.IO_ERROR, e);
		    }
		} else {
			log.debug("Coordination service disabled.");
		}
	}
	
	public ZKCoordinationService(String configurationFilePath) throws CoordinationException {
		this(CoordinationUtils.loadCoordinationClientConfig(configurationFilePath));
	}
	
	public static List<ZNodeDeletionEntry> getZNodeTimerDeletionList() {
		return znodeTimerDeletionList;
	}
	
	public static List<String> getZNodeOnCloseDeletionList() {
		return znodeOnCloseDeletionList;
	}

    public Set<ZKSyncPrimitive> getSyncPrimitives() {
        return syncPrimitives;
    }

    public CoordinationConfiguration getCoordinationConf() {
        return coordinationConf;
    }

    @Override
	public boolean isEnabled() {
		return enabled;
	}

	private void checkService() throws CoordinationException {
		if (!this.isEnabled()) {
			throw new CoordinationException(ExceptionCode.COORDINATION_SERVICE_NOT_ENABLED);
		}
	}
	
	@Override
	public Barrier createBarrier(String id, int count, int waitTimeout) throws CoordinationException {
		this.checkService();
        ZKBarrier barrier = new ZKBarrier(this.getZooKeeper(), id, count, waitTimeout);
        this.getSyncPrimitives().add(barrier);
		return barrier;
	}

	@Override
	public Group createGroup(String id) throws CoordinationException {
		this.checkService();
		if (ZKGroup.GROUP_COMM_NODE_ID.equals(id)) {
			throw new CoordinationException("'" + ZKGroup.GROUP_COMM_NODE_ID + 
					"' cannot be used a group id, since it is reserved.", ExceptionCode.GENERIC_ERROR);
		}
        ZKGroup group = new ZKGroup(this.getZooKeeper(), id);
        this.getSyncPrimitives().add(group);
		return group;
	}

	@Override
	public Queue createQueue(String id, int waitTimeout) throws CoordinationException {
		this.checkService();
        ZKQueue queue = new ZKQueue(this.getZooKeeper(), id, waitTimeout);
        this.getSyncPrimitives().add(queue);
		return queue;
	}
	
	public ZooKeeper getZooKeeper() {
		return zooKeeper;
	}

	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public void close() throws CoordinationException {
		if (this.isClosed()) {
			return;
		}
		try {
			if (this.isEnabled()) {
				this.cleanupOnCloseZNodes();
			    this.getZooKeeper().close();
			}
			this.closed = true;
		} catch (InterruptedException e) {
			throw new CoordinationException(ExceptionCode.GENERIC_ERROR, e);
		}
	}
	
	private void cleanupOnCloseZNodes() {
		for (String path : getZNodeOnCloseDeletionList()) {
			this.deleteZNode(path);
		}
	}

	@Override
	public Lock createLock(String id, int waitTimeout) throws CoordinationException {
		this.checkService();
        ZKLock lock = new ZKLock(this.getZooKeeper(), id, waitTimeout);
        this.getSyncPrimitives().add(lock);
		return lock;
	}
	
	public static void scheduleOnCloseZNodeDeletion(String path) {
		getZNodeOnCloseDeletionList().add(path);
	}
	
	public static void scheduleTimedZNodeDeletion(String path) {
		getZNodeTimerDeletionList().add(new ZNodeDeletionEntry(path, System.currentTimeMillis()));
	}
	
	private void deleteZNode(String path) {
		try {
			getZooKeeper().delete(path, -1);
		} catch (Exception ignore) {
			// ignore
		}
	}

    public boolean isCleanUpDone() {
        return cleanUpDone;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public void setCleanUpDone(boolean cleanUpDone) {
        this.cleanUpDone = cleanUpDone;
    }

    private static class ZNodeDeletionEntry {
		
		private long createdTime;
		
		private String path;
		
		public ZNodeDeletionEntry(String path, long createdTime) {
			this.path = path;
			this.createdTime = createdTime;
		}

		public long getCreatedTime() {
			return createdTime;
		}

		public String getPath() {
			return path;
		}
		
	}
	
	private class ZNodeDeletionTask implements Runnable {
		
		public void run() {
			for (ZNodeDeletionEntry entry : getZNodeTimerDeletionList()) {
				if (this.readyToDelete(entry)) {
					deleteZNode(entry.getPath());
				}
			}
		}
		
		private boolean readyToDelete(ZNodeDeletionEntry entry) {
			return (System.currentTimeMillis() - entry.getCreatedTime()) > ZNODE_CLEANUP_DELAY;
		}
		
	}

	@Override
	public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.None) {
            Event.KeeperState state = event.getState();
            switch (state) {
                case SyncConnected:
                    this.setCleanUpDone(false);
                    if (!isExpired()) {
                        Iterator<ZKSyncPrimitive> primitiveIterator = this.getSyncPrimitives().iterator();
                        while (primitiveIterator.hasNext()) {
                            String primitiveId = primitiveIterator.next().getId();
                            primitiveIterator.next().onConnect(primitiveId);
                        }
                    }
                    this.setExpired(false);
                    break;
                case Disconnected:
                    Timer sessionExpireHandlerTimer = new Timer();
                    /* Task to check whether session is expired */
                    sessionExpireHandlerTimer.schedule(new SessionExpireHandler(), this.getCoordinationConf().
                            getSessionTimeout() + SESSION_TIME_OUT_WAITING_DELAY);
                    break;
                case Expired:
                    this.setExpired(true);
                    this.cleanUp();
                    try {
                        this.getZooKeeper().close();
                        this.zooKeeper = new ZooKeeper(this.getCoordinationConf().getConnectionString(),
                                this.getCoordinationConf().getSessionTimeout(), this);
                        for (ZKSyncPrimitive primitive : this.getSyncPrimitives()) {
                            primitive.setZooKeeper(this.zooKeeper);
                            String primitiveId = primitive.getId();
                            primitive.onConnect(primitiveId);
                        }
                    } catch (Exception e) {
                        new CoordinationException(ExceptionCode.IO_ERROR, e);
                    }
                    break;
            }
        }
        if (log.isDebugEnabled()) {
			log.debug("At ZKCoordinationService#process: " + event.toString());
		}
	}

	@Override
	public IntegerCounter createIntegerCounter(String id)
			throws CoordinationException {
		this.checkService();
		return new ZKIntegerCounter(this.getZooKeeper(), id);
	}

    private synchronized void cleanUp() {
        if(!isCleanUpDone())  {
            for (ZKSyncPrimitive primitive : getSyncPrimitives()) {
                primitive.onExpired();
            }
        }
        this.setCleanUpDone(true);
    }

    private class SessionExpireHandler extends TimerTask {

        @Override
        public void run() {
            cleanUp();
        }
    }

}
