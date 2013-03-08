/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cassandra.explorer.utils;

import me.prettyprint.cassandra.serializers.AsciiSerializer;
import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.TimeUUIDSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.ddl.ComparatorType;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CassandraUtils {

    private static final Map<String, Serializer> serializerMap =
            new HashMap<String, Serializer>();

    private static final Map<String, CFInfo> columnFamilyCache = new HashMap<String, CFInfo>();
    // TODO Make this a bounded LRU cache to handle large cf number scenarios

    static {

        serializerMap.put(ComparatorType.UTF8TYPE.getClassName(), new StringSerializer());
        serializerMap.put(ComparatorType.ASCIITYPE.getClassName(), new AsciiSerializer());
        serializerMap.put(ComparatorType.LONGTYPE.getClassName(), new LongSerializer());
        serializerMap.put(ComparatorType.BYTESTYPE.getClassName(), new ByteBufferSerializer());
        serializerMap.put(ComparatorType.INTEGERTYPE.getClassName(), new IntegerSerializer());
        serializerMap.put(ComparatorType.UUIDTYPE.getClassName(), new UUIDSerializer());
        serializerMap.put(ComparatorType.TIMEUUIDTYPE.getClassName(), new TimeUUIDSerializer());
    }

    public static Serializer getSerializer(String comparatorClass) {
        return serializerMap.get(comparatorClass);
    }

    public static CFInfo getColumnFamilyInfo(Cluster cluster, Keyspace keyspace,
                                             String columnFamilyName) {
        CFInfo cfInfo = columnFamilyCache.get(keyspace + "-" + columnFamilyName);

        if (cfInfo == null) {
            cfInfo = new CFInfo(cluster, keyspace, columnFamilyName);
            columnFamilyCache.put(keyspace + "-" + columnFamilyName, cfInfo);
        }

        return cfInfo;
    }

    public static String getStringDeserialization(Serializer serializer, ByteBuffer data) {
        if(serializer instanceof ByteBufferSerializer){
            serializer = new StringSerializer();
        }
        Object columnName = serializer.fromByteBuffer(data);
        return columnName.toString();
    }

}
