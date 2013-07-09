/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.util.xpath;

import com.jayway.jsonpath.JsonPath;


public class SynapseJsonPath {

    private String jsonPathExpression;
    private JsonPath jsonPath;

    public String getJsonPathExpression() {
        return jsonPathExpression;
    }

    public void setJsonPathExpression(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
    }

    public SynapseJsonPath(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
        jsonPath=JsonPath.compile(jsonPathExpression);
    }

    public String stringValueOf(final String jsonStream){

        Object read;
        read = jsonPath.read(jsonStream);
        return read.toString();

        /*if(read instanceof String){
            return (String)read;
        } else {
            return ((List<String>)read).toString();
        }*/
    }
}
