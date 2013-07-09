/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.policy.search;

import java.io.Serializable;

import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;

/**
 * Encapsulate result with time stamp 
 */
public class SearchResult implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8176277439664665138L;

	/**
     * Result
     */
    private EntitledResultSetDTO resultSetDTO;

    /**
     * time stamp
     */
    private long cachedTime;

    public EntitledResultSetDTO getResultSetDTO() {
        return resultSetDTO;
    }

    public void setResultSetDTO(EntitledResultSetDTO resultSetDTO) {
        this.resultSetDTO = resultSetDTO;
    }

    public long getCachedTime() {
        return cachedTime;
    }

    public void setCachedTime(long cachedTime) {
        this.cachedTime = cachedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;

        SearchResult that = (SearchResult) o;

        if (cachedTime != that.cachedTime) return false;
        if (resultSetDTO != null ? !resultSetDTO.equals(that.resultSetDTO) : that.resultSetDTO != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = resultSetDTO != null ? resultSetDTO.hashCode() : 0;
        result = 31 * result + (int) (cachedTime ^ (cachedTime >>> 32));
        return result;
    }
}
