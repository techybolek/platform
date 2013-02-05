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
package org.wso2.siddhi.query.api.query.input.pattern.element;

import org.wso2.siddhi.query.api.query.input.BasicStream;

public class LogicalElement implements PatternElement {
    BasicStream basicStream1;
    LogicalElement.Type type;
    BasicStream basicStream2;

    public LogicalElement(BasicStream basicStream1, LogicalElement.Type type,
                          BasicStream basicStream2) {
        this.basicStream1 = basicStream1;
        this.type = type;
        this.basicStream2 = basicStream2;
    }

    public BasicStream getBasicStream1() {
        return basicStream1;
    }

    public BasicStream getBasicStream2() {
        return basicStream2;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        AND, OR
    }
}
