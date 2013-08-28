/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.objectmodel.context;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for FieldContextPath.
 */
public class FieldContextPathTest {

	@Test (expected=FieldContextException.class)
	public void testFieldContextInvalidPathCreation() throws FieldContextException {
		FieldContextUtils.parseFieldContextPath(null);
	}
	
	@Test
	public void testFieldContextPathCreation() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees.address.name");
		Assert.assertEquals(path.getComponentAt(0).getStringValue(), "rs1");
		Assert.assertEquals(path.getComponentAt(1).getStringValue(), "employees");
		Assert.assertEquals(path.getComponentAt(2).getStringValue(), "address");
		Assert.assertEquals(path.getComponentAt(3).getStringValue(), "name");
	}
	
	@Test
	public void testFieldContextPathAbsolutePath() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees.address.name");
		Assert.assertEquals("rs1.employees.address.name", path.getAbsolutePath());
	}
	
	@Test
	public void testFieldContextPathLength() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees.address.name");
		Assert.assertEquals(4, path.getLength());
	}
	
	@Test
	public void testFieldContextPathHeadTail() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees.address.name");
		Assert.assertEquals("rs1", path.getHead().getStringValue());
		Assert.assertEquals("employees.address.name", path.getTailPath().getAbsolutePath());
	}
	
	@Test (expected=FieldContextException.class)
	public void testFieldContextPathWithInvalidIndexCreation() throws FieldContextException {
		FieldContextUtils.parseFieldContextPath("rs1.employees.[0].address.name");
	}
	
	@Test
	public void testFieldContextPathWithIndexCreation() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees[1].address.name");
		Assert.assertEquals(path.getComponentAt(0).getStringValue(), "rs1");
		Assert.assertEquals(path.getComponentAt(1).getStringValue(), "employees");
		Assert.assertTrue(path.getComponentAt(2).isIndex());
		Assert.assertEquals(path.getComponentAt(2).getIndexValue(), 1);
		Assert.assertEquals(path.getComponentAt(3).getStringValue(), "address");
		Assert.assertEquals(path.getComponentAt(4).getStringValue(), "name");
	}
	
	@Test
	public void testFieldContextPathWithIndexAbsolutePath1() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees[1].address.name");
		Assert.assertEquals("rs1.employees[1].address.name", path.getAbsolutePath());
	}
	
	@Test
	public void testFieldContextPathWithIndexAbsolutePath2() throws FieldContextException {
		FieldContextPath path = FieldContextUtils.parseFieldContextPath("rs1.employees.address.name[0]");
		Assert.assertEquals("rs1.employees.address.name[0]", path.getAbsolutePath());
	}
	
}
