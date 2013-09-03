package org.apache.hadoop.hive.ql.udf.generic;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

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
public abstract class IncrementalGenericUDAFEvaluator extends GenericUDAFEvaluator {

    private static final Logger log = LoggerFactory.getLogger(IncrementalGenericUDAFEvaluator.class);

//    private final static Map<String, String> cassandraValidatorMap = new HashMap<String, String>();

    private final static String KEY_SEPERATOR = "#$@%%$";

    private Cassandra.Client client;
    private KsDef ksDef;
    private String host;
    private String keyspace;
    private String username;
    private String password;

    private CassandraDBHandler cassandraDBHandler;

    public  IncrementalGenericUDAFEvaluator(){
        cassandraDBHandler = new CassandraDBHandler();
    }


    public ByteBuffer getLastProcessedValue(String groupByKey, String operationIdentifier) {


        cassandraDBHandler.login();

        //Since the markerName internally used has the tenantId, scriptName,
        // and the actual MarkerName encoded it it.

        String markerName = HiveContext.getCurrentContext().getConf().
                get(HiveConf.ConfVars.HIVE_INCREMENTAL_MARKER_NAME.varname);

        String cfName = getAggregationFunctionName();
        if (!cassandraDBHandler.createCFIfNotExists(cfName, getDefaultParamsType())) {
            ColumnOrSuperColumn column = cassandraDBHandler.getColumn(cfName, markerName,
                    getColumnKey(groupByKey, operationIdentifier));
            if (null != column) {
                return column.getColumn().bufferForValue();
            }
            return null;
        }
        return null;
    }


    /**
     * This function will be called by GroupByOperator when it sees a new input
     * row.
     *
     * @param agg The object to store the aggregation result.
     */
    public Object evaluate(AggregationBuffer agg) throws HiveException {
        if (mode == Mode.PARTIAL1 || mode == Mode.PARTIAL2) {
            return terminatePartial(agg);
        } else {
            if (mode == getIncrementalEvaluationTerminationMode()) {
                return terminateIncrementalEvaluation(agg);
            } else {
                return terminate(agg);
            }
        }
    }



    public void insertProcessedValue(String groupByKey, String operationIdentifier, PrimitiveObjectInspector.
            PrimitiveCategory valueType, byte[] value) {


        String markerName = HiveContext.getCurrentContext().getConf().get(HiveConf.
                ConfVars.HIVE_INCREMENTAL_MARKER_NAME.varname);

        String cfName = getAggregationFunctionName();
        String columnKey = getColumnKey(groupByKey, operationIdentifier);

        cassandraDBHandler.addCFMetaData(cfName, ByteBufferUtil.bytes(columnKey), valueType);
        cassandraDBHandler.insertData(cfName, ByteBufferUtil.bytes(markerName), ByteBufferUtil.bytes(columnKey), value);
    }


    private String getColumnKey(String groupBykey, String identifier) {
        return groupBykey + KEY_SEPERATOR + identifier;
    }

    public abstract Mode getIncrementalEvaluationTerminationMode();

    public abstract Object terminateIncrementalEvaluation(AggregationBuffer agg) throws HiveException;

    public abstract String getAggregationFunctionName();

    public abstract PrimitiveObjectInspector.PrimitiveCategory getDefaultParamsType();


}
