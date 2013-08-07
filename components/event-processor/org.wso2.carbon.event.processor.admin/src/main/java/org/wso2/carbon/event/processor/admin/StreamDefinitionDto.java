package org.wso2.carbon.event.processor.admin;

import java.util.List;

public class StreamDefinitionDto {

    private String name;
    private java.util.List<String> attributeList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<String> attributeList) {
        this.attributeList = attributeList;
    }
}
