#!/bin/bash
function setup_build_server {
unset OPTIND
while getopts w:r:v:h:o: option
do
        case "${option}"
        in
                w) working_dir=${OPTARG};;
                r) resorce_dir=${OPTARG};;
                v) version=$OPTARG;;
                h) af_host_name=$OPTARG;;
                o) offset=$OPTARG;;
        esac
done

BUILD_SERVER_AS_HOME=$working_dir/buildserver/wso2as-${version}
JENKINS_TENANT_HOME=$BUILD_SERVER_AS_HOME/repository/jenkins

#Configure Build Server
echo "Setting up Build Server........."
mkdir $working_dir/buildserver

echo "[Build Server] Copying AS..."
/usr/bin/unzip -q $resorce_dir/packs/wso2as-${version}.zip -d $working_dir/buildserver

echo "[Build Server] Copying config xmls..."
cp $resorce_dir/configs/cloud-manager-user-mgt.xml $BUILD_SERVER_AS_HOME/repository/conf/user-mgt.xml
cp $resorce_dir/configs/cloud-manager-registry.xml $BUILD_SERVER_AS_HOME/repository/conf/registry.xml
cp $resorce_dir/configs/buildserver-tenant-mgt.xml $BUILD_SERVER_AS_HOME/repository/conf/tenant-mgt.xml

cat $resorce_dir/configs/buildserver-carbon.xml | sed -e "s@AF_HOST@$af_host_name@g"  | sed -e "s@OFFSET@$offset@g" > $BUILD_SERVER_AS_HOME/repository/conf/carbon.xml
cat $resorce_dir/configs/buildserver-axis2.xml | sed -e "s@AF_HOST@$af_host_name@g" > $BUILD_SERVER_AS_HOME/repository/conf/axis2/axis2.xml
cp $resorce_dir/configs/buildserver-webapp-classloading-environments.xml $BUILD_SERVER_AS_HOME/repository/conf/tomcat/webapp-classloading-environments.xml

cp $resorce_dir/configs/buildserver-wso2server.sh $BUILD_SERVER_AS_HOME/bin/wso2server.sh

cp $resorce_dir/lib/mysql-connector-java-5.1.12-bin.jar $BUILD_SERVER_AS_HOME/repository/components/lib

echo "[Build Server] Copying jenkins war..."
cp $resorce_dir/packs/jenkins.war $BUILD_SERVER_AS_HOME/repository/resources/

echo "[Build Server] Copying tenant integration component..."
cp $resorce_dir/lib/jenkins/org.wso2.carbon.appfactory.multitenant.jenkins-1.1.0-SNAPSHOT.jar $BUILD_SERVER_AS_HOME/repository/components/dropins

echo "[Build Server] Copying jenkins runtime..."
mkdir $BUILD_SERVER_AS_HOME/lib/runtimes/jenkins
cp $resorce_dir/lib/jenkins/runtime/* $BUILD_SERVER_AS_HOME/lib/runtimes/jenkins
cp $resorce_dir/lib/jenkins/org.wso2.carbon.appfactory.jenkinsext-1.0-SNAPSHOT.jar $BUILD_SERVER_AS_HOME/lib/runtimes/jenkins/

echo "[Build Server] Copying jenkins plugins..."
mkdir -p $JENKINS_TENANT_HOME
cp -r $resorce_dir/lib/jenkins/plugins $JENKINS_TENANT_HOME/

echo "[Build Server] Copying AS JNDI patch..."
cp $resorce_dir/lib/jenkins/AS-patch/org.wso2.carbon.tomcat_4.1.0.jar $BUILD_SERVER_AS_HOME/repository/components/plugins/

echo "[Build Server] Configuration done."

}



