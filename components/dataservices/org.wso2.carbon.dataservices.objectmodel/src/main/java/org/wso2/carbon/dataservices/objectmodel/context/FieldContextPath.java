/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.objectmodel.context;

/**
 * This class represents a field context path.
 */
public class FieldContextPath {

    private PathComponent[] components;

    private int lastIndexOffset;

    private String absolutePath;
    
    private FieldContextPath headPath;
    
    public FieldContextPath(PathComponent[] components) throws FieldContextException {
        this(components, 0);
    }

    private FieldContextPath(PathComponent[] components, int lastIndexOffset) throws FieldContextException {
        this.components = components;
        this.lastIndexOffset = lastIndexOffset;
        if (this.lastIndexOffset + 1 < this.components.length) {
            /* generate the head path */
            this.headPath = new FieldContextPath(this.components, this.lastIndexOffset + 1);
        }
        this.populateAbsolutePath();
    }

    private void populateAbsolutePath() {
        StringBuilder builder = new StringBuilder();
        PathComponent tail = this.getTail();
        if (this.getHeadPath() != null) {
            builder.append(this.getHeadPath());
            if (!tail.isIndex()) {
                builder.append(".");
            }
        }
        builder.append(tail.toString());
        this.absolutePath = builder.toString();
    }

    public FieldContextPath getHeadPath() {
        return headPath;
    }

    public PathComponent getTail() {
        return this.components[this.components.length - this.lastIndexOffset - 1];
    }

    public int getLength() {
        return this.components.length - this.lastIndexOffset;
    }

    protected PathComponent[] getComponents() {
        return components;
    }

    public PathComponent getComponentAt(int index) throws FieldContextException {
        if (index + this.lastIndexOffset >= this.components.length) {
            throw new FieldContextException("Invalid index for field context path: " + "Length - "
                    + this.getLength() + " Index - " + index + " Offset - " + this.lastIndexOffset);
        }
        return this.components[index];
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public int hashCode() {
        return this.getAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object rhs) {
        return this.getAbsolutePath().equals(rhs);
    }

    @Override
    public String toString() {
        return this.getAbsolutePath();
    }

    public static class PathComponent {

        private Object value;

        private boolean index;

        private String stringValue;

        public PathComponent(String name) {
            this.value = name;
            this.index = false;
        }

        public PathComponent(int index) {
            this.value = index;
            this.index = true;
        }

        public boolean isIndex() {
            return index;
        }

        public String getStringValue() {
            return (String) value;
        }

        public int getIndexValue() {
            return (Integer) value;
        }

        @Override
        public String toString() {
            if (this.stringValue != null) {
                return this.stringValue;
            }
            if (this.isIndex()) {
                this.stringValue = "[" + this.getIndexValue() + "]";
            } else {
                this.stringValue = this.getStringValue();
            }
            return this.stringValue;
        }

    }

}