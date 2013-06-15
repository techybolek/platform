package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class CloseAddLink extends UIComponent {
	
	private int count;

	public CloseAddLink(String name, int count, boolean isJSGenerate) {
		super(null, name, null, null, null, false, null, isJSGenerate);
		this.count = count;
	}

	@Override
	public String generate() {
		StringBuilder link = new StringBuilder();
        link.append("</tbody></table>");
        link.append("<input id=\"" + name.replaceAll(" ", "") + "CountTaker\" type=\"hidden\" value=\"" +
                count + "\" name=\"");
        link.append(name.replaceAll(" ", "") + UIGeneratorConstants.COUNT + "\"/>\n");

        link.append("</td></tr>");
        return link.toString();
	}

}
