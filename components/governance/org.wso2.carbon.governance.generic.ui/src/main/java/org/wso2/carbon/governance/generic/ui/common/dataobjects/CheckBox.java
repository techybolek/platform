package org.wso2.carbon.governance.generic.ui.common.dataobjects;

public class CheckBox extends UIComponent {
	
	private String value;
	private boolean isSkipName;
	

	public CheckBox(String name, String id, String widget, String value, String tooltip, boolean isSkipName, boolean isJSGenerate) {
		super(null, name, id, null, widget, false, tooltip, isJSGenerate);
		this.value = value;
		this.isSkipName = isSkipName;
	}

	@Override
	public String generate() {
		if (Boolean.toString(true).equals(value)) {
            return (isSkipName ? "<tr><td class=\"leftCol-big\">" + name + "</td>\n" : "")+
                    "<td><input type=\"checkbox\" checked=\"checked\" name=\"" +
                    widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "") +
                    "\" value=\"true\" title=\"" + tooltip + "\" /></td>";
        } else {
            return (isSkipName ? "<tr><td class=\"leftCol-big\">" + name + "</td>\n" : "")+
                    "<td><input type=\"checkbox\" name=\"" + widget.replaceAll(" ", "") + "_" +
                    name.replaceAll(" ", "") + "\" value=\"true\" title=\"" + tooltip +
                    "\"/></td>";
        }
	}

}
