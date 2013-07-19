package org.wso2.carbon.analytics.hive.annotation.builtin;

import org.wso2.carbon.analytics.hive.annotation.AbstractHiveAnnotation;

public class AnnotationTest extends AbstractHiveAnnotation {

    @Override
    public void init() {
        setAnnotationName("testAnnotationFoo");
        addParameter("foo");
        addParameter("foo1");
    }


    @Override
    public void execute() {
        String a= getParameter("ss");
    }

}
