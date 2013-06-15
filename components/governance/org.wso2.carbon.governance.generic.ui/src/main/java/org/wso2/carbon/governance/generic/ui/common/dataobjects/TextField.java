package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.ui.CarbonUIUtil;

public class TextField extends UIComponent {
	
	private String value;
	private boolean isURL;
	private String urlTemplate;
	private String startsWith;	
	private boolean hasValue;	
	protected boolean isPath;
	private HttpServletRequest request;
	
	public TextField(String label, String name,String id, String mandatory, String widget,
                           String value, boolean isURL, String urlTemplate, boolean isPath,
                           boolean isReadOnly, boolean hasValue, String tooltip, String startsWith,
                           HttpServletRequest request,boolean isJSGenerate){
		super(label, name,id, mandatory, widget,isReadOnly,tooltip,isJSGenerate);		
		this.value = value;
		this.isURL = isURL;
		this.urlTemplate = urlTemplate;		
		this.hasValue = hasValue;		
		this.startsWith = startsWith;
		this.isPath = isPath;
		this.request = request;		
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

        String selectResource = "";
        String selectResourceButton = "$('" + id + "_button').style.display='';";
        if (value != null) {
        	value = StringEscapeUtils.escapeHtml(value);
        }
        
        if (isPath) {
        	if (startsWith != null ) {
        		selectResource = " <input id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) + "\"   "+ (isJSGenerate ? "onclick=\"showGovernanceResourceTreeWithCustomPath(\\'" + id + "\\',\\'" + startsWith + "\\')" :"onclick=\"showGovernanceResourceTreeWithCustomPath('" + id + "','" + startsWith + "')")+";\"/>";
        	} else {
        		selectResource = " <input id=\"" + id + "_button\" type=\"button\" class=\"button\" value=\"..\" title=\"" + CarbonUIUtil.geti18nString("select.path",
                        "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) + "\" "+(isJSGenerate ? "onclick=\"showGovernanceResourceTree(\\'" + id + "\\')" :"onclick=\"showGovernanceResourceTree(\'" + id + "\')")+";\"/>";
        	}            
        }

        String browsePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH;

        String div = null;
        if (isURL && value != null) {
        	if (isJSGenerate) {
        		div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + browsePath : "") + (urlTemplate != null ? urlTemplate.replace("@{value}", value) : value) + "\">" + value + "</a>" +
                        "&nbsp;" + (!isReadOnly ? "<a onclick=\"$(\\'" + id + "_link\\').style.display=\\'none\\';$(\\'" + id +
                        "\\')." +
                        "style.display=\\'\\';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                        "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) +
                        "\" " +
                        "class=\"icon-link\" style=\"background-image: url(\\'../admin/images/edit.gif\\');float: none\"></a>" : "") + "</div>";
        	} else {
        		div = "<div id=\"" + id + "_link\"><a target=\"_blank\" href=\"" + (isPath ? "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + browsePath : "") + (urlTemplate != null ? urlTemplate.replace("@{value}", value) : value) + "\">" + value + "</a>" +
                        "&nbsp;" + (!isReadOnly ? "<a onclick=\"$('" + id + "_link').style.display='none';$('" + id +
                        "')." +
                        "style.display='';" + (isPath ? selectResourceButton : "") + "\" title=\"" + CarbonUIUtil.geti18nString("edit",
                        "org.wso2.carbon.governance.generic.ui.i18n.Resources", request.getLocale()) +
                        "\" " +
                        "class=\"icon-link\" style=\"background-image: url('../admin/images/edit.gif');float: none\"></a>" : "") + "</div>";
        	} 
        }
       
        //+ (hasValue ? "value=\"" + value + "\"" : "") +
        if ("true".equals(mandatory)) {
        	if (label != null) {
        		 element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>\n");
        	}        	
            element.append(" <td>" + (isURL && div != null ? div : "") + "<input" + (isURL && hasValue && value != null ? " style=\"display:none\"" : "") + " type=\"text\" name=\"" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "")
                    + "\" title=\"" + tooltip + "\" " + (hasValue && value != null ?  "value=\"" + value + "\"" :
                    "") + " id=\"" + id + "\" " +
                    "style=\"width:200px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td>");
            if (label != null) {
       		 	element.append("</tr>");
        	}
        } else {
        	if (label != null) {
       		 	element.append("<tr><td class=\"leftCol-big\">" + label + "</td>\n");
        	} 
            element.append(" <td>" + (isURL && div != null ? div : "") + "<input" + (isURL && hasValue && value != null ? " style=\"display:none\"" : "") + " type=\"text\" name=\"" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "")
                    + "\"  title=\"" + tooltip + "\" "  + (hasValue && value != null ? "value=\"" + value + "\"" :
                    "") + " id=\"" + id + "\" " +
                    "style=\"width:200px\"" + (isReadOnly ? " readonly" : "") + "/>" + (isPath ? selectResource : "") + "</td>");
            if (label != null) {
       		 	element.append("</tr>");
        	} 
        }
        return element.toString();
	}

}
