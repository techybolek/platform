#!/bin/bash
# This script create a Stratos2 demo release pack from svn and binaries.
# If you use this for Stratos2 vm image demo testing, then remember to copy jvm and mysql jar
# into the pack too.

if [[ -d ./demo_setup ]]; then
    rm -rf ./demo_setup
fi
if [[ -f ./demo_setup.zip ]]; then
    rm -f ./demo_setup.zip
fi

cp -rf ./setup ./demo_setup
cd ./demo_setup

cp -rf ../binaries/wso2adc-1.0.0.zip ./
cp -rf ../binaries/wso2cc-1.0.0.zip ./
cp -rf ../binaries/wso2elb-2.0.3.zip ./
cp -rf ../binaries/wso2s2agent-1.0.0.zip ./
cp -rf ../binaries/wso2s2cli-1.0.1.zip ./

cp -rf ../extra/wso2is-4.0.0.zip ./
cp -rf ../extra/wso2mb-2.0.1.zip ./
cp -rf ../extra/jdk1.6.0_24.zip ./
cp -rf ../extra/mysql-connector-java-5.1.17-bin.jar ./

cp -f ../cartridges/php.xml ./cartridges/
cp -f ../cartridges/mysql.xml ./cartridges/
cp -f ../cartridges/as1.xml ./cartridges/
cp -f ../cartridges/services/appserver1_as.xml ./cartridges/services/
cp -f ../cartridges/payload/appserver_as_001.txt ./cartridges/payload/

find ./ -name "*.svn"|xargs rm -rf

cd ../
zip -rq ./demo_setup.zip ./demo_setup/

