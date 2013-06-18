package org.apache.hadoop.hive.serde2.lazy.objectinspector.primitive;

import org.apache.hadoop.hive.serde2.lazy.CassandraLazyInteger;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class LazyCassandraIntObjectInspector extends LazyIntObjectInspector{

     public LazyCassandraIntObjectInspector(){
      super();
     }

    @Override
  public Object copyObject(Object o) {
      if ( o != null && o instanceof CassandraLazyInteger){
      CassandraLazyInteger org = (CassandraLazyInteger)o;
      CassandraLazyInteger copy = new CassandraLazyInteger
              (new LazyIntObjectInspector());
          copy.setData(org.getData().get());
       return copy;
      }
      return null;
  }

}
