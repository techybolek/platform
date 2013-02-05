/*
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.apache.qpid.server.subscription;

import org.apache.qpid.server.queue.AMQQueue;
import org.apache.qpid.server.subscription.Subscription;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.ByteBuffer;

public class SubscriptionList
{
    private ConcurrentLinkedQueue<Subscription> backend = new ConcurrentLinkedQueue<Subscription>();
	
    public SubscriptionList(AMQQueue queue)
    {
    }
	
    public synchronized void add(Subscription sub)
    {
    	backend.add(sub);
    }
	
    public synchronized boolean remove(Subscription sub)
    {
        return backend.remove(sub);
    }
	
    public Iterator<Subscription> iterator()
    {
        return backend.iterator();
    }
	
    public synchronized Subscription getHead()
    {
        return !backend.isEmpty()?backend.element(): null;
    }
	
    public int size()
    {
        return backend.size();
    }	
}
