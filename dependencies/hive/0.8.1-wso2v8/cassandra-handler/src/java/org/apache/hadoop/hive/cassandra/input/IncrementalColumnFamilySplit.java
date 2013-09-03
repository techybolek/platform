package org.apache.hadoop.hive.cassandra.input;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class IncrementalColumnFamilySplit extends InputSplit implements Writable, org.apache.hadoop.mapred.InputSplit {
    private long [] keys;
    private long startColName;
    private long endColName;

    private String[] dataNodes;
    private int iteration =0;


    public IncrementalColumnFamilySplit(long [] keys, long startTimeStamp, long endTimeStamp, String[] dataNodes) {
       this.keys = keys;
        this.startColName = startTimeStamp;
        this.endColName = endTimeStamp;
        this.dataNodes = dataNodes;
    }


    @Override
    public long getLength() throws IOException {
        return Long.MAX_VALUE;
    }

    @Override
    public String[] getLocations() throws IOException {
        return dataNodes;
    }

    protected IncrementalColumnFamilySplit() {

    }

    public void write(DataOutput out) throws IOException {
        out.writeLong(startColName);
        out.writeLong(endColName);

        out.writeInt(keys.length);
        for (long key: keys){
            out.writeLong(key);
        }

        out.writeInt(dataNodes.length);
        for (String endpoint : dataNodes) {
            out.writeUTF(endpoint);
        }
    }

    public void readFields(DataInput in) throws IOException {
        startColName = in.readLong();
        endColName = in.readLong();

        int numOfKeys = in.readInt();
        keys = new long[numOfKeys];
        for (int i=0; i<numOfKeys;  i++){
            keys[i] = in.readLong();
        }

        int numOfEndpoints = in.readInt();
        dataNodes = new String[numOfEndpoints];
        for (int i = 0; i < numOfEndpoints; i++) {
            dataNodes[i] = in.readUTF();
        }
    }

    public long [] getKeys(){
        return keys;
    }

    public long getStartColName() {
        return startColName;
    }

    public long nextKey(){
       if (iteration < keys.length){
            return keys[iteration++];
       }else {
           return -1;
       }
    }

    public long getEndColName() {
        return endColName;
    }
}
