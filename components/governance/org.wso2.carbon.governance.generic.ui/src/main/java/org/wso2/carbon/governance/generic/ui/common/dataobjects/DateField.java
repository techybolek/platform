package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class DateField extends UIComponent {
	
	private String value;
	private boolean isSkipName;	

	public DateField(String label, String name, String id, String mandatory, String widget,String value, boolean isReadOnly,
                     String tooltip, boolean isSkipName,boolean isJSGenerate) {
		super(label, name, id, mandatory, widget, isReadOnly, tooltip, isJSGenerate);
		this.value = value;
		this.isSkipName = isSkipName;
    }

	@Override
    public String generate() {
		StringBuilder element = new StringBuilder();
		String id;		
		if (this.id == null) {
			id = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
		} else {
			id = this.id;
		}
        if (value != null) {
        	 value = StringEscapeUtils.escapeHtml(value);
        }
        if (isSkipName) {
        	element.append("</tr>");        	
        }
        if (label != null ){
        	element.append("<td class=\"leftCol-big\">" + (label != null ? label : "") );      
        }

        if ("true".equals(mandatory)) {
        	element.append( "<span class=\"required\">*</span>");
        }
        if (label != null ) {
        	element.append("</td>");
        }
       
        

        element.append("<td>");
        if (!isReadOnly) {
            element.append("<a class=\"icon-link\" style=\"background-image: " +
                    "url( ../admin/images/calendar.gif);\" "+(isJSGenerate? "onclick=\"jQuery(\\'#" + id + "\\').datepicker(\\'show\\');\"" :"onclick=\"jQuery(\'#" + id + "\').datepicker(\'show\');\"")+
                    " href=\"javascript:void(0)\"></a>");

        }
        element.append("<input type=\"text\" name=\"" + widget.replaceAll(" ",
                "") + "_" + name.replaceAll(" ", "")
                    + "\" title=\"" + tooltip + "\" style=\"width:" + UIGeneratorConstants
                            .DATE_WIDTH + "px\"" + (isReadOnly ? " readonly" : "") + " id=\"" + id 
                    + "\" value=\"" +  (value != null ? value : "") + "\" />"
                    + "</td>");
        if (isSkipName) {
        	element.append("</tr>");
        }
        return element.toString();
    }

}
