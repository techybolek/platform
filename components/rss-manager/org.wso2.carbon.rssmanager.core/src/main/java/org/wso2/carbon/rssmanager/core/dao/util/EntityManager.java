/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.rssmanager.core.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.core.RSSTransactionManager;
import org.wso2.carbon.rssmanager.core.dao.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EntityManager {

    private static final Log log = LogFactory.getLog(EntityManager.class);
    private RSSTransactionManager txManager;
    private static final int DEFAULT_TRANSACTION_ISOLATION_LEVEL = -1;

    public EntityManager(final RSSTransactionManager txManager) {
        this.txManager = txManager;
    }

    /**
     * Thread local variable to track the status of active nested transactions
     */
    private static ThreadLocal<Integer> activeNestedTransactions = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };

    /**
     * This is used to keep the enlisted XADatasource objects
     */
    private static ThreadLocal<Set<XAResource>> enlistedXADataSources = new ThreadLocal<Set<XAResource>>() {
        protected Set<XAResource> initialValue() {
            return new HashSet<XAResource>();
        }
    };

    public RSSTransactionManager getRSSTransactionManager() {
        return txManager;
    }

    public boolean isInTransaction() {
        return activeNestedTransactions.get() > 0;
    }

    public synchronized boolean beginTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("beginTransaction()");
        }

        if (this.isInTransaction()) {
            return false;
        }

        if (activeNestedTransactions.get() == 0) {
            this.getRSSTransactionManager().begin();
        }
        activeNestedTransactions.set(activeNestedTransactions.get() + 1);
        return true;
    }

    public synchronized void endTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("endTransaction()");
        }
        activeNestedTransactions.set(activeNestedTransactions.get() - 1);
        /* commit all only if we are at the outer most transaction */
        if (activeNestedTransactions.get() == 0) {
            this.delistResource(XAResource.TMSUCCESS);
            this.getRSSTransactionManager().commit();
        } else if (activeNestedTransactions.get() < 0) {
            activeNestedTransactions.set(0);
        }
    }

    public synchronized void rollbackTransaction() throws RSSManagerException {
        if (log.isDebugEnabled()) {
            log.debug("rollbackTransaction()");
        }
        if (log.isDebugEnabled()) {
            log.debug("this.getRSSTxManager().rollback()");
        }
        this.delistResource(XAResource.TMFAIL);
        this.getRSSTransactionManager().rollback();
        activeNestedTransactions.set(0);
    }

    public Connection createConnection(DataSource dataSource) throws RSSDAOException {
        return this.createConnection(dataSource, DEFAULT_TRANSACTION_ISOLATION_LEVEL);
    }

    public synchronized Connection createConnection(DataSource dataSource,
                                                    int txIsolationLevel) throws RSSDAOException {
        Connection conn;
        try {
            conn = dataSource.getConnection();
            if (conn instanceof XAConnection && isInTransaction()) {
                Transaction tx =
                        this.getRSSTransactionManager().getTransactionManager().getTransaction();
                XAResource xaRes = ((XAConnection) conn).getXAResource();
                if (!isXAResourceEnlisted(xaRes)) {
                    if (txIsolationLevel >= 0) {
                        conn.setTransactionIsolation(txIsolationLevel);
                    }
                    tx.enlistResource(xaRes);
                    addToEnlistedXADataSources(xaRes);

                }
            }
            return conn;
        } catch (Exception e) {
            throw new RSSDAOException("Error occurred while creating datasource connection: " +
                    e.getMessage(), e);
        }
    }

    public synchronized void delistResource(int flag) throws RSSManagerException {
        Set<XAResource> enlistedResources = enlistedXADataSources.get();
        try {
            if (enlistedResources != null && !enlistedResources.isEmpty()) {
                Transaction tx =
                        this.getRSSTransactionManager().getTransactionManager().getTransaction();
                Iterator<XAResource> itr = enlistedResources.iterator();
                while (itr.hasNext()) {
                    XAResource resource = itr.next();
                    if (tx != null) {
                        tx.delistResource(resource, flag);
                    }
                    itr.remove();
                }
            }
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while delisting datasource " +
                    "connection: " + e.getMessage(), e);
        }
    }

    /**
     * This method adds XAResource object to enlistedXADataSources Threadlocal set
     *
     * @param resource XA resource associated with the connection
     */
    private synchronized void addToEnlistedXADataSources(XAResource resource) {
        enlistedXADataSources.get().add(resource);
    }

    private synchronized boolean isXAResourceEnlisted(XAResource resource) {
        return enlistedXADataSources.get().contains(resource);
    }

    public boolean hasNoActiveTransaction() {
        return txManager.hasNoActiveTransaction();
    }
    
}
