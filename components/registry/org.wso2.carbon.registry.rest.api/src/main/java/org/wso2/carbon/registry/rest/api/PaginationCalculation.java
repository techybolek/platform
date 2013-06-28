
/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.rest.api;

import javax.ws.rs.core.Response; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PaginationCalculation<E> extends RestSuper {
	
	protected Log log = LogFactory.getLog(PaginationCalculation.class);

	protected abstract E[] getResult();
	protected abstract Response display(E[] e,int begin,int end);	
	public Response displayPaginatedResult(int start, int size){
		E[] result = getResult();
		if(result != null){
			calculatePaginationIndexValues(start, size, result.length);
			return display(result,begin,end);
		}else{
			return null;
		}	
	}
	
	/**
	 * This method calculates the begin and end indexes for the traversal on the respective array
	 * @param start starting page number
	 * @param size number of records to be fetched
	 * @param total total number of records in the relevant array
	 */
	public void calculatePaginationIndexValues(int start,int size,int total ){
		//if start = 0 and size = 0 means retrieves all the tags on the requested resource
		if(start == 0 && size == 0){
			begin = 0;
			end = total - 1;	
		} else{
			begin = (start - 1) * pageSize;
			if(begin < 0 || begin > total - 1){
				end = -1;
			} else{
				if((begin + size - 1) > total - 1){
					end = total - 1;
				} else{
					end = begin + size - 1;
				}
			}
		}
	}
}
