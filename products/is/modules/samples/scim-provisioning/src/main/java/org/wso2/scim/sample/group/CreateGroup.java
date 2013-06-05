/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.scim.sample.group;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.scim.sample.utils.SCIMSamplesUtils;

import java.io.IOException;

public class CreateGroup {
    //user details
    private static String displayName = "eng";
    private static final String externalID = "eng";

    /*add SCIM IDs of actual users in the Identity Server in order to add members to the group*/
    private static final String[] members = {"1c93ded4-a142-4872-9be3-be03a09918b9",
                                             "1a0b742d-0f7e-4c86-b680-fd818553a87a"};

    public static void main(String[] args) {

        try {
            //load sample configuration
            SCIMSamplesUtils.loadConfiguration();
            //set the keystore
            SCIMSamplesUtils.setKeyStore();
            //create SCIM client
            SCIMClient scimClient = new SCIMClient();
            //create a group according to SCIM Group Schema
            Group scimGroup = scimClient.createGroup();
            scimGroup.setExternalId(externalID);
            scimGroup.setDisplayName(displayName);
            /************Uncomment the following if you want to add members to group*************/
            //set group members
            /*for (String member : members) {
                scimGroup.setMember(member);
            }*/
            //encode the group in JSON format

            String encodedGroup = scimClient.encodeSCIMObject(scimGroup, SCIMConstants.JSON);

            System.out.println("");
            System.out.println("");
            System.out.println("/******Group to be created in json format: " + encodedGroup + "******/");
            System.out.println("");


            PostMethod postMethod = new PostMethod(SCIMSamplesUtils.groupEndpointURL);
            //add basic auth header
            postMethod.addRequestHeader(SCIMConstants.AUTHORIZATION_HEADER,
                                        SCIMSamplesUtils.getBase64EncodedBasicAuthHeader(
                                                SCIMSamplesUtils.userName,
                                                SCIMSamplesUtils.password));
            //create request entity with the payload.
            RequestEntity requestEntity = new StringRequestEntity(encodedGroup, SCIMSamplesUtils.CONTENT_TYPE, null);
            postMethod.setRequestEntity(requestEntity);

            //create http client
            HttpClient httpClient = new HttpClient();
            //send the request
            int responseStatus = httpClient.executeMethod(postMethod);

            String response = postMethod.getResponseBodyAsString();

            System.out.println("");
            System.out.println("");
            System.out.println("/******SCIM group creation response status: " + responseStatus);
            System.out.println("SCIM group creation response data: " + response + "******/");
            System.out.println("");


        } catch (CharonException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
