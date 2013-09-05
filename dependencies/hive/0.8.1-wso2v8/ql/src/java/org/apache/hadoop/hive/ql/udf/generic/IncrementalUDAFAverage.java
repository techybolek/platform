/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.udf.generic;

import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.*;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * IncrementalUDAFAverage.
 */
@Description(name = "avg", value = "_FUNC_(x, id) - Returns the mean of a set by " +
        "considering the last processed results")
public class IncrementalUDAFAverage extends AbstractGenericUDAFResolver {

    static final Log LOG = LogFactory.getLog(IncrementalUDAFAverage.class.getName());

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {
        if(parameters.length != 2){
           throw new UDFArgumentTypeException(parameters.length - 1,
          "Exactly two arguments are expected in incr_avg function. But found "+parameters.length);
        }

        if (parameters[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(0,
                    "Only primitive type arguments are accepted but "
                            + parameters[0].getTypeName() + " is passed.");
        }

        if (parameters[1].getCategory() != ObjectInspector.Category.PRIMITIVE ||
                !((PrimitiveTypeInfo) parameters[1]).getPrimitiveCategory().
                        equals(PrimitiveObjectInspector.PrimitiveCategory.STRING)) {
            throw new UDFArgumentTypeException(1,
                    "Only String type is accepted but "
                            + parameters[0].getTypeName() + " is passed for operation Id.");
        }


        switch (((PrimitiveTypeInfo) parameters[0]).getPrimitiveCategory()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case TIMESTAMP:
                return new IncrementalUDAFAverageEvaluator();
            case BOOLEAN:
            default:
                throw new UDFArgumentTypeException(0,
                        "Only numeric or string type arguments are accepted but "
                                + parameters[0].getTypeName() + " is passed.");
        }
    }

    /**
     * IncrementalUDAFAverageEvaluator.
     */
    public static class IncrementalUDAFAverageEvaluator extends IncrementalGenericUDAFEvaluator {
        private static final String SUM_VARIABLE = "sum";
        private static final String COUNT_VARIABLE = "count";

        // For PARTIAL1 and COMPLETE
        PrimitiveObjectInspector inputOI;
        PrimitiveObjectInspector idOI;

//        For PARTIAL2 and FINAL
        StructObjectInspector soi;
        StructField countField;
        StructField sumField;
        StructField idField;

        LongObjectInspector countFieldOI;
        DoubleObjectInspector sumFieldOI;
        StringObjectInspector idFieldOI;

        // For PARTIAL1 and PARTIAL2
        Object[] partialResult;

        // For FINAL and COMPLETE
        DoubleWritable result;


        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters)
                throws HiveException {
//      assert (parameters.length == 1);
            super.init(m, parameters);

            // init input
            if (mode == Mode.PARTIAL1 || mode == Mode.COMPLETE) {
                inputOI = (PrimitiveObjectInspector) parameters[0];
                idOI = (PrimitiveObjectInspector) parameters[1];

            } else {
                soi = (StructObjectInspector) parameters[0];
                countField = soi.getStructFieldRef("count");
                sumField = soi.getStructFieldRef("sum");
                idField = soi.getStructFieldRef("operationId");

                countFieldOI = (LongObjectInspector) countField
                        .getFieldObjectInspector();
                sumFieldOI = (DoubleObjectInspector) sumField.getFieldObjectInspector();
                idFieldOI = (StringObjectInspector) idField.getFieldObjectInspector();
            }

            // init output
            if (mode == Mode.PARTIAL1 || mode == Mode.PARTIAL2) {
                // The output of a partial aggregation is a struct containing
                // a "long" count and a "double" sum.

                ArrayList<ObjectInspector> foi = new ArrayList<ObjectInspector>();
                foi.add(PrimitiveObjectInspectorFactory.writableLongObjectInspector);
                foi.add(PrimitiveObjectInspectorFactory.writableDoubleObjectInspector);
                foi.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector);
                ArrayList<String> fname = new ArrayList<String>();
                fname.add("count");
                fname.add("sum");
                fname.add("operationId");
                partialResult = new Object[3];
                partialResult[0] = new LongWritable(0);
                partialResult[1] = new DoubleWritable(0);
                partialResult[2] = new Text();
                return ObjectInspectorFactory.getStandardStructObjectInspector(fname,
                        foi);

            } else {
                result = new DoubleWritable(0);
                return PrimitiveObjectInspectorFactory.writableDoubleObjectInspector;
            }
        }


        static class IncrementalAverageAgg implements AggregationBuffer, IncrementalBuffer {
            long count;
            double sum;
            String groupByString;
            String id;

            public void setRowKey(String key) {
                groupByString = key;
            }

            public void setId(String operationId){
                id = operationId;
            }


        }

        @Override
        public AggregationBuffer getNewAggregationBuffer() throws HiveException {
            IncrementalAverageAgg result = new IncrementalAverageAgg();
            reset(result);
            return result;
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            IncrementalAverageAgg myagg = (IncrementalAverageAgg) agg;
            myagg.count = 0;
            myagg.sum = 0;
            myagg.groupByString = "";
            myagg.id = "";
        }

        boolean warned = false;

        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters)
                throws HiveException {
            assert (parameters.length == 1);
            Object p = parameters[0];
            Object id = parameters[1];

            if (p != null) {
                IncrementalAverageAgg myagg = (IncrementalAverageAgg) agg;
                try {
                    double v = PrimitiveObjectInspectorUtils.getDouble(p, inputOI);
                    String idValue = PrimitiveObjectInspectorUtils.getString(id, idOI);

                    myagg.count++;
                    myagg.sum += v;
                    myagg.id = idValue;
                } catch (NumberFormatException e) {
                    if (!warned) {
                        warned = true;
                        LOG.warn(getClass().getSimpleName() + " "
                                + StringUtils.stringifyException(e));
                        LOG.warn(getClass().getSimpleName()
                                + " ignoring similar exceptions.");
                    }
                }
            }
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            IncrementalAverageAgg myagg = (IncrementalAverageAgg) agg;
            ((LongWritable) partialResult[0]).set(myagg.count);
            ((DoubleWritable) partialResult[1]).set(myagg.sum);
            ((Text) partialResult[2]).set(myagg.id);
            return partialResult;
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial)
                throws HiveException {
            if (partial != null) {
                IncrementalAverageAgg myagg = (IncrementalAverageAgg) agg;
                Object partialCount = soi.getStructFieldData(partial, countField);
                Object partialSum = soi.getStructFieldData(partial, sumField);
                Object partialId = soi.getStructFieldData(partial, idField);

                myagg.count += countFieldOI.get(partialCount);
                myagg.sum += sumFieldOI.get(partialSum);
                myagg.id = idFieldOI.getPrimitiveJavaObject(partialId);
            }
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            IncrementalAverageAgg myagg = (IncrementalAverageAgg) agg;
            if (myagg.count == 0) {
                return null;
            } else {
                result.set(myagg.sum / myagg.count);
                return result;
            }
        }


        public Object terminateIncrementalEvaluation(AggregationBuffer agg) throws HiveException {
            IncrementalAverageAgg myagg = (IncrementalAverageAgg) agg;
            double lastProcessedSum = 0;
            long lastCount = 0;
            ByteBuffer sumValue = getLastProcessedValue(myagg.groupByString,
                    getVariableId(SUM_VARIABLE, myagg.id));
            if (null != sumValue) {
                lastProcessedSum = sumValue.getDouble();
            }
            ByteBuffer countValue = getLastProcessedValue(myagg.groupByString,
                    getVariableId(COUNT_VARIABLE, myagg.id));
            if (null != countValue) {
                lastCount = countValue.getLong();
            }

            double finalSum = myagg.sum + lastProcessedSum;
            long finalCount = myagg.count + lastCount;

            if (myagg.count + lastCount == 0) {
                return null;
            } else {
                result.set(finalSum / finalCount);
                insertProcessedValue(myagg.groupByString, getVariableId(SUM_VARIABLE, myagg.id),
                        PrimitiveObjectInspector.PrimitiveCategory.DOUBLE,
                        ByteBufferUtil.bytes(finalSum).array());
                insertProcessedValue(myagg.groupByString, getVariableId(COUNT_VARIABLE, myagg.id),
                        PrimitiveObjectInspector.PrimitiveCategory.LONG,
                        ByteBufferUtil.bytes(finalCount).array());
                return result;
            }
        }

        private String getVariableId(String variableName, String id){
          return variableName+"_"+id;
        }



        public Mode getIncrementalEvaluationTerminationMode() {
            return Mode.FINAL;
        }

        public String getAggregationFunctionName() {
            return "incr_avg";
        }

        public PrimitiveObjectInspector.PrimitiveCategory getDefaultParamsType() {
            return PrimitiveObjectInspector.PrimitiveCategory.INT;
        }


    }

}
