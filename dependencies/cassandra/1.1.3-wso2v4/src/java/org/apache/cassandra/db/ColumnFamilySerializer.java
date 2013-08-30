package org.apache.cassandra.db;
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


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.apache.cassandra.config.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cassandra.io.IColumnSerializer;
import org.apache.cassandra.io.ISerializer;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.utils.UUIDGen;

public class ColumnFamilySerializer implements ISerializer<ColumnFamily>
{
    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilySerializer.class);

    /*
     * Serialized ColumnFamily format:
     *
     * [serialized for intra-node writes only, e.g. returning a query result]
     * <cf nullability boolean: false if the cf is null>
     * <cf id>
     *
     * [in sstable only]
     * <column bloom filter>
     * <sparse column index, start/finish columns every ColumnIndexSizeInKB of data>
     *
     * [always present]
     * <local deletion time>
     * <client-provided deletion time>
     * <column count>
     * <columns, serialized individually>
    */
    public void serialize(ColumnFamily columnFamily, DataOutput dos)
    {
        serialize(columnFamily, dos, MessagingService.version_);
    }

    /**public void serialize(ColumnFamily columnFamily, DataOutput dos, int version)
    {
        serialize(columnFamily, dos, MessagingService.version_);
    }*/

    public void serialize(ColumnFamily columnFamily, DataOutput dos, int version)
    {
        try
        {
            if (columnFamily == null)
            {
                dos.writeBoolean(false);
                return;
            }

            dos.writeBoolean(true);

            serializeCfId(columnFamily.id(), dos, version);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        serializeForSSTable(columnFamily, dos);
    }

    public int serializeForSSTable(ColumnFamily columnFamily, DataOutput dos)
    {
        try
        {
            serializeCFInfo(columnFamily, dos);

            Collection<IColumn> columns = columnFamily.getSortedColumns();
            int count = columns.size();
            dos.writeInt(count);
            int i = 0;
            for (IColumn column : columns)
            {
                columnFamily.getColumnSerializer().serialize(column, dos);
                i++;
            }
            assert count == i: "CF size changed during serialization: was " + count + " initially but " + i + " written";
            return count;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void serializeCFInfo(ColumnFamily columnFamily, DataOutput dos) throws IOException
    {
        dos.writeInt(columnFamily.getLocalDeletionTime());
        dos.writeLong(columnFamily.getMarkedForDeleteAt());
    }

    public int serializeWithIndexes(ColumnFamily columnFamily, ColumnIndexer.RowHeader index, DataOutput dos)
    {
        ColumnIndexer.serialize(index, dos);
        return serializeForSSTable(columnFamily, dos);
    }

    public ColumnFamily deserialize(DataInput dis) throws IOException
    {
        return deserialize(dis, MessagingService.version_, IColumnSerializer.Flag.LOCAL, TreeMapBackedSortedColumns.factory());
    }

    public ColumnFamily deserialize(DataInput dis, int version, IColumnSerializer.Flag flag, ISortedColumns.Factory factory) throws IOException
    {
        if (!dis.readBoolean())
            return null;

        ColumnFamily cf = ColumnFamily.create(deserializeCfId(dis, version), factory);
        deserializeFromSSTableNoColumns(cf, dis);
        deserializeColumns(dis, cf, flag);
        return cf;
    }

    public void deserializeColumns(DataInput dis, ColumnFamily cf, IColumnSerializer.Flag flag) throws IOException
    {
        int size = dis.readInt();
        deserializeColumns(dis, cf, size, flag);
    }

    /* column count is already read from DataInput */
    public void deserializeColumns(DataInput dis, ColumnFamily cf, int size, IColumnSerializer.Flag flag) throws IOException
    {
        for (int i = 0; i < size; ++i)
        {
            IColumn column = cf.getColumnSerializer().deserialize(dis, flag, (int) (System.currentTimeMillis() / 1000));
            cf.addColumn(column);
        }
    }

    public ColumnFamily deserializeFromSSTableNoColumns(ColumnFamily cf, DataInput input) throws IOException
    {
        cf.delete(input.readInt(), input.readLong());
        return cf;
    }

    public void serializeCfId(UUID cfId, DataOutput dos, int version) throws IOException
    {
        if (version < MessagingService.VERSION_111) // try to use CF's old id where possible (CASSANDRA-3794)
        {
            Integer oldId = Schema.instance.convertNewCfId(cfId);

            if (oldId == null)
                throw new IOException("Can't serialize ColumnFamily ID " + cfId + " to be used by version " + version +
                                      ", because int <-> uuid mapping could not be established (CF was created in mixed version cluster).");

            dos.writeInt(oldId);
        }
        else
            UUIDGen.write(cfId, dos);
    }

    public UUID deserializeCfId(DataInput dis, int version) throws IOException
    {
        // create a ColumnFamily based on the cf id
        UUID cfId = (version < MessagingService.VERSION_111)
                     ? Schema.instance.convertOldCfId(dis.readInt())
                     : UUIDGen.read(dis);

        if (Schema.instance.getCF(cfId) == null)
            throw new UnknownColumnFamilyException("Couldn't find cfId=" + cfId, cfId);

        return cfId;
    }

    public int cfIdSerializedSize(int version)
    {
        return version < MessagingService.VERSION_111 ? DBConstants.intSize : DBConstants.uuidSize;
    }

    public long serializedSize(ColumnFamily cf)
    {
        return serializedSize(cf, MessagingService.version_);
    }

    public long serializedSize(ColumnFamily cf, int version)
    {
        return cf == null ? DBConstants.boolSize : cf.serializedSize(version);
    }
}
