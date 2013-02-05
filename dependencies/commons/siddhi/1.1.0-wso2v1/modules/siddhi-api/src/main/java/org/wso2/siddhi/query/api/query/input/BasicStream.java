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
package org.wso2.siddhi.query.api.query.input;

import org.wso2.siddhi.query.api.query.QueryEventStream;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.query.input.handler.Handler;
import org.wso2.siddhi.query.api.query.input.handler.Window;
import org.wso2.siddhi.query.api.query.input.pattern.element.PatternElement;
import org.wso2.siddhi.query.api.query.input.sequence.element.SequenceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasicStream implements SingleStream, SequenceElement, PatternElement {

    protected String streamId;
    protected StreamDefinition streamDefinition;
    protected String streamReferenceId;
    protected List<Handler> handlerList = new ArrayList<Handler>();
    protected boolean isCounterStream = false;


    protected BasicStream(String streamId, String streamReferenceId,
                       List<Handler> handlerList) {
        this.streamId = streamId;
        this.streamReferenceId = streamReferenceId;
        this.handlerList = handlerList;
//        isCounterStream = counterStream;
    }

    protected BasicStream(String streamId) {
        this(streamId, streamId);
    }

    public BasicStream(String streamReferenceId, String streamId) {
        this.streamId = streamId;
        this.streamReferenceId = streamReferenceId;
    }

    public BasicStream handler(Handler.Type type, String name, Object... parameters) {
        handlerList.add(new Handler(name, type, parameters));
        return this;
    }

    public BasicStream addHandler(Handler handler) {
        handlerList.add(handler);
        return this;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getStreamReferenceId() {
        return streamReferenceId;
    }

    public BasicStream setStreamReferenceId(String streamReferenceId) {
        this.streamReferenceId = streamReferenceId;
        return this;
    }

    public List<Handler> getHandlerList() {
        return handlerList;
    }

    public BasicStream handler(Condition filterCondition) {
        handlerList.add(new Handler(null, Handler.Type.FILTER, new Object[]{filterCondition}));
        return this;
    }

    @Override
    public List<String> getStreamIds() {
        List<String> list = new ArrayList<String>();
        list.add(streamId);
        return list;
    }

    public void setCounterStream(boolean counterStream) {
        isCounterStream = counterStream;
    }

    @Override
    public List<QueryEventStream> constructQueryEventStreamList(
            Map<String, StreamDefinition> streamDefinitionMap,
            List<QueryEventStream> queryEventStreams) {
        streamDefinition = streamDefinitionMap.get(streamId);
        QueryEventStream queryEventStream = new QueryEventStream(streamId, streamReferenceId, streamDefinition);
        queryEventStream.setCounterStream(isCounterStream);
        queryEventStreams.add(queryEventStream);
        return queryEventStreams;
    }

    public SingleStream window(String name, Object... parameters) {
       return new WindowStream(this,new Window(name, parameters));
    }


    public SingleStream setWindow(Window window) {
        return new WindowStream(this,window);
    }

}
