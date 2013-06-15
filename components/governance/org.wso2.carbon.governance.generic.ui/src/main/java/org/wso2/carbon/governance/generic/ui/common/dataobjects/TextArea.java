package org.wso2.carbon.governance.generic.ui.common.dataobjects;

import org.apache.commons.lang.StringEscapeUtils;
import org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants;

public class TextArea extends UIComponent {

	private String value;
	private int height;
	private int width;
	private boolean isRichText;
	private boolean isSkipName;
	
	public TextArea(String label, String name,String id, String mandatory, String widget,
                    String value, int height, int width, boolean isReadOnly,
                    boolean isRichText, String tooltip,boolean isSkipName,boolean isJSGenerate){
		super(label, name, id, mandatory, widget, isReadOnly, tooltip, isJSGenerate);
		this.value = value;
		this.height = height;
		this.width = width;
		this.isRichText = isRichText;
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
		
        StringBuilder size = new StringBuilder("style=\"");
        value = StringEscapeUtils.escapeHtml(value);
        if (height > 0) {
            size.append("height:").append(height).append("px;");
        }
        if (width > 0) {
            size.append("width:").append(width).append("px\"");
        } else {
            size.append("width:").append(UIGeneratorConstants.DEFAULT_WIDTH).append("px\"");
        }
        if (isSkipName) {
        	 element.append("<td><textarea  name=\"" + widget.replaceAll(" ","") + "_" + name.replaceAll(" ", "") + "\" title=\"" + tooltip + "\" " +
                     "id=\"id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ",
                     "") + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + (value != null ? StringEscapeUtils.escapeHtml(value): "" )+"</textarea></td>");
              return element.toString();
        }
        
        if ("true".equals(mandatory)) {
            if (isRichText) {
                element.append("<td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>" +
                        " <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\""
                        + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ",
                        "") + "\" title=\"" + tooltip + "\" id=\""
                        + id + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + (value != null ? value : "") + "</textarea>");
                element = appendRichTextScript(element, width, height, widget, name);
                element.append("</td></tr>");

            } else {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>" +
                        " <td><textarea  name=\"" + widget.replaceAll(" ",
                        "") + "_" + name.replaceAll(" ", "") + "\" title=\"" + tooltip + "\" id=\""
                        + id + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + (value != null ? value : "")  + "</textarea>");
                //element = appendEmptyScript(element, widget, name);
                element.append("</td></tr>");

            }
        } else {
            if (isRichText) {
                element.append("<tr><td class=\"leftCol-big\">" + label + "<span class=\"required\">*</span></td>" +
                        " <td  style=\"font-size:8px\" class=\"yui-skin-sam\"><textarea  name=\""
                        + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ",
                        "") + "\" title=\"" + tooltip + "\" id=\""
                        + id + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + (value != null ? value : "")  + "</textarea>");
                element = appendRichTextScript(element, width, height, widget, name);
                element.append("</td></tr>");

            } else {
            	if (label != null) {
            		element.append("<tr><td class=\"leftCol-big\">" + label + "</td>" );
            	}
            	
            	element.append("<td><textarea  name=\"" + widget.replaceAll(" ",
                        "") + "_" + name.replaceAll(" ", "") + "\" title=\"" + tooltip + "\" id=\""
                        + id + "\" " + size  + (isReadOnly ? " readonly" : "") + " >" + (value != null ? value : "")  + "</textarea>");
                //element = appendEmptyScript(element, widget, name);
            	if (label != null) {
            		element.append("</td></tr>");
            	}

            }
        }
        
        return element.toString();
    }
	
	private StringBuilder appendRichTextScript(StringBuilder element, int width, int height, String widget, String name) {
        String attrName = widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
        String eleName = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
        String ele_id = "_id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
        String fun_name = "set_" + eleName;
        String richTextAttrName = "yui_txt_" + eleName;
        element.append("<script>\n" +
                "\n" + "var " + richTextAttrName + ";\n" +
                "(function() {\n" +
                "    var Dom = YAHOO.util.Dom,\n" +
                "        Event = YAHOO.util.Event;\n" +
                "    \n" +
                "    var myConfig = {\n" +
                "        height: '" + "120" + "px',\n" +
                "        width: '" + "400" + "px',\n" +
                "        dompath: true,\n" +
                "        focusAtStart: true\n" +
                "    };\n" +
                "\n" +
                "    YAHOO.log('Create the Editor..', 'info', 'example');\n" +
                "    " + richTextAttrName + " = new YAHOO.widget.SimpleEditor('" + eleName + "', myConfig);\n" +
                "    " + richTextAttrName + ".render();\n" +
                "\n" +
                "})();\n");

        element.append("function " + fun_name + "(){\n" +
                "        var form1 = document.getElementById('CustomUIForm');\n" +
                "        var newInput = document.createElement('input');\n" +
                "        newInput.setAttribute('type','hidden');\n" +
                "        newInput.setAttribute('name','" + attrName + "');\n" +
                "        newInput.setAttribute('id','" + ele_id + "');\n" +
                "        form1.appendChild(newInput);" +

                "    var contentText=\"\";\n" +
                "    " + richTextAttrName + ".saveHTML();\n" +
                "    contentText = " + richTextAttrName + ".get('textarea').value;\n" +
                "    document.getElementById(\"" + ele_id + "\").value = contentText;\n" +
                "}");

        element.append("</script>");

        return element;
    }
	
	private StringBuilder appendEmptyScript(StringBuilder element, String widget, String name) {
	        //Create a empty JS function to avoid errors in rich text false state;
	        String eleName = "id_" + widget.replaceAll(" ", "") + "_" + name.replaceAll(" ", "");
	        String fun_name = "set_" + eleName;
	        element.append("<script>\n");
	        element.append("function " + fun_name + "(){}");
	        element.append("</script>");
	        return element;
	}


}
