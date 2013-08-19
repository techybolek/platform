/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.formatter.admin.internal;

/**
 * to store Not deployed event formatter configuration file details (filepath & event formatter name)
 */
public class EventFormatterConfigurationFileDto {

    private String filePath;
    private String eventFormatterName;

    public EventFormatterConfigurationFileDto(String filePath, String eventFormatterName) {
        this.filePath = filePath;
        this.eventFormatterName = eventFormatterName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getEventFormatterName() {
        return eventFormatterName;
    }

    public void setEventFormatterName(String eventFormatterName) {
        this.eventFormatterName = eventFormatterName;
    }
}
