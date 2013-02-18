/**
 * This is a open souce project registered under GPL Licence
 */
package org.wso2.carbon.service.mgt.ui;

import java.util.HashMap;

/**
 * @author geeth
 *
 */
public class ServiceTypeNaming {
    
    private static HashMap map = new HashMap();

    public ServiceTypeNaming() {
        
        if (map.isEmpty()){
            map.put("axis2", "Axis2");
            map.put("data_service", "Data service");
            map.put("js_service", "JS service");
            map.put("sts", "STS");
        }
    }
    
    
    public String convertString(String str) {
        
        if(map.containsKey(str)){
            return map.get(str).toString();
        }

        str = str.replace("_", " ");
        str = toTitleCase(str);
        return str;

    }

    public String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }
}
