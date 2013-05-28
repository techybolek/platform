package org.wso2.carbon.governance.generic.ui.common.dataobjects;

public abstract class UIComponent {
	
	protected String label;
	protected String mandatory;	
	protected String name;
	protected boolean isReadOnly;
	protected String tooltip;	
	protected String widget;
	
	public UIComponent(String label,String name,String mandatory ,String widget,boolean isReadOnly,String tooltip){
		this.label = label;
		this.name = name;
		this.mandatory = mandatory;		
		this.widget = widget;	
		this.isReadOnly = isReadOnly;
		this.tooltip = tooltip;
	}
	
	public abstract String generate();

}
