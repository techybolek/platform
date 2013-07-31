#!/bin/bash

#JAVA_HOME=/opt/jdk1.6.0_25
#export JAVA_HOME
#PATH=$PATH:/uer/bin:$JAVA_HOME/bin
#export PATH
APPSERVER_VERSION=5.1.0
#This is required to skip host name verfication in git client
export GIT_SSL_NO_VERIFY=1

ELB_HOME=`pwd`/setup/elb/wso2elb-2.0.3
APPFACTORY_HOME=`pwd`/setup/appfactory/wso2appfactory-1.0.0
DEV_CONTROLLER_HOME=`pwd`/setup/dev-cloud/controller/wso2sc-1.0.1
TEST_CONTROLLER_HOME=`pwd`/setup/test-cloud/controller/wso2sc-1.0.1
STAGING_CONTROLLER_HOME=`pwd`/setup/staging-cloud/controller/wso2sc-1.0.1
PROD_CONTROLLER_HOME=`pwd`/setup/prod-cloud/controller/wso2sc-1.0.1
DEV_CLOUD_AS_HOME=`pwd`/setup/dev-cloud/wso2as-${APPSERVER_VERSION}
TEST_CLOUD_AS_HOME=`pwd`/setup/test-cloud/wso2as-${APPSERVER_VERSION}
PROD_CLOUD_AS_HOME=`pwd`/setup/prod-cloud/wso2as-${APPSERVER_VERSION}
STAGING_CLOUD_AS_HOME=`pwd`/setup/staging-cloud/wso2as-${APPSERVER_VERSION}
APIMANAGER_HOME=`pwd`/setup/apimanager/wso2am-1.3.1
JENKINS_HOME=`pwd`/setup/jenkins
REDMINE_HOME=`pwd`/setup/redmine/apache-tomcat-7.0.32
GITBLIT_HOME=`pwd`/setup/gitblit
DEV_SS_HOME=`pwd`/setup/dev-ss/wso2ss-1.0.2
TEST_SS_HOME=`pwd`/setup/test-ss/wso2ss-1.0.2
STAGING_SS_HOME=`pwd`/setup/staging-ss/wso2ss-1.0.2
PROD_SS_HOME=`pwd`/setup/prod-ss/wso2ss-1.0.2
S2_SC_HOME=`pwd`/setup/s2/wso2sc-1.0.1
S2_CC_HOME=`pwd`/setup/s2/wso2cc-1.0.1
S2_ELB_HOME=`pwd`/setup/s2/wso2elb-2.0.5
S2_GITBLIT_HOME=`pwd`/setup/s2/gitblit

. `pwd`/setup.conf

echo "*******Starting to set up Appfactory************"
SETUP_DIR=`pwd`/setup;
RESOURCE_DIR=`pwd`/resources
echo $SETUP_DIR
if [ ! -d "$SETUP_DIR" ];then
echo "Setting up the deployment first time"
mkdir setup
#configure elb
mkdir setup/elb
echo "Setting up ELB........"

/usr/bin/unzip  -q resources/packs/wso2elb-2.0.3.zip -d setup/elb

cp resources/configs/elb-carbon.xml $ELB_HOME/repository/conf/carbon.xml
cp resources/configs/elb-axis2.xml $ELB_HOME/repository/conf/axis2/axis2.xml
cat resources/configs/loadbalancer.conf | sed -e "s@AF_HOST@$af_host_name@g" >  $ELB_HOME/repository/conf/loadbalancer.conf

#configure appfactory
mkdir setup/appfactory
echo "Setting up Appfactory........"
/usr/bin/unzip  -q resources/packs/wso2appfactory-1.0.0.zip   -d setup/appfactory

cp resources/configs/wso2server.sh $APPFACTORY_HOME/bin
cat resources/configs/appfactory.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml
cp resources/configs/appfactory-user-mgt.xml $APPFACTORY_HOME/repository/conf/user-mgt.xml
cp resources/configs/appfactory-registry.xml $APPFACTORY_HOME/repository/conf/registry.xml
cat resources/configs/appfactory-carbon.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/carbon.xml
cat resources/configs/appfactory-axis2.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/axis2/axis2.xml
cat resources/configs/appfactory-confirmation-email-config.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/email/confirmation-email-config.xml
cat resources/configs/appfactory-invite-user-email-config.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/email/invite-user-email-config.xml
cat resources/configs/appfactory-sso-idp-config.xml  | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/sso-idp-config.xml
cp resources/configs/appfactory-humantask.xml $APPFACTORY_HOME/repository/conf/humantask.xml
cp resources/configs/carbon-console-web.xml $APPFACTORY_HOME/repository/conf/tomcat/carbon/WEB-INF/web.xml
cp -r resources/patches/af/*  $APPFACTORY_HOME/repository/components/patches
cp -r resources/CreateTenant.zip  $APPFACTORY_HOME/repository/deployment/server/bpel/
cp -r resources/appmgt  $APPFACTORY_HOME/repository/deployment/server/jaggeryapps/
cp -r resources/configs/endpoints  $APPFACTORY_HOME/repository/conf/appfactory

#cp resources/org.wso2.carbon.appfactory.apiManager.integration-1.0.2.jar $APPFACTORY_HOME/repository/components/plugins/org.wso2.carbon.appfactory.apiManager.integration_1.0.2.jar
#this is required for fast app page load
cp resources/configs/tenant-mgt.xml $APPFACTORY_HOME/repository/conf/tenant-mgt.xml


cp resources/lib/mysql-connector-java-5.1.12-bin.jar $APPFACTORY_HOME/repository/components/lib
mkdir  setup/tmp
cd setup/tmp
mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/af-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.archetype -DartifactId=af-archetype -Dversion=1.0.0 -Dpackaging=jar > /dev/null
mvn archetype:generate -DartifactId=afdefault -DgroupId=org.wso2.af -DarchetypeArtifactId=maven-archetype-webapp -Dversion=SNAPSHOT -DinteractiveMode=false  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/jaxrs-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.jaxrsarchetype -DartifactId=jaxrs-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null
mvn archetype:generate -DartifactId=jaxrsdefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.jaxrsarchetype -DarchetypeArtifactId=jaxrs-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local   > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/jaxws-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.jaxwsarchetype -DartifactId=jaxws-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null
mvn archetype:generate -DartifactId=jaxwsdefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.jaxwsarchetype -DarchetypeArtifactId=jaxws-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/jaggery-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.jaggeryarchetype -DartifactId=jaggery-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null

mvn archetype:generate -DartifactId=jaggerydefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.jaggeryarchetype -DarchetypeArtifactId=jaggery-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/bpel-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.bpelarchetype -DartifactId=bpel-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null

mvn archetype:generate -DartifactId=bpeldefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.bpelarchetype -DarchetypeArtifactId=bpel-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/dbs-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.dbsarchetype -DartifactId=dbs-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null

mvn archetype:generate -DartifactId=dbsdefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.dbsarchetype -DarchetypeArtifactId=dbs-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null
cd ../..

#configure controller
mkdir setup/s2
echo "Setting up Controller........"
/usr/bin/unzip  -q resources/packs/wso2sc-1.0.1.zip -d setup/s2

cp resources/configs/cloud-manager-user-mgt.xml $S2_SC_HOME/repository/conf/user-mgt.xml
cat resources/configs/cloud-manager-axis2.xml | sed -e "s@AF_HOST@$af_host_name@g" > $S2_SC_HOME/repository/conf/axis2/axis2.xml
cp resources/configs/cloud-manager-registry.xml $S2_SC_HOME/repository/conf/registry.xml
cat resources/configs/cloud-manager-carbon.xml | sed -e "s@AF_HOST@$af_host_name@g" > $S2_SC_HOME/repository/conf/carbon.xml
#cp resources/configs/cartridge-config.properties $S2_SC_HOME/repository/conf/cartridge-config.properties
cp resources/configs/tenant-mgt.xml $S2_SC_HOME/repository/conf/tenant-mgt.xml
cp resources/configs/cloud-manager-stratos.xml $S2_HOME_HOME/repository/conf/multitenancy/stratos.xml

mkdir $S2_SC_HOME/repository/conf/appfactory
cp $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml $S2_SC_HOME/repository/conf/appfactory

cp $APPFACTORY_HOME/repository/components/plugins/org.wso2.carbon.appfactory.common_1.0.2.jar $S2_SC_HOME/repository/components/dropins
cp resources/lib/org.wso2.carbon.appfactory.tenant.roles-1.0.2.jar $S2_SC_HOME/repository/components/dropins
cp resources/lib/org.wso2.carbon.appfactory.tenant.mgt.stub-1.0.0.jar $S2_SC_HOME/repository/components/dropins
cp $APPFACTORY_HOME/repository/components/plugins/org.wso2.carbon.appfactory.userstore_1.0.2.jar $S2_SC_HOME/repository/components/lib
cp resources/lib/mysql-connector-java-5.1.12-bin.jar $S2_SC_HOME/repository/components/lib


. `pwd`/set-up-as.sh

 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "dev" -v $APPSERVER_VERSION -h $af_host_name
 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "test" -v $APPSERVER_VERSION -h $af_host_name
 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "staging" -v $APPSERVER_VERSION -h $af_host_name
 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "prod" -v $APPSERVER_VERSION -h $af_host_name

 echo "Creating Databases ........"

 MYSQL=`which mysql`

 Q0="DROP DATABASE IF EXISTS userstore;"
 Q1d="DROP DATABASE IF EXISTS devregistry;"
 Q1t="DROP DATABASE IF EXISTS testregistry;"
 Q1s="DROP DATABASE IF EXISTS stagingregistry;"
 Q1p="DROP DATABASE IF EXISTS prodregistry;"
 Q2="DROP DATABASE IF EXISTS registry;"


 Q3="CREATE DATABASE userstore;"
 Q4="USE userstore;"
 Q5="SOURCE $APPFACTORY_HOME/dbscripts/mysql.sql;"

 Q6="CREATE DATABASE registry;"
 Q7d="CREATE DATABASE devregistry;"
 Q7t="CREATE DATABASE testregistry;"
 Q7s="CREATE DATABASE stagingregistry;"
 Q7p="CREATE DATABASE prodregistry;"
 Q8="USE registry;"
 Q8d="USE devregistry;"
 Q8t="USE testregistry;"
 Q8s="USE stagingregistry;"
 Q8p="USE prodregistry;"



 SQL="${Q0}${Q1d}${Q1t}${Q1s}${Q1p}${Q2}${Q3}${Q4}${Q5}${Q6}${Q8}${Q5}${Q7d}${Q8d}${Q5}${Q7t}${Q8t}${Q5}${Q7s}${Q8s}${Q5}${Q7p}${Q8p}${Q5}"

 $MYSQL -uroot -proot -A -e "$SQL";



fi
