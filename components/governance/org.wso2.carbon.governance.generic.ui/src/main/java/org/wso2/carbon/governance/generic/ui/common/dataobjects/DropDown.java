package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;

public class DropDown extends UIComponent {
	
	private String[] values;
	private String value;

	public DropDown(String label, String name, String mandatory, String[] values,
                                String widget, String value, String tooltip) {
	    super(label, name, mandatory, widget, false, tooltip);
	    this.values = values;
		this.value = value;
    }

	@Override
	public String generate() {
		StringBuilder dropDown = new StringBuilder();
        if ("true".equals(mandatory)) {
            dropDown.append((label != null ? "<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n" : "") +
                    "<td><select id=\"id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " +
                    "name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ",
                    "-") + "\" title=\"" + tooltip + "\">");
        } else {
            dropDown.append((label != null ? "<tr><td class=\"leftCol-big\">" + label + "</td>\n" :"")+
                    "<td><select id=\"id_" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ", "-") + "\" " +
                    "name=\"" + widget.replaceAll(" ", "_") + "_" + name.replaceAll(" ",
                    "-") + "\" title=\"" + tooltip + "\">");
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
