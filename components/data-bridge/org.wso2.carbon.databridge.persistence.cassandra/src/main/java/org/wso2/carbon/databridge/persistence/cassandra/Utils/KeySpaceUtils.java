/*
 * Copyright 2013 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.persistence.cassandra.Utils;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.persistence.cassandra.internal.util.ServiceHolder;

import javax.xml.namespace.QName;

/**
 * Date: 4/4/13
 * Time: 12:59 PM
 */
public class KeySpaceUtils {

    private static final Log log = LogFactory.getLog(KeySpaceUtils.class);

    public static final String DEFAULT_KEY_SPACE_NAME = "EVENT_KS";
    private static final String KEY_SPACE_NAME_ELEMENT = "keySpaceName";

    public static String getKeySpaceName() {

        String keySpaceName = DEFAULT_KEY_SPACE_NAME;
        DataBridgeReceiverService dataBridgeReceiverService = ServiceHolder.getDataBridgeReceiverService();

        if (dataBridgeReceiverService != null) {
            OMElement keySpaceNameElement = dataBridgeReceiverService.getInitialConfig().getFirstChildWithName(
                    new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE, KEY_SPACE_NAME_ELEMENT));

            if (keySpaceNameElement != null) {
                keySpaceName = keySpaceNameElement.getText();
            } else {
                log.warn("Unable to find key space name property in data-bridge-config.xml. Hence using default value:" + DEFAULT_KEY_SPACE_NAME);
            }
        }

        return keySpaceName;
    }
}
