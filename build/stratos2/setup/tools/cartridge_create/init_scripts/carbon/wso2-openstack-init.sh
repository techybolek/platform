#!/bin/bash

# ----------------------------------------------------------------------------
#  Copyright 2005-20012 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
export LOG=/var/log/wso2-openstack.log
export instance_path=/var/lib/cloud/instance
PUBLIC_IP=""
CRON_DURATION=1
PRODUCT_NAME=""

#Check whether if any java processes are running
if [[ "$(pidof java)" ]]; then
   # process was found
   echo "An already running java process is found. Exiting..." >> $LOG
    if [[ "$(pidof cron)" ]]; then
        crontab -r
    fi
   exit 0;
else
   # process not found
   echo "No java process found. Coninue script" >> $LOG
fi

echo ---------------------------- >> $LOG


if [ ! -d ${instance_path}/payload ]; then
    echo "creating payload dir ... " >> $LOG
    mkdir ${instance_path}/payload
    echo "payload dir created ... " >> $LOG
    # payload will be copied into ${instance_path}/payload/launch-params file
    cp ${instance_path}/user-data.txt ${instance_path}/payload/launch-params
    echo "payload copied  ... " >> $LOG
    for i in `/usr/bin/ruby /opt/get-launch-params.rb`
    do
    echo "exporting to bashrc $i ... " >> $LOG
        echo "export" ${i} >> /home/ubuntu/.bashrc
    done
    source /home/ubuntu/.bashrc
    # Write a cronjob to execute wso2-openstack-init.sh periodically until public ip is assigned
    crontab -l > ./mycron
    echo "*/${CRON_DURATION} * * * * /opt/wso2-openstack-init.sh > /var/log/wso2-openstack-init.log" >> ./mycron
    crontab ./mycron
    rm ./mycron

fi


echo ---------------------------- >> $LOG

echo "getting public ip from metadata service" >> $LOG
wget http://169.254.169.254/latest/meta-data/public-ipv4
files="`cat public-ipv4`"
if [[ -z ${files} ]]; then
    echo "getting public ip. If fail retry 30 times" >> $LOG
    for i in {1..30}
    do
      rm -f ./public-ipv4
      wget http://169.254.169.254/latest/meta-data/public-ipv4
      files="`cat public-ipv4`"
      if [ -z $files ]; then
          echo "Public ip is not yet assigned. Wait and continue for $i the time ..." >> $LOG
          sleep 1
      else
          echo "Public ip assigned" >> $LOG
          crontab -r
          break
      fi
    done

    if [ -z $files ]; then
      echo "Public ip is not yet assigned. Exiting ..." >> $LOG
      exit 0
    fi
    for x in $files
    do
        PUBLIC_IP="$x"
    done


else 
    PUBLIC_IP="$files"
    crontab -r
fi


for i in `/usr/bin/ruby /opt/get-launch-params.rb`
do
    export ${i}
done


if [ "$ADMIN_USERNAME" = "" ]; then
	echo Launching with default admin username >> $LOG
else 
	cd /opt/${PRODUCT_NAME}/repository/conf
	find . -name "*user-mgt.xml" | xargs sed -i "s/<UserName>admin<\/UserName>/<UserName>$ADMIN_USERNAME<\/UserName>/g"
fi

if [ "$ADMIN_PASSWORD" = "" ]; then
	echo Launching with default admin password >> $LOG
else 
	cd /opt/${PRODUCT_NAME}/repository/conf
	find . -name "*user-mgt.xml" | xargs sed -i "s/<Password>admin<\/Password>/<Password>$ADMIN_PASSWORD<\/Password>/g"
fi

# Modifying axis2.xml file member
	cd /opt/${PRODUCT_NAME}/repository/conf/axis2

	find . -name "axis2.xml" | xargs sed -i "s/<hostName>member_host_name<\/hostName>/<hostName>$MEMBER_HOST<\/hostName>/g"
	find . -name "axis2.xml" | xargs sed -i "s/<port>member_port<\/port>/<port>$MEMBER_PORT<\/port>/g"

	find . -name "axis2.xml" | xargs sed -i "s/local_member_host<\/parameter>/$PUBLIC_IP<\/parameter>/g"
	find . -name "axis2.xml" | xargs sed -i "s/local_member_bind_address<\/parameter>/$PRIVATE_IP<\/parameter>/g"

if [[ -d /opt/${PRODUCT_NAME} ]]; then
	echo "Starting carbon server ..." >> $LOG
	nohup /opt/${PRODUCT_NAME}/bin/wso2server.sh & >> $LOG
	sleep 1
	if [[ "$(pidof java)" ]]; then
	    echo "Carbon server started" >> $LOG
	    crontab -r
    	    rm -f /opt/${PRODUCT_NAME}.zip
	fi
else

    echo "Carbon server is not started yet" >> $LOG
fi


