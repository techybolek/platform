package org.wso2.carbon.identity.application.authentication.framework;

import java.util.Comparator;

public class ApplicationAuthenticatorsComparator implements Comparator<ApplicationAuthenticator>{

    public int compare(ApplicationAuthenticator o1, ApplicationAuthenticator o2) {
        return o2.getFactor() - o1.getFactor();
    }
}