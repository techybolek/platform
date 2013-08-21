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

. `pwd`/set-up-elb.sh
 setup_elb -w $SETUP_DIR -r $RESOURCE_DIR -e "dev" -v $ELB_VERSION -h $af_host_name -o 10


#configure appfactory
 . `pwd`/set-up-af.sh
 setup_af -w $SETUP_DIR -r $RESOURCE_DIR -e "dev" -v $AF_VERSION -h $af_host_name -o 0

. `pwd`/set-up-sc.sh
 setup_sc -w $SETUP_DIR -r $RESOURCE_DIR -e "dev" -v $SC_VERSION -h $af_host_name -o 20 
 setup_sc -w $SETUP_DIR -r $RESOURCE_DIR -e "test" -v $SC_VERSION -h $af_host_name -o 21
# setup_sc -w $SETUP_DIR -r $RESOURCE_DIR -e "staging" -v $SC_VERSION -h $af_host_name -o 22
 setup_sc -w $SETUP_DIR -r $RESOURCE_DIR -e "prod" -v $SC_VERSION -h $af_host_name -o 23

. `pwd`/set-up-as.sh

 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "dev" -v $APPSERVER_VERSION -h $af_host_name -o 2
 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "test" -v $APPSERVER_VERSION -h $af_host_name -o 4
# setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "staging" -v $APPSERVER_VERSION -h $af_host_name -o 7
 setup_as -w $SETUP_DIR -r $RESOURCE_DIR -e "prod" -v $APPSERVER_VERSION -h $af_host_name -o 5

#configure build server
. `pwd`/set-up-build-server.sh
 setup_build_server -w $SETUP_DIR -r $RESOURCE_DIR -v $APPSERVER_VERSION -h $af_host_name -o 30

#configure git server
. `pwd`/set-up-gitblit.sh
 setup_git_server -w $SETUP_DIR -r $RESOURCE_DIR -v $GITBLIT_VERSION -h $af_host_name -o 0


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
 Q5="SOURCE `pwd`/mysql.sql;"

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

#configure bam
. `pwd`/set-up-bam.sh
setup_bam -w $SETUP_DIR -r $RESOURCE_DIR -e "dev" -v $BAM_VERSION -h $bam_host_name -o 3

find $SETUP_DIR -name .svn | xargs rm -rf


fi
