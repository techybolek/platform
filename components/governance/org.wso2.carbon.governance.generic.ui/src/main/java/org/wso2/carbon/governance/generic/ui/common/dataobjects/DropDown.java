package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;

public class DropDown extends UIComponent {
	
	private String[] values;
	private String value;

	public DropDown(String label, String name,String id, String mandatory, String[] values,
                                String widget, String value, String tooltip,boolean isJSGenerate) {
	    super(label, name, id, mandatory, widget, false, tooltip, isJSGenerate);
	    this.values = values;
		this.value = value;
    }

	@Override
	public String generate() {
		String id;		
		if (this.id == null) {
			id = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
		} else {
			id = this.id;
		}
		
		StringBuilder dropDown = new StringBuilder();
        if ("true".equals(mandatory)) {
            dropDown.append((label != null ? "<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>" : "") +
                    "<td><select id=\"" + id + "\" " +
                    "name=\"" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ",
                    "") + "\" title=\"" + tooltip + "\">");
        } else {
            dropDown.append((label != null ? "<tr><td class=\"leftCol-big\">" + label + "</td>" :"")+
                    "<td><select id=\"" + id + "\" " +
                    "name=\"" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ",
                    "") + "\" title=\"" + tooltip + "\">");
        }

        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + StringEscapeUtils.escapeHtml(values[i])
                     + "\"");
            if (value != null && values[i].equals(value)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(StringEscapeUtils.escapeHtml(values[i]));
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        if (label != null) {
        	 dropDown.append("</tr>");
        }
        return dropDown.toString();
	}

}
