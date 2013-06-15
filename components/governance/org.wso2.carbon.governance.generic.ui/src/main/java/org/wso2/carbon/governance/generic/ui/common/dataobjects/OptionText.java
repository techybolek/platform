package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.CarbonUIUtil;

public class OptionText extends UIComponent {
	
	private String originalName;
	private int index;
	private String[] values;
	private String option;
	private String text;
	private boolean isURL;
	private String urlTemplate;
	private boolean isPath;
	private String startsWith;
	private HttpServletRequest request;
	
	public OptionText(String originalName, int index,String label, String name, String id,  String[] values, String widget,
                      String option, String text, boolean isURL, String urlTemplate,
                      boolean isPath, String tooltip, String startsWith, HttpServletRequest request,boolean isJSGenerate) {
		super(label, name, id, null, widget, false, tooltip, isJSGenerate);
		this.originalName = originalName;
		this.index = index;
		this.values = values;
		this.option = option;
		this.text = text;
		this.isURL = isURL;
		this.urlTemplate = urlTemplate;
		this.isPath = isPath;
		this.startsWith = startsWith;
		this.request = request;
    }

	@Override
	public String generate() {
		
		if (name == null) {
			name = originalName + index;
		}
		StringBuilder dropDown = new StringBuilder();
        dropDown.append("<tr><td class=\"leftCol\"><select name=\"" + widget.replaceAll(" ",
                "") + "_" + name.replaceAll(" ", "") + "\" title=\"" + tooltip + "\">");
        String id;		
		if (this.id == null) {
			id = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
		} else {
			id = this.id;
		}
        for (int i = 0; i < values.length; i++) {
            dropDown.append("<option value=\"" + StringEscapeUtils.escapeHtml(values[i]) +
                    "\"");
            if (option != null && values[i].equals(option)) {
                dropDown.append(" selected>");
            } else {
                dropDown.append(">");
            }
            dropDown.append(StringEscapeUtils.escapeHtml(values[i]));
            dropDown.append("</option>");
        }
        dropDown.append("</select></td>");
        if (isURL && text != null) {
            String selectResource = "";
            String selectResourceButton = "$('" + id + "_button').style.display='';";
            if (isPath) {
            	if (startsWith != null) {
            		selectResource = " <input style=\"display:none\" id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                            "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTreeWithCustomPath('" + id + "','" + startsWith + "');\"/>";
            	} else {
            		selectResource = " <input style=\"display:none\" id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                            "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            	}                
            }

            String browsePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH;            

            String div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + browsePath : "") +
                    StringEscapeUtils.escapeHtml((urlTemplate != null ? urlTemplate.replace("@{value}", text) : text))
                    + "\">" + StringEscapeUtils.escapeHtml(text) + "</a>" +
                    "&nbsp;<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                    "')." +
                    "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                    "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) +
                    "\" " +
                    "class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a></div>";
            dropDown.append("<td>" + div + "<input style=\"display:none\" type=\"text\" name=\"" + widget.replaceAll(" ", "") + UIGeneratorConstants.TEXT_FIELD
                    + "_" + name.replaceAll(" ", "") + "\" title=\"" + tooltip + "\" value=\"" +
                    StringEscapeUtils.escapeHtml(text)
                    + "\" id=\""
                    + id +"\" style=\"width:400px\"/>" + (isPath ? selectResource : "") + "</td>");
        } else {
            String selectResource = "";
            if (isPath) {
                selectResource = " <input type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) + "\" onclick=\"showGovernanceResourceTree('" + id + "');\"/>";
            }
            dropDown.append("<td width=500px><input type=\"text\" name=\"" + widget.replaceAll(" ", "") + UIGeneratorConstants.TEXT_FIELD
                    + "_" + name.replaceAll(" ", "") + "\"  title=\"" + tooltip + "\" "+(text != null ? " value=\"" +StringEscapeUtils.escapeHtml(text) : "") + "\" id=\"" + id +
                    "\" style=\"width:400px\"/>" + (isPath ? selectResource : "") + "</td>");
        }
        if (originalName != null && widget != null) {
        	dropDown.append("<td><a class=\"icon-link\" title=\"delete\" onclick=\"" + "delete" + originalName.replaceAll(" ", "") + "_" + widget.replaceAll(" ", "") + "(this.parentNode.parentNode.rowIndex)\" style=\"background-image:url(../admin/images/delete.gif);\">Delete</a></td>");
        }
        dropDown.append("</tr>");
        return dropDown.toString();
	}

}
