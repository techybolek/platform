/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.annotation.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.annotation.HiveAnnotation;
import org.wso2.carbon.analytics.hive.exception.AnnotationConfigException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;

/**
 *Build annotations from annotation-config.xml
 */
public class AnnotationBuilder {
    private static final Log log = LogFactory.getLog(AnnotationBuilder.class);


    private static Map<String, Set<String>> annotationConfigs = new HashMap<String, Set<String>>();



    private static Map<String, HiveAnnotation> annotations = new HashMap<String, HiveAnnotation>();


    public static HiveAnnotation getAnnotation(String name){
              return annotations.get(name);
    }
    public static void clearAnnotations() {
       if(!annotations.isEmpty()){
           annotations.clear();
       }
    }

    public static void setAnnotations(Map<String, HiveAnnotation> annotations) {
        AnnotationBuilder.annotations = annotations;
    }

    public static void addAnnotation(String name, HiveAnnotation annotation){

        if(validate(name,annotation.getParameters().keySet())){
        annotations.put(name,annotation);
        } else{
            log.error("Couldn't validate the annotation...");
        }

    }

    public static OMElement loadConfigXML() throws AnnotationConfigException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + HiveConstants.ANNOTATION_CONFIG_XML;

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = HiveConstants.ANNOTATION_CONFIG_XML
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new AnnotationConfigException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + HiveConstants.ANNOTATION_CONFIG_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new AnnotationConfigException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
    }


    public static void populateAnnotations(OMElement config) {



        OMElement annotationElems = config.getFirstChildWithName(
                new QName(HiveConstants.ANALYTICS_NAMESPACE,
                        HiveConstants.ANNOTATIONS__ELEMENT));

        if (annotationElems != null) {


            for (Iterator annotationIterator = annotationElems.getChildrenWithName(new QName(HiveConstants.ANNOTATION_ELEMENT));
                 annotationIterator.hasNext(); ) {
                Set<String> parameterSet=new HashSet<String>();
                OMElement annotation = (OMElement) annotationIterator.next();
                OMElement nameElem = annotation.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE,HiveConstants.ANNOTATION_NAME__ELEMENT));
                OMElement paramElem = annotation.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE,HiveConstants.ANNOTATION_PARAMS_ELEMENT));

                if(paramElem !=null){
                    String params=paramElem.getText();
                    params=params.replaceAll("\\s+", "");
                    String[] parameters = params.split(",");

                    for (String p : parameters) {
                        parameterSet.add(p);
                    }


                }
                annotationConfigs.put(nameElem.getText(), parameterSet);
            }
        }


    }

    private static boolean validate(String name,Set<String> params){
        Set<String> paramValues = annotationConfigs.get(name);

        if (paramValues != null && params != null && paramValues.size() == params.size()) {
            return paramValues.containsAll(params);
        } else {
            return false;
        }
    }

    public static Map<String, HiveAnnotation> lookupAnnotations(){
        return annotations;
    }



}
