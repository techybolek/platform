==========================
WSO2 Identity Server 4.5.0-SNAPSHOT
==========================
Welcome to the WSO2 Identity Server 4.5.0-SNAPSHOT release.

WSO2 Identity Server is an open source Identity and Entitlement management server having support for XACML, SAML, OAuth, OpenID and SCIM.

This is based on the revolutionary WSO2 Carbon framework. All the major features have been developed as pluggable Carbon components.

Key Features
============

* Entitlement Engine with XACML 3.0/2.0 and WS-XACML support.
* OAuth Authorization Server with OAuth 2.0/1.0a support.
* Identity Provisioning with SCIM 1.1.
* SAML 2.0 Web Browser SSO Identity Provider. 
* OpenID 2.0 OpenID Provider.  
* Claim based Security Token Service(STS) with SAML 2.0/1.1 support.
* Support for various types of User Stores such as JDBC, Cassandra, LDAP, Active Directory in Read/Write mode.
* Integrated Windows Authentication and webSEAL authentication.
* XMPP based multi-factor authentication.
* Improved User Management.
* Claim Management.
* User Profiles and Profile Management.
* XKMS.
* Separable front-end and back-end - a single front-end server can be used to administer several back-end servers.

System Requirements
===================

1. Minimum memory - 1 GB

2. Processor      - Pentium 800MHz or equivalent at minimum

3. Java SE Development Kit 1.6 or higher

4. The Management Console requires full Javascript enablement of the Web browser.

5. To build WSO2 Identity Server from the Source distribution, it is also necessary that you
   have Maven 3 or later.

For more details see
   http://docs.wso2.org/wiki/display/IS410/Installation+Prerequisites


Project Resources
=================

* Home page          : http://wso2.com/products/identity-server
* Library            : http://wso2.org/library/identity
* Wiki               : http://docs.wso2.org/wiki/display/IS450/WSO2+Identity+Server+Documentation
* JIRA-Issue Tracker : https://wso2.org/jira/browse/IDENTITY
                     : https://wso2.org/jira/browse/CARBON
* QA Artifacts       
     SVN             : https://svn.wso2.org/repos/wso2/trunk/commons/qa/solutions/identity
* Forums             : http://stackoverflow.com/questions/tagged/wso2/
* WSO2 Developer List  : dev@wso2.org

    
Installation and Running
========================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the /bin directory
3. Once the server starts, point your Web browser to https://localhost:9443/carbon/
4. For more information, see the Installation Guide

Known Issues
============

All known issues have been recorded at https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10908, https://wso2.org/jira/browse/IDENTITY and https://wso2.org/jira/browse/CARBON

Issues Fixed In This Release
============================

https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=10918

Carbon Binary Distribution Directory Structure
==============================================

CARBON_HOME
        |-- bin
        |-- dbscripts
        |-- lib
        |-- repository
        |-- |-- carbonapps
        |   |-- components
        |   |-- conf
        |   |-- data
        |   |-- database
        |   |-- deployment
        |   |-- logs
        |   |-- resources
        |   |   `-- security
        |   `-- tenants
        |-- samples
        |-- tmp <folder>
        |-- webapp-mode
        |-- LICENSE.txt <file>
        |-- INSTALL.txt <file>
        |-- README.txt <file>
        `-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts

    - dbscripts
      Contains the SQL scripts for setting up the database on a variety of
      Database Management Systems, including H2, Derby, MSSQL, MySQL and
      Oracle.

    - lib
      Contains the basic set of libraries required to start-up WSO2 Identity Server in standalone mode

    - repository
      The repository where services and modules deployed in WSO2 Identity Server are stored.

        - components
          Contains OSGi bundles and configurations
      
        - conf
          Contains configuration files

        - resources
          Contains resources that may be required such as security resources ; e.g. key stores.
	
	    - data
	      Contains the schema and data directories related to embedded ldap
         
        - database
          Contains the database

        - deployment
          Contains Axis2 deployment details

        - logs
          Contains all log files created during execution

        - tenants
          Contains tenant details

    - tmp
      Used for storing temporary files, and is pointed to by the java.io.tmpdir System property

    - samples
      Contains the default set of samples shipped with WSO2 Identity Server

    - LICENSE.txt
      Apache License 2.0 under which WSO2 Identity Server is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 Identity Server

    - release-notes.html
      Release information for WSO2 Identity Server 4.5.0-SNAPSHOT


Secure Sensitive Information in Carbon Configuration Files
==========================================================

There are sensitive information such as passwords in the carbon configuration.
You can secure them by using secure vault. Please go through following steps to
secure them with default mode.

1. Configure secure vault with default configurations by running ciphertool
	script from bin directory.

> ciphertool.sh -Dconfigure   (in UNIX)

This script would do following configurations that you need to do by manually

(i) Replaces sensitive elements in configuration files,  that have been defined in
		 cipher-tool.properties, with alias token values.
(ii) Encrypts plain text password which is defined in cipher-text.properties file.
(iii) Updates secret-conf.properties file with default keystore and callback class.

cipher-tool.properties, cipher-text.properties and secret-conf.properties files
			can be found at repository/conf/security directory.

2. Start server by running wso2server script from bin directory

> wso2server.sh   (in UNIX)

By default mode, it would ask you to enter the master password
(By default, master password is the password of carbon keystore and private key)

3. Change any password by running ciphertool script from bin directory.

> ciphertool -Dchange  (in UNIX)

For more details see
http://docs.wso2.org/wiki/display/Carbon404/WSO2+Carbon+Secure+Vault

Support
=======
We are committed to ensuring that your enterprise middleware deployment is completely supported from
evaluation to production. Our unique approach ensures that all support leverages our open development
methodology and is provided by the very same engineers who build the technology.

For more details and to take advantage of this unique opportunity, visit http://wso2.com/support/.


For more information on WSO2 Carbon, visit the WSO2 Oxygen Tank (http://wso2.org)


Crypto Notice
=============

This distribution includes cryptographic software.  The country in
which you currently reside may have restrictions on the import,
possession, use, and/or re-export to another country, of
encryption software.  Before using any encryption software, please
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included cryptographic
software:

Apacge Rampart   : http://ws.apache.org/rampart/
Apache WSS4J     : http://ws.apache.org/wss4j/
Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/

For more information about WSO2 Identity Server please see http://wso2.org/projects/identity or visit the
WSO2 Oxygen Tank developer portal for addition resources.

For further details, see the WSO2 Carbon documentation at
http://docs.wso2.org/wiki/display/Carbon404/WSO2+Carbon+Documentation

---------------------------------------------------------------------------
(c) Copyright 2013 WSO2 Inc.
