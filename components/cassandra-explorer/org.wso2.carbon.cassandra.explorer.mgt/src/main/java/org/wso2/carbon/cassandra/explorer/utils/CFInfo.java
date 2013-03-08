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

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import org.wso2.carbon.cassandra.explorer.connection.ConnectionManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CFInfo {

    private String keyspace;

    private String columnFamilyName;

    private Serializer keySerializer = new ByteBufferSerializer();

    private Serializer columnSerializer = new ByteBufferSerializer();

    private Map<ByteBuffer, Serializer> valueSerializerMap = new HashMap<ByteBuffer, Serializer>();

    public CFInfo(Cluster cluster, Keyspace keyspace, String name) {
        this.setKeyspace(keyspace.getKeyspaceName());
        this.setColumnFamilyName(name);

        ColumnFamilyDefinition cfDef = ConnectionManager.getColumnFamilyDefinition(
                cluster, keyspace, getColumnFamilyName());

        this.keySerializer = CassandraUtils.getSerializer(cfDef.getKeyValidationClass());
        ComparatorType comparatorType;
        List<ColumnDefinition> columnMetaData = new ArrayList<ColumnDefinition>();
        if (cfDef != null) {
            comparatorType = cfDef.getComparatorType();
            columnMetaData = cfDef.getColumnMetadata();

            this.columnSerializer = ConnectionManager.getSerializer(comparatorType.getClassName());
        }

        for (ColumnDefinition columnDefinition : columnMetaData) {
            valueSerializerMap.put(columnDefinition.getName(),
                                   CassandraUtils.getSerializer(
                                           columnDefinition.getValidationClass()));
        }

    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getColumnFamilyName() {
        return columnFamilyName;
    }

    public void setColumnFamilyName(String columnFamilyName) {
        this.columnFamilyName = columnFamilyName;
    }

    public Serializer getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(Serializer keySerializer) {
        this.keySerializer = keySerializer;
    }

    public Serializer getColumnSerializer() {
        return columnSerializer;
    }

    public void setColumnSerializer(Serializer columnSerializer) {
        this.columnSerializer = columnSerializer;
    }

    public Serializer getColumnValueSerializer(ByteBuffer columnName) {
        Serializer serializer = valueSerializerMap.get(columnName);

        if (serializer == null) {
            serializer = new StringSerializer(); // Defaults to UTF8 based String serializer
        }

        return serializer;
    }

}
