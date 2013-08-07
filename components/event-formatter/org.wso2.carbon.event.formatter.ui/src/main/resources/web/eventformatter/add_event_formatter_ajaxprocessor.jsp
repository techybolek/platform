<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventOutputPropertyConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%

    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);

    String eventFormatterName = request.getParameter("eventFormatter");
    String msg = null;
    String streamNameWithVersion = request.getParameter("streamNameWithVersion");
    String transportAdaptorName = request.getParameter("transportAdaptorName");
    String outputParameterSet = request.getParameter("outputParameters");
    String mappingType = request.getParameter("mappingType");

    EventFormatterPropertyDto[] eventFormatterProperties = null;

    if (outputParameterSet != null) {
        String[] properties = outputParameterSet.split("\\|");
        if (properties != null) {
            // construct property array for each property
            eventFormatterProperties = new EventFormatterPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$");
                if (propertyNameAndValue != null) {
                    eventFormatterProperties[index] = new EventFormatterPropertyDto();
                    eventFormatterProperties[index].setKey(propertyNameAndValue[0].trim());
                    eventFormatterProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }

    if (mappingType.equals("wso2event")) {

        String metaDataSet = request.getParameter("metaData");
        EventOutputPropertyConfigurationDto[] metaWSO2EventConfiguration = null;

        if (metaDataSet != null) {
            String[] properties = metaDataSet.split("\\$");
            if (properties != null) {
                // construct property array for each property
                metaWSO2EventConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^");
                    if (propertyConfiguration != null) {
                        metaWSO2EventConfiguration[index] = new EventOutputPropertyConfigurationDto();
                        metaWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                        metaWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                        metaWSO2EventConfiguration[index].setType(propertyConfiguration[2].trim());
                        index++;
                    }
                }

            }
        }

        String correlationDataSet = request.getParameter("correlationData");
        EventOutputPropertyConfigurationDto[] correlationWSO2EventConfiguration = null;

        if (correlationDataSet != null) {
            String[] properties = correlationDataSet.split("\\$");
            if (properties != null) {
                // construct property array for each property
                correlationWSO2EventConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^");
                    if (propertyConfiguration != null) {
                        correlationWSO2EventConfiguration[index] = new EventOutputPropertyConfigurationDto();
                        correlationWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                        correlationWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                        correlationWSO2EventConfiguration[index].setType(propertyConfiguration[2].trim());
                        index++;
                    }
                }

            }
        }

        String payloadDataSet = request.getParameter("payloadData");
        EventOutputPropertyConfigurationDto[] payloadWSO2EventConfiguration = null;

        if (payloadDataSet != null) {
            String[] properties = payloadDataSet.split("\\$");
            if (properties != null) {
                // construct property array for each property
                payloadWSO2EventConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^");
                    if (propertyConfiguration != null) {
                        payloadWSO2EventConfiguration[index] = new EventOutputPropertyConfigurationDto();
                        payloadWSO2EventConfiguration[index].setName(propertyConfiguration[0].trim());
                        payloadWSO2EventConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                        payloadWSO2EventConfiguration[index].setType(propertyConfiguration[2].trim());
                        index++;
                    }
                }

            }
        }


        try {
            // add transport adaptor via admin service
            stub.addWSO2EventFormatter(eventFormatterName, streamNameWithVersion, transportAdaptorName, metaWSO2EventConfiguration, correlationWSO2EventConfiguration, payloadWSO2EventConfiguration, eventFormatterProperties);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

%>

<script type="text/javascript">
    alert(msg);
</script>
<%

    }
} else if (mappingType.equals("text")) {
    String dataSet = request.getParameter("textData");
    String dataFrom = request.getParameter("dataFrom");

    try {
        // add transport adaptor via admin service
        stub.addTextEventFormatter(eventFormatterName, streamNameWithVersion, transportAdaptorName, dataSet, eventFormatterProperties, dataFrom);
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

%>

<script type="text/javascript">
    alert(msg);
</script>
<%

    }
} else if (mappingType.equals("xml")) {
    String dataSet = request.getParameter("textData");
    String dataFrom = request.getParameter("dataFrom");

    try {
        // add transport adaptor via admin service
        stub.addXmlEventFormatter(eventFormatterName, streamNameWithVersion, transportAdaptorName, dataSet, eventFormatterProperties, dataFrom);
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

%>

<script type="text/javascript">
    alert(msg);
</script>
<%

    }
} else if (mappingType.equals("map")) {

    String mapDataSet = request.getParameter("mapData");
    EventOutputPropertyConfigurationDto[] eventOutputPropertyConfiguration = null;

    if (mapDataSet != null) {
        String[] properties = mapDataSet.split("\\$");
        if (properties != null) {
            // construct property array for each property
            eventOutputPropertyConfiguration = new EventOutputPropertyConfigurationDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyConfiguration = property.split("\\^");
                if (propertyConfiguration != null) {
                    eventOutputPropertyConfiguration[index] = new EventOutputPropertyConfigurationDto();
                    eventOutputPropertyConfiguration[index].setName(propertyConfiguration[0].trim());
                    eventOutputPropertyConfiguration[index].setValueOf(propertyConfiguration[1].trim());
                    index++;
                }
            }

        }
    }

    try {
        // add transport adaptor via admin service
        stub.addMapEventFormatter(eventFormatterName, streamNameWithVersion, transportAdaptorName, eventOutputPropertyConfiguration, eventFormatterProperties);
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

%>

<script type="text/javascript">
    alert(msg);
</script>
<%

    }
} else if (mappingType.equals("json")) {
    String dataSet = request.getParameter("jsonData");
    String dataFrom = request.getParameter("dataFrom");

    try {
        // add transport adaptor via admin service
        stub.addJsonEventFormatter(eventFormatterName, streamNameWithVersion, transportAdaptorName, dataSet, eventFormatterProperties, dataFrom);
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

%>

<script type="text/javascript">
    alert(msg);
</script>
<%

        }
    }


%>  <%=msg%>   <%

%>
