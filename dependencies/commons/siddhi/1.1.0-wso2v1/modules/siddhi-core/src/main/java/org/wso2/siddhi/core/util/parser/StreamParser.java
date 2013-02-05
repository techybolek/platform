/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.core.util.parser;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.CannotLoadClassException;
import org.wso2.siddhi.core.exception.EventStreamNotExistException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.SimpleHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.chain.HandlerChain;
import org.wso2.siddhi.core.query.processor.handler.chain.MultipleHandlerChain;
import org.wso2.siddhi.core.query.processor.handler.chain.PassThroughHandlerChain;
import org.wso2.siddhi.core.query.processor.handler.chain.SingleHandlerChain;
import org.wso2.siddhi.core.query.processor.handler.chain.filter.DefaultFilterHandler;
import org.wso2.siddhi.core.query.processor.handler.chain.filter.FilterHandler;
import org.wso2.siddhi.core.query.processor.handler.pattern.*;
import org.wso2.siddhi.core.query.processor.handler.sequence.CountSequenceInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.OrSequenceInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.SequenceHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.SequenceInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.join.*;
import org.wso2.siddhi.core.query.processor.window.RunnableWindowProcessor;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.core.query.projector.QueryProjector;
import org.wso2.siddhi.core.statemachine.pattern.AndPatternState;
import org.wso2.siddhi.core.statemachine.pattern.CountPatternState;
import org.wso2.siddhi.core.statemachine.pattern.OrPatternState;
import org.wso2.siddhi.core.statemachine.pattern.PatternState;
import org.wso2.siddhi.core.statemachine.sequence.CountSequenceState;
import org.wso2.siddhi.core.statemachine.sequence.OrSequenceState;
import org.wso2.siddhi.core.statemachine.sequence.SequenceState;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.condition.ConditionValidator;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.query.QueryEventStream;
import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.input.Stream;
import org.wso2.siddhi.query.api.query.input.WindowStream;
import org.wso2.siddhi.query.api.query.input.handler.Handler;
import org.wso2.siddhi.query.api.query.input.handler.Window;
import org.wso2.siddhi.query.api.query.input.pattern.PatternStream;
import org.wso2.siddhi.query.api.query.input.sequence.SequenceStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StreamParser {

    static final Logger log = Logger.getLogger(StreamParser.class);


    public static List<HandlerProcessor> parseStream(Stream queryStream,
                                                     List<QueryEventStream> queryEventStreamList,
                                                     QueryProjector queryProjector,
                                                     SiddhiContext siddhiContext) {
        List<HandlerProcessor> handlerProcessorList = new ArrayList<HandlerProcessor>();

        if (queryStream instanceof BasicStream) {

            SimpleHandlerProcessor simpleHandlerProcessor = new SimpleHandlerProcessor((BasicStream) queryStream, parseFilerHandlerChain((BasicStream) queryStream, queryEventStreamList), siddhiContext);

            if (queryStream instanceof WindowStream) {
                WindowProcessor windowProcessor = generateWindowProcessor(((WindowStream) queryStream).getWindow(), siddhiContext, null, false, getStreamDefinition((BasicStream) queryStream, queryEventStreamList));
                simpleHandlerProcessor.setNext(windowProcessor);
                windowProcessor.setNext(queryProjector);
            } else {
                simpleHandlerProcessor.setNext(queryProjector);
            }

            handlerProcessorList.add(simpleHandlerProcessor);
            return handlerProcessorList;

        } else if (queryStream instanceof JoinStream) {
            ConditionExecutor onConditionExecutor;
            if (((JoinStream) queryStream).getOnCompare() != null) {
                onConditionExecutor = ExecutorParser.parseCondition(((JoinStream) queryStream).getOnCompare(), queryEventStreamList, null);
            } else {
                onConditionExecutor = ExecutorParser.parseCondition(Condition.bool(Expression.value(true)), queryEventStreamList, null);
            }
            JoinProcessor leftInStreamJoinProcessor;
            JoinProcessor rightInStreamJoinProcessor;
            JoinProcessor leftRemoveStreamJoinProcessor;
            JoinProcessor rightRemoveStreamJoinProcessor;
            Lock lock;
            if (siddhiContext.isDistributedProcessing()) {
                lock = siddhiContext.getHazelcastInstance().getLock(siddhiContext.getElementIdGenerator().createNewId() + "-join-lock");
            } else {
                lock = new ReentrantLock();
            }
            switch (((JoinStream) queryStream).getTrigger()) {
                case LEFT:
                    leftInStreamJoinProcessor = new LeftInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    rightInStreamJoinProcessor = new RightInStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessing(), lock);
                    leftRemoveStreamJoinProcessor = new LeftRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    rightRemoveStreamJoinProcessor = new RightRemoveStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessing(), lock);
                    break;
                case RIGHT:
                    leftInStreamJoinProcessor = new LeftInStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessing(), lock);
                    rightInStreamJoinProcessor = new RightInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    leftRemoveStreamJoinProcessor = new LeftRemoveStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessing(), lock);
                    rightRemoveStreamJoinProcessor = new RightRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    break;
                default:
                    leftInStreamJoinProcessor = new LeftInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    rightInStreamJoinProcessor = new RightInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    leftRemoveStreamJoinProcessor = new LeftRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    rightRemoveStreamJoinProcessor = new RightRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessing(), lock);
                    break;
            }
            Constant within = ((JoinStream) queryStream).getWithin();
            if (within != null) {
                long longValue = ExecutorParser.getLong(within);
                leftInStreamJoinProcessor.setWithin(longValue);
                rightInStreamJoinProcessor.setWithin(longValue);
                leftRemoveStreamJoinProcessor.setWithin(longValue);
                rightRemoveStreamJoinProcessor.setWithin(longValue);
            }
            BasicStream leftStream = (BasicStream) ((JoinStream) queryStream).getLeftStream();
            BasicStream rightStream = (BasicStream) ((JoinStream) queryStream).getRightStream();

            WindowProcessor leftWindowProcessor = generateWindowProcessor(leftStream instanceof WindowStream ? ((WindowStream) leftStream).getWindow() : new Window("length", new Object[]{Integer.MAX_VALUE}), siddhiContext, lock, false, getStreamDefinition(leftStream, queryEventStreamList));
            WindowProcessor rightWindowProcessor = generateWindowProcessor(rightStream instanceof WindowStream ? ((WindowStream) rightStream).getWindow() : new Window("length", new Object[]{Integer.MAX_VALUE}), siddhiContext, lock, false, getStreamDefinition(rightStream, queryEventStreamList));

            SimpleHandlerProcessor leftSimpleHandlerProcessor = new SimpleHandlerProcessor(leftStream, parseFilerHandlerChain(leftStream, queryEventStreamList), siddhiContext);
            SimpleHandlerProcessor rightSimpleHandlerProcessor = new SimpleHandlerProcessor(rightStream, parseFilerHandlerChain(rightStream, queryEventStreamList), siddhiContext);

            leftSimpleHandlerProcessor.setNext(leftInStreamJoinProcessor);
            rightSimpleHandlerProcessor.setNext(rightInStreamJoinProcessor);

            //joinStreamPacker next
            leftInStreamJoinProcessor.setNext(queryProjector);
            rightInStreamJoinProcessor.setNext(queryProjector);
            leftRemoveStreamJoinProcessor.setNext(queryProjector);
            rightRemoveStreamJoinProcessor.setNext(queryProjector);

            //Window joinStreamPacker relation settings
            leftInStreamJoinProcessor.setWindowProcessor(leftWindowProcessor);
            leftWindowProcessor.setNext(leftRemoveStreamJoinProcessor);

            rightInStreamJoinProcessor.setWindowProcessor(rightWindowProcessor);
            rightWindowProcessor.setNext(rightRemoveStreamJoinProcessor);


            //joinStreamPacker prev
            rightInStreamJoinProcessor.setOppositeWindowProcessor(leftInStreamJoinProcessor.getWindowProcessor());
            leftInStreamJoinProcessor.setOppositeWindowProcessor(rightInStreamJoinProcessor.getWindowProcessor());

            rightRemoveStreamJoinProcessor.setOppositeWindowProcessor(leftInStreamJoinProcessor.getWindowProcessor());
            leftRemoveStreamJoinProcessor.setOppositeWindowProcessor(rightInStreamJoinProcessor.getWindowProcessor());

            handlerProcessorList.add(leftSimpleHandlerProcessor);
            handlerProcessorList.add(rightSimpleHandlerProcessor);
            return handlerProcessorList;

        } else if (queryStream instanceof PatternStream) {

            List<PatternState> patternStateList = StateParser.convertToPatternStateList(StateParser.identifyStates(((PatternStream) queryStream).getPatternElement()));
            //    queryEventStreamList ;
            // PatternQueryStreamPacker patternStreamPacker = new PatternQueryStreamPacker(stateList);
            // PatternQuerySingleStreamProcessor[] patternSingleStreamAnalyserArray = new PatternQuerySingleStreamProcessor[stateList.size()];
            for (String streamId : queryStream.getStreamIds()) {

                //    List<BasicStream> streamList = new ArrayList<BasicStream>();
                List<PatternInnerHandlerProcessor> patternInnerHandlerProcessorList = new ArrayList<PatternInnerHandlerProcessor>();
                for (PatternState state : patternStateList) {
                    if (state.getBasicStream().getStreamId().equals(streamId)) {
                        //           streamList.add(state.getBasicStream());

                        HandlerChain handlerChain = parseFilerHandlerChain((BasicStream) state.getBasicStream(), queryEventStreamList);
                        PatternInnerHandlerProcessor patternInnerHandlerProcessor;

                        if (state instanceof OrPatternState) {
                            patternInnerHandlerProcessor = new OrPatternInnerHandlerProcessor(((OrPatternState) state), handlerChain, patternStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        } else if (state instanceof AndPatternState) {
                            patternInnerHandlerProcessor = new AndPatternInnerHandlerProcessor(((AndPatternState) state), handlerChain, patternStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        } else if (state instanceof CountPatternState) {
                            patternInnerHandlerProcessor = new CountPatternInnerHandlerProcessor(((CountPatternState) state), handlerChain, patternStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        } else {
                            patternInnerHandlerProcessor = new PatternInnerHandlerProcessor(state, handlerChain, patternStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        }

                        state.setPatternInnerHandlerProcessor(patternInnerHandlerProcessor);
                        patternInnerHandlerProcessorList.add(patternInnerHandlerProcessor);
                        //  patternSingleStreamAnalyserArray[state.getStateNumber()] = patternInnerHandlerProcessor;

                        patternInnerHandlerProcessor.setQueryProjector(queryProjector);

                        //patternInnerHandlerProcessor.setPrevious(singleStreamPacker); since not needed not set
                    }
                }

                PatternHandlerProcessor patternHandlerProcessor = new PatternHandlerProcessor(streamId, patternInnerHandlerProcessorList, siddhiContext);
                patternHandlerProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

                handlerProcessorList.add(patternHandlerProcessor);

                //for persistence, elementId marking and window
                for (PatternInnerHandlerProcessor patternInnerHandlerProcessor : patternInnerHandlerProcessorList) {
                    if (((PatternStream) queryStream).getWithin() != null) {
                        patternInnerHandlerProcessor.setWithin(ExecutorParser.getLong(((PatternStream) queryStream).getWithin()));
                    }
                    siddhiContext.getPersistenceService().addPersister(patternInnerHandlerProcessor);
                }

            }


            //   patternStreamPacker.setPatternSingleStreamAnalyserArray(patternSingleStreamAnalyserArray);
            //patternStreamPacker next
            //  patternStreamPacker.setNext(next, 0);

            for (PatternState state : patternStateList) {
                state.getInnerHandlerProcessor().init();
            }
            return handlerProcessorList;

        } else if (queryStream instanceof SequenceStream) {


            List<SequenceState> sequenceStateList = StateParser.convertToSequenceStateList(StateParser.identifyStates(((SequenceStream) queryStream).getSequenceElement()));
            //    queryEventStreamList ;
            // PatternQueryStreamPacker patternStreamPacker = new PatternQueryStreamPacker(stateList);
            // PatternQuerySingleStreamProcessor[] patternSingleStreamAnalyserArray = new PatternQuerySingleStreamProcessor[stateList.size()];
            for (String streamId : queryStream.getStreamIds()) {

                //    List<BasicStream> streamList = new ArrayList<BasicStream>();
                List<SequenceInnerHandlerProcessor> sequenceInnerHandlerProcessorList = new ArrayList<SequenceInnerHandlerProcessor>();
                for (SequenceState state : sequenceStateList) {
                    if (state.getBasicStream().getStreamId().equals(streamId)) {
                        //           streamList.add(state.getBasicStream());


                        HandlerChain handlerChain = parseFilerHandlerChain(state.getBasicStream(), queryEventStreamList);
                        SequenceInnerHandlerProcessor sequenceInnerHandlerProcessor;


                        if (state instanceof OrSequenceState) {
                            sequenceInnerHandlerProcessor = new OrSequenceInnerHandlerProcessor(((OrSequenceState) state), handlerChain, sequenceStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        } else if (state instanceof CountSequenceState) {
                            sequenceInnerHandlerProcessor = new CountSequenceInnerHandlerProcessor(((CountSequenceState) state), handlerChain, sequenceStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        } else {
                            sequenceInnerHandlerProcessor = new SequenceInnerHandlerProcessor(state, handlerChain, sequenceStateList.size(), siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                        }

                        state.setSequenceInnerHandlerProcessor(sequenceInnerHandlerProcessor);
                        sequenceInnerHandlerProcessorList.add(sequenceInnerHandlerProcessor);
                        //  patternSingleStreamAnalyserArray[state.getStateNumber()] = patternSingleStreamAnalyser;

                        sequenceInnerHandlerProcessor.setNext(queryProjector);

                    }
                }

                SequenceHandlerProcessor sequenceHandlerProcessor = new SequenceHandlerProcessor(streamId, sequenceInnerHandlerProcessorList, siddhiContext);
                sequenceHandlerProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

                handlerProcessorList.add(sequenceHandlerProcessor);

                //for persistence, elementId marking and window
                for (SequenceInnerHandlerProcessor sequenceInnerHandlerProcessor : sequenceInnerHandlerProcessorList) {
                    if (((SequenceStream) queryStream).getWithin() != null) {
                        sequenceInnerHandlerProcessor.setWithin(ExecutorParser.getLong(((SequenceStream) queryStream).getWithin()));
                    }
                    siddhiContext.getPersistenceService().addPersister(sequenceInnerHandlerProcessor);
                }
            }

            //   patternStreamPacker.setPatternSingleStreamAnalyserArray(patternSingleStreamAnalyserArray);
            //patternStreamPacker next
            //  patternStreamPacker.setNext(next, 0);

            for (SequenceState state : sequenceStateList) {
                state.getSequenceInnerHandlerProcessor().init();
            }

            for (HandlerProcessor queryStreamProcessor : handlerProcessorList) {
                List<SequenceInnerHandlerProcessor> otherStreamAnalyserList = new ArrayList<SequenceInnerHandlerProcessor>();
                for (HandlerProcessor otherQueryStreamProcessor : handlerProcessorList) {
                    if (otherQueryStreamProcessor != queryStreamProcessor) {
                        otherStreamAnalyserList.addAll(((SequenceHandlerProcessor) otherQueryStreamProcessor).getSequenceInnerHandlerProcessorList());
                    }
                }
                ((SequenceHandlerProcessor) queryStreamProcessor).setOtherSequenceInnerHandlerProcessorList(otherStreamAnalyserList);
            }
            return handlerProcessorList;
        }
        return handlerProcessorList;

    }

    private static StreamDefinition getStreamDefinition(BasicStream basicStream, List<QueryEventStream> queryEventStreamList) {
        for (QueryEventStream queryEventStream : queryEventStreamList) {
            if (queryEventStream.getStreamId().equals(basicStream.getStreamId()) || queryEventStream.getReferenceStreamId().equals(basicStream.getStreamReferenceId())) {
                return queryEventStream.getStreamDefinition();
            }
        }
        throw new EventStreamNotExistException("no stream found with StreamId: " + basicStream.getStreamId() + " StreamReferenceId: " + basicStream.getStreamReferenceId());
    }

//    private static Handler detachWindow(BasicStream singleStream) {
//        Handler windowProcessor = new Handler("length", Handler.Type.WIN, new Object[]{Integer.MAX_VALUE});
//        for (Iterator<Handler> iterator = singleStream.getHandlerList().iterator(); iterator.hasNext(); ) {
//            Handler handler = iterator.next();
//            if (handler.getOutputType() == Handler.Type.WIN) {
//                windowProcessor = handler;
//                iterator.remove();
//            }
//        }
//        return windowProcessor;
//
//    }

    private static HandlerChain parseFilerHandlerChain(BasicStream inputStream,
                                                       List<QueryEventStream> queryEventStreamList) {
        List<FilterHandler> filterHandlerList = new ArrayList<FilterHandler>();
        List<Handler> handlerList = inputStream.getHandlerList();
        for (Handler handler : handlerList) {
            FilterHandler streamHandler = null;
            if (handler.getType() == Handler.Type.FILTER) {
                if (handler.getName() == null) {   //default filter
                    Condition condition = (Condition) handler.getParameters()[0];
                    ConditionValidator.validate(condition, queryEventStreamList, inputStream.getStreamReferenceId());
                    streamHandler = new DefaultFilterHandler(ExecutorParser.parseCondition(condition, queryEventStreamList, inputStream.getStreamReferenceId()));

                }
            }
            filterHandlerList.add(streamHandler);

        }
        if (filterHandlerList.size() == 1) {
            return new SingleHandlerChain(filterHandlerList.get(0));
        } else if (filterHandlerList.size() > 1) {
            return new MultipleHandlerChain(filterHandlerList);
        } else {
            return new PassThroughHandlerChain();
        }
//        if (inputStream instanceof WindowStream) {
//            filterHandlerList.add(generateWindowProcessor(((WindowStream) inputStream).getWindow(), context));
//        }
//        if (filterHandlerList.size() > 0) {
//            QueryStreamHandler lastStreamHandler = (QueryStreamHandler) filterHandlerList.get(filterHandlerList.size() - 1);
//            lastStreamHandler.setNext(singleStreamPacker);
//
//        }
    }

//    private static void connectToStreamFlow(List<QueryStreamProcessor> queryStreamProcessorList,
//                                            QueryStreamProcessor queryStreamProcessor) {
//        if (queryStreamProcessorList.size() > 0) {
//            QueryStreamHandler prevStreamHandler = (QueryStreamHandler) queryStreamProcessorList.get(queryStreamProcessorList.size() - 1);
//            prevStreamHandler.setNext(queryStreamProcessor);
//        }
//        queryStreamProcessorList.add(queryStreamProcessor);
//    }

    private static WindowProcessor generateWindowProcessor(Window window, SiddhiContext siddhiContext, Lock lock,
                                                           boolean async, StreamDefinition streamDefinition) {
        WindowProcessor windowProcessor = null;
        try {
            windowProcessor = (WindowProcessor) org.wso2.siddhi.core.util.ClassLoader.loadClass(WindowProcessor.class.getPackage().getName() +
                    "." + window.getName().substring(0, 1).toUpperCase() + window.getName().substring(1) + "WindowProcessor");
        } catch (CannotLoadClassException e) {
            throw new QueryCreationException("Cannot load window for the name " + window.getName(), e);
        }
//                    Window window = new TimeWindowProcessor();
        windowProcessor.setSiddhiContext(siddhiContext);
        windowProcessor.setStreamDefinition(streamDefinition);
        if (windowProcessor instanceof RunnableWindowProcessor) {
            ((RunnableWindowProcessor) windowProcessor).setScheduledExecutorService(siddhiContext.getScheduledExecutorService());
            ((RunnableWindowProcessor) windowProcessor).setThreadBarrier(siddhiContext.getThreadBarrier());
        }
        windowProcessor.setParameters(window.getParameters());

        //for adding elementId
        windowProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

        if (lock == null) {
            if (siddhiContext.isDistributedProcessing()) {
                windowProcessor.setLock(siddhiContext.getHazelcastInstance().getLock(windowProcessor.getElementId() + "-lock"));
            } else {
                windowProcessor.setLock(new ReentrantLock());
            }
        } else {
            windowProcessor.setLock(lock);
        }
        //for persistence
        siddhiContext.getPersistenceService().addPersister(windowProcessor);
        windowProcessor.init(async);
        return windowProcessor;
    }

}
