package org.wso2.carbon.event.builder.core;

public interface EventListener {
    public void onAddDefinition(Object definition);

    public void onRemoveDefinition(Object definition);
}
