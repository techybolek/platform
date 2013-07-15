package org.wso2.carbon.analytics.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.core.common.AnnotationFactory;
import org.wso2.carbon.analytics.core.exception.AnalyticsConfigurationException;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsContext {
    private static final Log log = LogFactory.getLog(AnalyticsContext.class);

    Map<String, String> annotations = new HashMap<String, String>();


    public AnalyticsContext() {

    }

    public void init() {
        AnnotationFactory annotationFactory = new AnnotationFactory();

        try {
            OMElement annotationElem = annotationFactory.loadConfigXML();

            setAnnotations(annotationFactory.populateAnnotations(annotationElem));

        } catch (AnalyticsConfigurationException e) {
            log.error("The Analytics config was not found.");
        }


    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

}
