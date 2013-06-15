package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator;

public class AddLink extends UIComponent {
	
	private String addIconPath;
	private String[] subList;
	private boolean isPath;
	private String startsWith;
	private boolean isDisplay;

	public AddLink(String label, String name, String id, String addIconPath, String widget, String[] subList, boolean isPath, String startsWith,boolean isDisplay,boolean isJSGenerate) {
	    super(label, name, id, null, widget, false, null, isJSGenerate);
	    this.addIconPath = addIconPath;
	    this.subList = subList;
	    this.isPath = isPath;
	    this.startsWith = startsWith;
	    this.isDisplay = isDisplay;	    
    }

	@Override
	public String generate() {
		
		StringBuilder link = new StringBuilder();
        link.append("<tr><td colspan=\"3\"><a class=\"icon-link\" style=\"background-image: url(");
        link.append(addIconPath);
        link.append(");\" onclick=\"");
        if (startsWith != null) {
        	link.append("add" + name.replaceAll(" ", "") + "_" + widget.replaceAll(" ", "") + "(" + (isPath ? "'path'" : "''") + "," + "'"+startsWith +"'"+  ")\">"); //creating a JavaScript onclick method name which should be identical ex: addEndpoint_Endpoint
        } else {
        	link.append("add" + name.replaceAll(" ", "") + "_" + widget.replaceAll(" ", "") + "(" + (isPath ? "'path'" : "''") + ")\">"); //creating a JavaScript onclick method name which should be identical ex: addEndpoint_Endpoint
        }
        link.append("Add " + label.replaceAll(" ", "-")); //This is the display string for add item ex: Add EndPoint
        link.append("</a></td></tr>");
        link.append("<tr><td colspan=\"3\">");
        link.append("<table class=\"styledLeft\" style=\" "+ (!isDisplay ? "display:none;" : "")+"border: 1px solid rgb(204, 204, 204) ! important;\"><thead>" +
                GenericUIGenerator.printSubHeaders(subList) +
                "</thead><tbody id=\"" + name.replaceAll(" ", "") + "Mgt\">");
        return link.toString();
	}

}
