/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.AddLink;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.CheckBox;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.CloseAddLink;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.DateField;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.DropDown;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.OptionText;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.TextArea;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.TextField;
import org.wso2.carbon.governance.generic.ui.common.dataobjects.UIComponent;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;

/* This is the class which generate the service UI by reading service-config.xml */
public class GenericUIGenerator {

    private static final Log log = LogFactory.getLog(GenericUIGenerator.class);

    private String dataElement;
    private String dataNamespace;

    public GenericUIGenerator() {
        this(UIGeneratorConstants.DATA_ELEMENT, UIGeneratorConstants.DATA_NAMESPACE);
    }

    public GenericUIGenerator(String dataElement, String dataNamespace) {
        this.dataElement = dataElement;
        this.dataNamespace = dataNamespace;
    }

    //StringBuffer serviceUI;

    public OMElement getUIConfiguration(String content, HttpServletRequest request,
                                        ServletConfig config, HttpSession session) throws Exception {
        OMElement omElement = null;
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(content));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            omElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            log.error("Unable to parse the UI configuration.", e);
        }
        return omElement;
    }


    public String printWidgetWithValues(OMElement widget, OMElement data,
                                        boolean isFilterOperation, HttpServletRequest request,
                                        ServletConfig config) {
        return printWidgetWithValues(widget, data, isFilterOperation, true, true, request, config);
    }

    public String printWidgetWithValues(OMElement widget, OMElement data,
                                        boolean isFilterOperation, boolean markReadonly, boolean hasValue, HttpServletRequest request,
                                        ServletConfig config) {
        if (isFilterOperation && Boolean.toString(false).equals(
                widget.getAttributeValue(new QName(null, UIGeneratorConstants.FILTER_ATTRIBUTE)))) {
            return "";
        }
        int columns = 2; //default value of number of columns is 2
        String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
        boolean collapsed = true;  // Default collapse set to true
        String widgetCollapse = widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLLAPSED));
        if (widgetCollapse != null) {
            collapsed = Boolean.valueOf(widgetCollapse);
        }

        String divId = "_collapse_id_" + widgetName.replaceAll(" ", "");

        OMElement dataHead = null;
        if (data != null) {
            dataHead = GenericUtil.getChildWithName(data, widgetName, dataNamespace);
        }
        if (widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLUMN)) != null) {
            columns = Integer.parseInt(widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_COLUMN)));
        }
        Iterator subHeadingIt = widget.getChildrenWithName(new QName(null, UIGeneratorConstants.SUBHEADING_ELEMENT));
        StringBuilder table = new StringBuilder();
        table.append("<div id=\"" + divId + "\"  "+"onmouseover='title=\"\"' onmouseout='title=\""+String.valueOf(collapsed)+"\"'"
                + " title=\"" + String.valueOf(collapsed) + "\"><table class=\"normal-nopadding\" cellspacing=\"0\">");
        List<String> subList = new ArrayList<String>();
        OMElement sub = null;
        if (subHeadingIt != null && subHeadingIt.hasNext()) {
            sub = (OMElement) subHeadingIt.next(); // NO need to have multiple subheading elements in a single widget element
        }
        if (sub != null && UIGeneratorConstants.SUBHEADING_ELEMENT.equals(sub.getLocalName())) {
            Iterator headingList = sub.getChildrenWithLocalName(UIGeneratorConstants.HEADING_ELEMENT);
            while (headingList.hasNext()) {
                OMElement subheading = (OMElement) headingList.next();
                subList.add(subheading.getText());
            }
            if (subList.size() > columns) {
                /*This is the place where special scenario comes in to play with number of columns other
              than having two columns
                */
                return ""; // TODO: throw an exception
            }
        }
        table.append(printMainHeader(widgetName, columns));
        if (subList.size() > 2) {
            //if the column size is not 2 we print sub-headers first before going in to loop
            //In this table there should not be any field with maxOccurs unbounded//
            table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
        }
        Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
        int columnCount = 0;
        int rowCount = 0;
        OMElement inner = null;
        while (arguments.hasNext()) {
            OMElement arg = (OMElement) arguments.next();
            String maxOccurs = "";
            if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                if (isFilterOperation && Boolean.toString(false).equals(
                        arg.getAttributeValue(new QName(null, UIGeneratorConstants.FILTER_ATTRIBUTE)))) {
                    continue;
                }
                rowCount++; //this variable used to find the which raw is in and use this to print the sub header
                String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                String tooltip = arg.getAttributeValue(new QName(null,
                        UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
                if (tooltip == null) {
                    tooltip = "";
                }
                tooltip = StringEscapeUtils.escapeHtml(tooltip);
                //Read the maxOccurs value
                maxOccurs = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT));
                if (maxOccurs != null) {
                    if (!UIGeneratorConstants.MAXOCCUR_BOUNDED.equals(maxOccurs) && !UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
                        //if user has given something else other than unbounded
                        return ""; //TODO: throw an exception
                    }
                    if (!UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
                        //if maxOccurs is not unbounded then print the sub header otherwise we will show the adding link
                        if (rowCount == 1) {
                            // We print the sub header only when we parse the first element otherwise we'll print sub header for each field element
                            table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
                        }
                    }
                } else {
                    if (subList.size() == 2 && rowCount == 1) {
                        // We print the sub header only when we parse the first element otherwise we'll print sub header for each field element
                        // sub headers are printed in this position only if column number is exactly 2//
                        table.append(printSubHeaders(subList.toArray(new String[subList.size()])));
                    }
                }
                if (dataHead != null) {
                    //if the data xml contains the main element then get the element contains value
                    inner = GenericUtil.getChildWithName(dataHead, arg.getFirstChildWithName
                            (new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText().replaceAll(" ", "-"),
                            dataNamespace);
                }
                String value = null;
                String optionValue = null;
                if (UIGeneratorConstants.TEXT_FIELD.equals(elementType)) {
                    String mandat = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));

                    boolean isReadOnly = false;

                    if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                        isReadOnly = true;
                    }
                    if (isFilterOperation) {
                        mandat = "false";
                    }
                    boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                            new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
                    String urlTemplate = arg.getAttributeValue(
                            new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                    boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                            new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
                    String startsWith = arg.getAttributeValue(new QName(null,UIGeneratorConstants.PATH_START_WITH));
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    } else {
                        value = arg.getAttributeValue(new QName(null, UIGeneratorConstants.DEFAULT_ATTRIBUTE));
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        UIComponent textField = new TextField(null, arg.getFirstChildWithName(new QName(null,
                                    UIGeneratorConstants.ARGUMENT_NAME)).getText(),null, widgetName,
                                    value, isURL, urlTemplate, isPath, isReadOnly,
                                    hasValue, tooltip, startsWith, request);
                        table.append(textField.generate());
                
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }

                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }
                        UIComponent text =  new TextField(label, name, mandat, widgetName, value,
                        			isURL, urlTemplate, isPath, isReadOnly, hasValue,
                                    tooltip, startsWith, request);
                        table.append(text.generate());
                    }
                } else if (UIGeneratorConstants.DATE_FIELD.equals(elementType)) {
                    String mandet = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    boolean isReadOnly = false;

                    if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                        isReadOnly = true;
                    }
                    if (isFilterOperation) {
                        mandet = "false";
                    }
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    }

                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        UIComponent dateField = new DateField(null,arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(),null,widgetName, value, isReadOnly, tooltip,false);
                        table.append(dateField.generate());
                        
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }
                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }

                        UIComponent dateField = new DateField(label, name, mandet, widgetName, value,isReadOnly, tooltip,true);
                        table.append(dateField.generate());
                    }
                } else if (UIGeneratorConstants.OPTION_FIELD.equals(elementType)) {
                    OMElement firstChildWithName = arg.getFirstChildWithName(
                            new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                    String mandat = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    String name = firstChildWithName.getText();
                    String label = firstChildWithName.getAttributeValue(
                            new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                    if (label == null) {
                        label = name;
                    }

                    if (inner != null) {
                        //if the element contains value is not null get the value
                        optionValue = inner.getText();
                    }
                    List<String> optionValues = getOptionValues(arg, request, config);
                    if (isFilterOperation) {
                        optionValues.add(0, "");
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        UIComponent dropDown = new DropDown(null,name, null,optionValues.toArray(new String[optionValues.size()]),
                        	                                    widgetName, optionValue, tooltip);
                        table.append(dropDown.generate());
                        
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }

                    } else {
                    	UIComponent dropDown = new DropDown(label, name, mandat,
                                    optionValues.toArray(new String[optionValues.size()]),
                                    widgetName, optionValue, tooltip);
                        table.append(dropDown.generate());
                    }
                } else if (UIGeneratorConstants.CHECKBOX_FIELD.equals(elementType)) {
                    String name = arg.getFirstChildWithName(
                            new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        optionValue = inner.getText();
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        UIComponent checkBox  = new CheckBox(name, widgetName, optionValue,tooltip,true);
                        table.append(checkBox.generate());
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }

                    } else {
                    	UIComponent checkBox  = new CheckBox(name, widgetName, optionValue,tooltip,false);
                        table.append(checkBox.generate());
                    }
                } else if (UIGeneratorConstants.TEXT_AREA_FIELD.equals(elementType)) {
                    String mandet = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    String richText = arg.getAttributeValue(new QName(null, UIGeneratorConstants.IS_RICH_TEXT));

                    boolean isReadOnly = false;

                    if (markReadonly && "true".equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                        isReadOnly = true;
                    }

                    boolean isRichText = false; //By default rich text is off
                    if (richText != null) {
                        isRichText = Boolean.valueOf(richText);
                    }

                    if (isFilterOperation) {
                        mandet = "false";
                    }
                    if (inner != null) {
                        //if the element contains value is not null get the value
                        value = inner.getText();
                    }
                    int height = -1;
                    int width = -1;
                    String heightString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.HEIGHT_ATTRIBUTE));
                    if (heightString != null) {
                        try {
                            height = Integer.parseInt(heightString);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    String widthString = arg.getAttributeValue(new QName(null, UIGeneratorConstants.WIDTH_ATTRIBUTE));
                    if (widthString != null) {
                        try {
                            width = Integer.parseInt(widthString);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (columns > 2) {
                        if (columnCount == 0) {
                            table.append("<tr>");
                        }
                        UIComponent textArea = new TextArea(null, arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(), null, widgetName, value, height, width, isReadOnly, false, tooltip, true);
                        table.append(textArea.generate());
                       
                        columnCount++;
                        if (columnCount == columns) {
                            table.append("</tr>");
                            columnCount = 0;
                        }
                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }

                        UIComponent textArea = new TextArea(label, name, mandet, widgetName, value,
                     	                                    height, width, isReadOnly, isRichText, tooltip,false);
                        table.append(textArea.generate());
                        
                    }
                } else if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                    if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(maxOccurs)) {
                        // This is the code segment to run in maxoccur unbounded situation
//                        String addedItems = "0";
//                        if(dataHead != null){
//                            addedItems = dataHead.getFirstChildWithName(new QName(null,UIGeneratorConstants.COUNT)).getText();
//                        }
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        if (label == null) {
                            label = name;
                        }
                        boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
                        String urlTemplate = arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                        boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));

                        String startsWith = arg.getAttributeValue(new QName(null,UIGeneratorConstants.PATH_START_WITH));

//                        String addedOptionValues [] = new String[Integer.parseInt(addedItems)];
//                        String addedValues[] = new String[Integer.parseInt(addedItems)];
                        List<String> addedOptionValues = new ArrayList<String>();
                        List<String> addedValues = new ArrayList<String>();
                        int addedItemsCount = 0;
                        if (dataHead != null) {
                            //if the element contains value is not null get the value
                            // with option-text field we put text value like this text_value.replaceAll(" ","-")
                            Iterator itemChildIt = dataHead.getChildElements();
                            int i = 0;
                            while (itemChildIt.hasNext()) {
                                // get all the filled values to the newly added fields
                                Object itemChildObj = itemChildIt.next();
                                if (!(itemChildObj instanceof OMElement)) {
                                    continue;
                                }
                                OMElement itemChildEle = (OMElement) itemChildObj;

                                if (!(itemChildEle.getQName().equals(new QName(dataNamespace,
                                        UIGeneratorConstants.ENTRY_FIELD)))) {
                                    continue;
                                }

                                String entryText = itemChildEle.getText();
                                String entryKey = null;
                                String entryVal;
                                int colonIndex = entryText.indexOf(":");
                                if (colonIndex < entryText.length() - 1) {
                                    entryKey = entryText.substring(0, colonIndex);
                                    entryText = entryText.substring(colonIndex + 1);
                                }
                                entryVal = entryText;

                                if (entryKey != null && !entryKey.equals("")) {
                                    addedOptionValues.add(entryKey);
                                } else {
                                    addedOptionValues.add("0");
                                }

                                if (entryVal != null) {
                                    addedValues.add(entryVal);
                                }

                                i++;
                            }
                            addedItemsCount = i;
                        }
                        /* if there are no added items headings of the table will hide,else display */
                        boolean isDisplay = false;
                        
                        if (addedItemsCount == 0) {
                        	isDisplay = false;                        	
                        } else if (addedItemsCount > 0) {
                        	isDisplay = true;                        	                    
                        }
                        UIComponent addLink =  new AddLink(label, name, UIGeneratorConstants.ADD_ICON_PATH,
                                widgetName, subList.toArray(new String[subList.size() + 1]), isPath, startsWith, isDisplay);
                    	
                        table.append(addLink.generate());  
                        List<String> optionValues = getOptionValues(arg, request, config);
                        if (addedItemsCount > 0) {
                            // This is the place where we fill already added entries
                            for (int i = 0; i < addedItemsCount; i++) {
                                String addedOptionValue = addedOptionValues.get(i);
                                String addedValue = addedValues.get(i);
                                if (addedOptionValue != null && addedValue != null) {
                                	UIComponent optionText = new OptionText(name, (i + 1),null,null,
                                                                           optionValues.toArray(new String[optionValues.size()]),
                                                                           widgetName,
                                                                           addedOptionValue,
                                                                           addedValue,
                                                                           isURL, urlTemplate, isPath, tooltip, startsWith, request);
                                    table.append(optionText.generate());
                                }
                            }
                        }
                        UIComponent closeAddLink = new CloseAddLink(name, addedItemsCount);
                        table.append(closeAddLink.generate()); // add the previously added items and then close the tbody
                    } else {
                        OMElement firstChildWithName = arg.getFirstChildWithName(
                                new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
                        String name = firstChildWithName.getText();
                        String label = firstChildWithName.getAttributeValue(
                                new QName(UIGeneratorConstants.ARGUMENT_LABEL));

                        String startsWith = arg.getAttributeValue(new QName(null,UIGeneratorConstants.PATH_START_WITH));

                        if (label == null) {
                            label = name;
                        }

                        boolean isURL = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_ATTRIBUTE)));
                        String urlTemplate = arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.URL_TEMPLATE_ATTRIBUTE));
                        boolean isPath = Boolean.toString(true).equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.PATH_ATTRIBUTE)));
                        if (dataHead != null) {
                            //if the element contains value is not null get the value
                            // with option-text field we put text value like this text_value.replaceAll(" ","-")

                            inner = GenericUtil.getChildWithName(dataHead, UIGeneratorConstants.TEXT_FIELD +
                                    arg.getFirstChildWithName(
                                            new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(),
                                    dataNamespace);
                            if (inner != null) {
                                value = inner.getText();
                            }
                            OMElement optionValueElement = GenericUtil.getChildWithName(dataHead, arg.getFirstChildWithName
                                    (new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText(),
                                    dataNamespace);
                            if (optionValueElement != null) {
                                optionValue = optionValueElement.getText();
                            }

                        }
                        List<String> optionValues = getOptionValues(arg, request, config);
                        UIComponent optionText = new OptionText(null, 0,label, name,optionValues.toArray(new String[optionValues.size()]),
                                    widgetName, optionValue, value, isURL, urlTemplate, isPath,
                                    tooltip, startsWith, request);
                        table.append(optionText.generate());                        
                    }
                }
            }
        }
        table.append("</table></div>");
        return table.toString();
    }

    public String printMainHeader(String header, int columns) {
        StringBuilder head = new StringBuilder();
        head.append("<thead><tr><th style=\"border-right:0\" colspan=\"" + columns + "\">");
        head.append(header);
        head.append("</th></tr></thead>");
        return head.toString();

    }

    public static String printSubHeaders(String[] headers) {
        StringBuilder subHeaders = new StringBuilder();
        subHeaders.append("<tr>");
        for (String header : headers) {
            subHeaders.append("<td class=\"sub-header\">");
            subHeaders.append((header == null) ? "" : header);
            subHeaders.append("</td>");
        }
        subHeaders.append("<td class=\"sub-header\"></td>");
        subHeaders.append("</tr>");
        return subHeaders.toString();
    }

    public String printCloseAddLink(String name, int count) {
        StringBuilder link = new StringBuilder();
        link.append("</tbody></table>");
        link.append("<input id=\"" + name.replaceAll(" ", "-") + "CountTaker\" type=\"hidden\" value=\"" +
                count + "\" name=\"");
        link.append(name.replaceAll(" ", "-") + UIGeneratorConstants.COUNT + "\"/>\n");

        link.append("</td></tr>");
        return link.toString();
    }

    /* This is the method which extract information from the UI and embedd them to xml using value elements */
    public OMElement getDataFromUI(OMElement head, HttpServletRequest request) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace(dataNamespace, "");
        OMElement data = fac.createOMElement(dataElement, namespace);
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            OMElement widgetData = fac.createOMElement(GenericUtil.getDataElementName(widgetName),
                    namespace);
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            //implement the new way of extracting data if the maxoccurs unbounded happend in option-text field
                            String count = request.getParameter(name.replaceAll(" ", "-") + UIGeneratorConstants.COUNT);

                            for (int i = 0; i < Integer.parseInt(count); i++) {
                                String entryValue = "";
                                String input = request.getParameter(widgetName.replaceAll(" ", "_") +
                                        "_" + name.replaceAll(" ", "-") + (i + 1));
                                if (input != null && !("".equals(input))) {
                                    entryValue += input;
                                }
                                entryValue += ":";
                                String inputTextValue = request.getParameter(widgetName.replaceAll(" ", "_") +
                                        UIGeneratorConstants.TEXT_FIELD +
                                        "_" + name.replaceAll(" ", "-") + (i + 1));
                                if (inputTextValue != null && !("".equals(inputTextValue))) {
                                    entryValue += inputTextValue;
                                }
                                if (!":".equals(entryValue)) {
                                    OMElement entryElement = fac.createOMElement(UIGeneratorConstants.ENTRY_FIELD,
                                            namespace);
                                    entryElement.setText(entryValue);
                                    widgetData.addChild(entryElement);
                                }
                            }

                        }
                        // if maxoccurs unbounded is not mentioned use the default behaviour
                        else {
                            String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                    name.replaceAll(" ", "-"));
                            if (input != null && !("".equals(input))) {
                                OMElement text = fac.createOMElement(GenericUtil.getDataElementName(name),
                                        namespace);
                                text.setText(input);
                                widgetData.addChild(text);
                            }
                            String inputOption = request.getParameter(widgetName.replaceAll(" ", "_") +
                                    UIGeneratorConstants.TEXT_FIELD +
                                    "_" + name.replaceAll(" ", "-"));
                            if (inputOption != null && !("".equals(inputOption))) {
                                OMElement value = fac.createOMElement(
                                        GenericUtil.getDataElementName(UIGeneratorConstants.TEXT_FIELD + name),
                                        namespace);
                                value.setText(inputOption);
                                widgetData.addChild(value);
                            }
                        }
                    } else {
                        String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                name.replaceAll(" ", "-"));
                        OMElement text = null;

                        if (input != null && !("".equals(input))) {
                            text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                            text.setText(input);
                            widgetData.addChild(text);

                        } else {
                            if (name.equals("Name")) {
                                text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                                text.setText(GovernanceConstants.DEFAULT_SERVICE_NAME);
                                widgetData.addChild(text);
                            }
                            if (name.equals("Namespace")) {
                                text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                                text.setText(UIGeneratorConstants.DEFAULT_NAMESPACE);
                                widgetData.addChild(text);
                            }
                        }

                    }
                }
            }
            data.addChild(widgetData);
        }
        return GenericUtil.addExtraElements(data, request);
    }

    public List<Map> getTooltipList(OMElement head) {
        List<Map> res = new ArrayList<Map>();

        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();

                    //check the validation fields and get the id's of them
                    String value = arg.getAttributeValue(new QName(null,
                            UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
                    if (value != null && !"".equals(value)) {
                        if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                            if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                    arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("tooltip", value);
                                map.put("properties", "unbounded");
                                res.add(map);
                            } else {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("tooltip", value);
                                res.add(map);
                            }
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            List ids = new ArrayList<String>();
                            ids.add(widgetName.replaceAll(" ", "_") + "_" + name.replaceAll("" +
                                    " ", "-"));
                            map.put("ids", ids);
                            map.put("tooltip", value);
                            res.add(map);
                        }
                    }
                }
            }
        }
        return res;
    }

    public OMElement getDataFromUIForBasicFilter(OMElement head, HttpServletRequest request) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace(dataNamespace, "");
        OMElement data = fac.createOMElement(dataElement, namespace);
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            OMElement widgetData = fac.createOMElement(GenericUtil.getDataElementName(widgetName),
                                                       namespace);
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            continue;

                        }
                        // if maxoccurs unbounded is not mentioned use the default behaviour
                        else {
                            String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                                                name.replaceAll(" ", "-"));
                            if (input != null && !("".equals(input))) {
                                OMElement text = fac.createOMElement(GenericUtil.getDataElementName(name),
                                                                     namespace);
                                text.setText(input);
                                widgetData.addChild(text);
                            }
                            String inputOption = request.getParameter(widgetName.replaceAll(" ", "_") +
                                                                      UIGeneratorConstants.TEXT_FIELD +
                                                                      "_" + name.replaceAll(" ", "-"));
                            if (inputOption != null && !("".equals(inputOption))) {
                                OMElement value = fac.createOMElement(
                                        GenericUtil.getDataElementName(UIGeneratorConstants.TEXT_FIELD + name),
                                        namespace);
                                value.setText(inputOption);
                                widgetData.addChild(value);
                            }
                        }
                    } else {
                        String input = request.getParameter(widgetName.replaceAll(" ", "_") + "_" +
                                                            name.replaceAll(" ", "-"));
                        OMElement text = null;

                        if (input != null && !("".equals(input))) {
                            text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                            text.setText(input);
                            widgetData.addChild(text);

                        } else {
                            if (name.equals("Name")) {
                                text = fac.createOMElement(GenericUtil.getDataElementName(name), namespace);
                                text.setText(GovernanceConstants.DEFAULT_SERVICE_NAME);
                                widgetData.addChild(text);
                            }
                        }

                    }
                }
            }
            data.addChild(widgetData);
        }
        return GenericUtil.addExtraElements(data, request);
    }

    public List<Map> getValidationAttributes(OMElement head) {
        List<Map> res = new ArrayList<Map>();

        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String elementType = arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE));
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();

                    //check the validation fields and get the id's of them
                    String value = arg.getAttributeValue(new QName(null,
                            UIGeneratorConstants.VALIDATE_ATTRIBUTE));
                    if (value != null && !"".equals(value)) {
                        if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(elementType)) {
                            if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                    arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("name", name);
                                map.put("regexp", value);
                                map.put("properties", "unbounded");
                                res.add(map);
                            } else {
                                Map<String, Object> map = new HashMap<String, Object>();
                                List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ",
                                        "_") + "_" + name.replaceAll("" + " ", "-"));
                                ids.add(widgetName.replaceAll(" ", "_") + UIGeneratorConstants
                                        .TEXT_FIELD + "_" + name.replaceAll("" + " ", "-"));
                                map.put("ids", ids);
                                map.put("name", name);
                                map.put("regexp", value);
                                res.add(map);
                            }
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            List ids = new ArrayList<String>();
                                ids.add(widgetName.replaceAll(" ", "_") + "_" + name.replaceAll("" +
                                    " ", "-"));
                            map.put("ids", ids);
                            map.put("name", name);
                            map.put("regexp", value);
                            res.add(map);
                        }
                    }
                }
            }
        }
        return res;
    }

    

    


    public String[] getMandatoryIdList(OMElement head) {
        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    //check the mandatory fields and get the id's of them
                    String mandatory = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    if (mandatory != null && "true".equals(mandatory)) {
                        id.add("id_" + widgetName.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-"));
                    }
                }
            }
        }
        return id.toArray(new String[id.size()]);
    }

    public String[] getKeyList(OMElement head, String[] keys) {
        List<String> id = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));

        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;

            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    String key = widgetName.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
                        if(Arrays.asList(keys).contains(key.toLowerCase())){
                        id.add(key);
                        }
                }
            }
        }
        return id.toArray(new String[id.size()]);
    }

    public String[] getMandatoryNameList(OMElement head) {
        List<String> name = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    String name_element = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                    String mandatory = arg.getAttributeValue(new QName(null, UIGeneratorConstants.MANDETORY_ATTRIBUTE));
                    if (mandatory != null && "true".equals(mandatory)) {
                        name.add(name_element);
                    }
                }
            }
        }
        return name.toArray(new String[name.size()]);
    }

    public String[] getUnboundedTooltipList(OMElement head) {
        List<String> tooltips = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            String tooltip = arg.getAttributeValue(new QName(null,
                                    UIGeneratorConstants.TOOLTIP_ATTRIBUTE));
                            if (tooltip == null) {
                                tooltip = "";
                            }
                            tooltips.add(tooltip);
                        }
                    }
                }
            }
        }
        return tooltips.toArray(new String[tooltips.size()]);
    }

    public String[] getUnboundedNameList(OMElement head) {
        List<String> name = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            name.add(arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText());
                        }
                    }
                }
            }
        }
        return name.toArray(new String[name.size()]);
    }

    public String[] getUnboundedWidgetList(OMElement head) {
        List<String> widgetList = new ArrayList<String>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the widget names of them
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(arg.getAttributeValue(
                                new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            widgetList.add(widget.getAttributeValue(new QName(null, UIGeneratorConstants.WIDGET_NAME)));
                        }
                    }
                }
            }
        }
        return widgetList.toArray(new String[widgetList.size()]);
    }

    public String[][] getDateIdAndNameList(OMElement head, boolean markReadOnly) {
        List<String[]> result = new ArrayList<String[]>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            String widgetName = widget.getAttributeValue(new QName(null, UIGeneratorConstants.ARGUMENT_NAME));
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    if (UIGeneratorConstants.DATE_FIELD.equals(arg.getAttributeValue(new QName
                            (null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        if (markReadOnly && "true".equals(arg.getAttributeValue(new QName(null,
                                UIGeneratorConstants.READONLY_ATTRIBUTE)))) {
                            continue;
                        }
                        String name = arg.getFirstChildWithName(new QName(null, UIGeneratorConstants.ARGUMENT_NAME)).getText();
                        String[] idAndName = new String[2];
                        idAndName[0] = "id_" + widgetName.replaceAll(" ",
                                "_") + "_" + name.replaceAll(" ", "-");
                        idAndName[1] = name;
                        result.add(idAndName);
                    }
                }
            }
        }
        return result.toArray(new String[result.size()][2]);
    }

    public String[][] getUnboundedValues(OMElement head, HttpServletRequest request,
                                         ServletConfig config) {
        List<String[]> values = new ArrayList<String[]>();
        Iterator it = head.getChildrenWithName(new QName(UIGeneratorConstants.WIDGET_ELEMENT));
        while (it.hasNext()) {
            OMElement widget = (OMElement) it.next();
            Iterator arguments = widget.getChildrenWithLocalName(UIGeneratorConstants.ARGUMENT_ELMENT);
            OMElement arg = null;
            while (arguments.hasNext()) {
                arg = (OMElement) arguments.next();
                if (UIGeneratorConstants.ARGUMENT_ELMENT.equals(arg.getLocalName())) {
                    //check the unbounded fields and get the values of drop-down in option-text type
                    if (UIGeneratorConstants.OPTION_TEXT_FIELD.equals(arg.getAttributeValue(new QName(null, UIGeneratorConstants.TYPE_ATTRIBUTE)))) {
                        //previous check is used to check the max occur unbounded only with option-text fields with other fields it will ignore
                        if (UIGeneratorConstants.MAXOCCUR_UNBOUNDED.equals(
                                arg.getAttributeValue(new QName(null, UIGeneratorConstants.MAXOCCUR_ELEMENT)))) {
                            List<String> inner = getOptionValues(arg, request, config);
                            values.add(inner.toArray(new String[inner.size()]));
                        }
                    }
                }
            }
        }
        return values.toArray(new String[0][0]);
    }

    private List<String> getOptionValues(OMElement arg, HttpServletRequest request,
                                         ServletConfig config) {
        OMElement values = arg.getFirstChildWithName(new QName(null,
                UIGeneratorConstants.OPTION_VALUES));
        Iterator iterator = values.getChildrenWithLocalName(UIGeneratorConstants.OPTION_VALUE);
        List<String> inner = new ArrayList<String>();
        if (iterator != null && iterator.hasNext()) {
            while (iterator.hasNext()) {
                inner.add(((OMElement) iterator.next()).getText());
            }
            return inner;
        } else {
            try {
                String className = values.getAttributeValue(new QName(null,
                        UIGeneratorConstants.OPTION_VALUE_CLASS));
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<?> populatorClass = Class.forName(className, true, loader);

                DropDownDataPopulator populator = (DropDownDataPopulator) populatorClass.newInstance();
                String[] list = populator.getList(request, config);
                return new ArrayList<String>(Arrays.asList(list));
            } catch (ClassNotFoundException e) {
                log.error("Unable to load populator class", e);
            } catch (InstantiationException e) {
                log.error("Unable to load populator class", e);
            } catch (IllegalAccessException e) {
                log.error("Unable to load populator class", e);
            }
        }
        return inner;
    }

}
