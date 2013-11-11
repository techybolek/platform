================================================================================
                        WSO2 Complex Event Processing Server 3.0.0
================================================================================

Welcome to the WSO2 CEP 3.0.0 release

WSO2 CEP is a lightweight and easy-to-use Open Source Complex Event Processing
Server (CEP) is available under the Apache Software License v2.0.WSO2 Complex
Event Processor identifies the most meaningful events within the event cloud,
analyzes their impacts, and acts on them in real time. Its built to be extremely
high performing and massively scalable.

This is based on the revolutionary WSO2 Carbon framework. All the major features
have been developed as pluggable Carbon components.

New Features In This Release
==================================

1. Siddhi Query Partitioning
2. Siddhi Event Tables
3. Event Adaptor Manager components to handle broker manager related configurations for CEP 3.0.0
4. Output rate limiting support for Siddhi
5. "coalesce" support for Siddhi
6. External Time Window for Siddhi
7. Stats calculation to Siddhi
8. Event tracing ability to Siddhi
9. Default timestamp converter for Siddhi
10. Attribute Type Converters to Siddhi
11. Support Multiple Input Event Adaptor Types - WSO2 CEP supports WSO2Event, WS-Event, WS-Event-Local, JMS, Email, input adaptor types
12. Support Multiple Output Event Adaptor Types - WSO2 CEP supports WSO2Event, WS-Event, WS-Event-Local, JMS, Email, MySQL, Cassandra, HTTP, SMS output adaptor types
13. Reading data from DB and use it within queries using External calls
14. New component called "EventFormatter" to handle the events when sending out from the CEP
15. New component called "EventBuilder" to handle incoming events to CEP
16. Monitoring Support - WSO2 CEP supports system, per execution plan/per deployment artifact monitoring.
17. Tracing Support - WSO2 CEP supports tracing a message at multiple stages of the event flow.
18. JMS Queue support to CEP
19. Ability for Siddhi to retrieve resources/properties from registry
20. Processing XML/JSON input child elements as separate input messages
21. Registry integration for Event Formatter (XML, Text and JSON mapping)
22. Sample framework for CEP
23. Dashboard integration using WSO2 User Engagement Server
24. Bug fixes

Key Features of WSO2 CEP
==================================
* Support Multiple Input Event Adaptor Types - WSO2 CEP supports WSO2Event, WS-Event, WS-Event-Local, JMS, Email, input adaptor types
* Support Multiple Output Event Adaptor Types - WSO2 CEP supports WSO2Event, WS-Event, WS-Event-Local, JMS, Email, MySQL, Cassandra, HTTP, SMS output adaptor types
* Event Adaptor Manager components to handle adaptor manager related configurations for CEP 3.0.0
* Siddhi Query Partitioning
* Siddhi Event Tables
* External Time Window for Siddhi
* New component called "EventFormatter" to handle the events when sending out from the CEP
* New component called "EventBuilder" to handle incoming events to CEP
* Output rate limiting support for Siddhi
* Attribute Type Converters to Siddhi
* Monitoring Support - WSO2 CEP supports system, per execution plan/per deployment artifact monitoring.
* Tracing Support - WSO2 CEP supports tracing a message at multiple stages of the event flow.
* Registry integration for Event Formatter (XML, Text and JSON mapping)
* Sample framework for CEP
* Registry based Output Topics
* Registry based stream definition store
* Easily Integrates with Enterprise System - by RESTful HTTP protocol with JSON, JMS MAP/XML/Text messages, SOAP and Email
* Supports Highly Available Deployment - Using Hazelcast distributed cache as a shared working memory.
* Support for Long Duration Queries - Supports periodic snapshots to a scalable persistence store (Apache Cassandra).
* Tightly Integrates with WSO2 Business Activity Monitor - for recording and post processing of events with Map-Reduce via Apache Hadoop.
* Dashboard integration using WSO2 User Engagement Server
* Enhanced Siddhi Extensions with new extension points
* Configuration based class loading mechanism for Siddhi
* Arbitrary key values pair support for WSO2 Events

System Requirements
==================================

1. Minimum memory - 1 GB
2. Processor      - Pentium 800MHz or equivalent at minimum
3. Java SE Development Kit 1.6.0_21 or higher
4. To build WSO2 CEP from the Source distribution, it is necessary that you have
   JDK 1.6 and Maven 3

For more details see
    http://docs.wso2.org/wiki/display/CEP300/Installation+Prerequisites

Installation & Running
==================================

1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/

4. Use the following username and password to login

    username : admin
    password : admin

You can use the carbon Feature management Console and add the back end runtime features to CEP.
For more information on installation read INSTALL.txt.
   

WSO2 CEP 3.0.0 Binary Distribution Directory Structure
==========================================================


     CARBON_HOME
        |-- bin <directory>
        |-- dbscripts <directory>
        |-- lib <directory>
        |-- client-lib
        |-- repository <directory>           |
        |   |-- components <directory>
        |   |-- conf <directory>
        |   |-- data <directory>
        |   |-- database <directory>
        |   |-- deployment <directory>
        |   |-- logs <directory>
        |   |-- resources <directory>
        |   |   -- security <directory>
        |   `-- tenants <directory>
        |-- modules <directory>
        |-- resources <directory>
        |-- webapp-mode <directory>
        |-- tmp <directory>
	    |-- samples <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        `-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases.

    - lib
      Contains the basic set of libraries required to start-up  WSO2 CEP
      in standalone mode

    - client-lib
          Contains the basic set of libraries required to run the Data-bridge clients.

    - repository
      The repository where services and modules deployed in WSO2 CEP
      are stored.

        - carbonapps
          Carbon Application hot deployment directory.

    	- components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. Ex: axis2.xml, carbon.xml

        - data
          Contains internal LDAP related data.

        - database
          Contains the database

        - deployment
          Contains server side and client side Axis2 repositories.
	      All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

        - resources
          Contains additional resources that may be required.

	    - tenants
	      Directory will contain relevant tenant artifacts
	      in the case of a multitenant deployment.

    -modules
      contains the modules that needed for UES dashboard.

    -resources
      contains the resources like themes.

    - webapp-mode
      The user has the option of running WSO2 Carbon in webapp mode (hosted as a web-app in an application server).
            This directory contains files required to run Carbon in webapp mode.

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property.

    - LICENSE.txt
      Apache License 2.0 under which WSO2 CEP is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document contains information on installing WSO2 CEP

    - release-notes.html
      Release information for WSO2 CEP 3.0.0


Secure sensitive information in carbon configuration files
==============================================================

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
http://docs.wso2.org/wiki/display/Carbon420/WSO2+Carbon+Secure+Vault


Training
==================================

WSO2 Inc. offers a variety of professional Training Programs, including
training on general Web services as well as WSO2 CEP, Siddhi, WSO2 BAM,
Data Services and a number of other products.

For additional support information please refer to
http://wso2.com/training/

Support
==================================

We are committed to ensuring that your enterprise middleware deployment is completely supported
from evaluation to production. Our unique approach ensures that all support leverages our open
development methodology and is provided by the very same engineers who build the technology.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 CEP, visit the WSO2 Oxygen Tank (http://wso2.com)


Issue Tracker
==================================

  https://wso2.org/jira/browse/CARBON
  https://wso2.org/jira/browse/CEP



Crypto Notice
==================================

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
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

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/


For further details, see theWSO2 Complex Event Processor documentation at
http://docs.wso2.org/wiki/display/CEP300/

---------------------------------------------------------------------------
(c) Copyright 2013 WSO2 Inc.