package org.wso2.carbon.governance.generic.ui.common.dataobjects;

public abstract class UIComponent {
	
	protected String label;
	protected String mandatory;	
	protected String name;
	protected boolean isReadOnly;
	protected String tooltip;	
	protected String widget;
	protected String id;
	protected boolean isJSGenerate;
	
	public UIComponent(String label,String name,String id,String mandatory ,String widget,boolean isReadOnly,String tooltip,boolean isJSGenerate){
		this.label = label;
		this.name = name;
		this.mandatory = mandatory;		
		this.widget = widget;	
		this.isReadOnly = isReadOnly;
		this.tooltip = tooltip;
		this.id = id;
		this.isJSGenerate = isJSGenerate;
	}
	
	public abstract String generate();

}
