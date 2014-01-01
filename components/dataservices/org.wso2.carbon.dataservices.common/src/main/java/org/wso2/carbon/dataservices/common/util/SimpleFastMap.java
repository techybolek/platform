/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a Java java.util.Map implementation, with fast put/remove operations.
 */
public class SimpleFastMap<K, V> implements Map<K, V> {
    
    private MapEntry<K, V>[] entries;
    
    @SuppressWarnings("unchecked")
    public SimpleFastMap(int c) {
        this.entries = new MapEntry[c];
        for (int i = 0; i < this.entries.length; i++) {
            this.entries[i] = new MapEntry<K, V>();
        }
    }
    
    @Override
    public int size() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean containsKey(Object key) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public V get(Object key) {
        int index = this.hashIndex(key);
        MapEntry<K, V> entry = this.entries[index];
        return entry != null && entry.valid && entry.key.equals(key) ? entry.value : null;
    }

    @Override
    public V put(K key, V value) {
        int index = this.hashIndex(key);
        this.entries[index].key = key;
        this.entries[index].value = value;
        this.entries[index].valid = true;
        return null;
    }

    @Override
    public V remove(Object key) {
        int index = this.hashIndex(key);
        MapEntry<K, V> entry = this.entries[index];
        if (entry == null) {
            return null;
        }
        //entry.valid = false;
        return entry.value;
    }
    
    private int hashIndex(Object key) {
        return Math.abs(key.hashCode()) % entries.length;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {   
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Set<K> keySet() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Collection<V> values() {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new IllegalStateException("Not implemented");
    }
    
    @SuppressWarnings("hiding")
    private class MapEntry<K, V> {
        public K key;
        public V value;
        public boolean valid;
        public MapEntry() {}
    }

}
