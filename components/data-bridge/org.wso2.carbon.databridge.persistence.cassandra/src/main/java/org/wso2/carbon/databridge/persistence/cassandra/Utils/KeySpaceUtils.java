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
/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.persistence.cassandra.internal.util.ServiceHolder;

import javax.xml.namespace.QName;


public class KeySpaceUtils {

    private static final Log log = LogFactory.getLog(KeySpaceUtils.class);

    public static final String DEFAULT_KEY_SPACE_NAME = "EVENT_KS";
    public static final String DEFAULT_INDEX_KEYSPACE_NAME = "EVENT_INDEX_KS";
    private static final String KEY_SPACE_NAME_ELEMENT = "keySpaceName";
    private static final String INDEX_KEY_SPACE_NAME_ELEMENT = "eventIndexKeySpaceName";

    private static String keySpaceName;
    private static String indexKeySpaceName;

    public static String getKeySpaceName() {
        if (null == keySpaceName) {
            keySpaceName = DEFAULT_KEY_SPACE_NAME;

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
        }
        return keySpaceName;
    }

    public static String getIndexKeySpaceName() {
        if (null == indexKeySpaceName) {
            indexKeySpaceName = DEFAULT_INDEX_KEYSPACE_NAME;
            DataBridgeReceiverService dataBridgeReceiverService = ServiceHolder.getDataBridgeReceiverService();

            if (dataBridgeReceiverService != null) {
                OMElement indexKeySpaceNameElement = dataBridgeReceiverService.getInitialConfig().getFirstChildWithName(
                        new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE, INDEX_KEY_SPACE_NAME_ELEMENT));

                if (indexKeySpaceNameElement != null) {
                    indexKeySpaceName = indexKeySpaceNameElement.getText();
                } else {
                    log.warn("Unable to find index key space name property in data-bridge-config.xml. " +
                            "Hence using default value:" + DEFAULT_INDEX_KEYSPACE_NAME);
                }
            }
        }

        return indexKeySpaceName;
    }
}
