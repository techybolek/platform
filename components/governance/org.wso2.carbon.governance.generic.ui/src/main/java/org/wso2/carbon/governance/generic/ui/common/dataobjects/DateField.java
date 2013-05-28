package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class DateField extends UIComponent {
	
	private String value;
	private boolean isSkipName;	

	public DateField(String label,String name,String mandatory, String widget,String value, boolean isReadOnly,
                     String tooltip, boolean isSkipName) {
	    super(label, name, mandatory, widget, isReadOnly, tooltip);
	    this.value = value;
	    this.isSkipName = isSkipName;
    }

	@Override
    public String generate() {
		StringBuilder element = new StringBuilder();
        String id = "id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-");
        if (value != null) {
        	 value = StringEscapeUtils.escapeHtml(value);
        }
        if (isSkipName) {
        	element.append("</tr>");        	
        }
        element.append("<td class=\"leftCol-big\">" + (label != null ? label : "") );          

        if ("true".equals(mandatory)) {
        	element.append( "<span class=\"required\">*</span>");
        }
        element.append("</td>\n");
       
        

        element.append("<td>");
        if (!isReadOnly) {
            element.append("<a class=\"icon-link\" style=\"background-image: " +
                    "url( ../admin/images/calendar.gif);\" onclick=\"jQuery('#" + id + "')" +
                    ".datepicker( 'show' );\" href=\"javascript:void(0)\"></a>");

        }
        element.append("<input type=\"text\" name=\"" + widget.replaceAll(" ",
                "_") + "_" + name.replaceAll(" ", "-")
                    + "\" title=\"" + tooltip + "\" style=\"width:" + UIGeneratorConstants
                            .DATE_WIDTH + "px\"" + (isReadOnly ? " readonly" : "") + " id=\"" + id 
                    + "\" value=\"" +  (value != null ? value : "") + "\" />"
                    + "</td>");
        if (isSkipName){
        	element.append("</tr>");
        }
        return element.toString();
    }

}
