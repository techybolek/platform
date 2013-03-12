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
export LOG=/var/log/wso2-cartridge.log
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
    cp ${instance_path}/user-data.txt ${instance_path}/user-data.zip
    pushd ${instance_path}
    unzip user-data.zip
    popd
    cp ${instance_path}/id_rsa /root/.ssh/wso2
    chmod 600 /root/.ssh/wso2
    cp ${instance_path}/launch-params ${instance_path}/payload/launch-params
    echo "payload copied  ... " >> $LOG
    # Write a cronjob to execute wso2-cartridge-init.sh periodically until public ip is assigned
    crontab -l > ./mycron
    echo "*/${CRON_DURATION} * * * * /opt/wso2-cartridge-init.sh > /var/log/wso2-cartridge-init.log" >> ./mycron
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

echo "Replacing template with payload values" >> $LOG

CONF_PATH="/opt/wso2as-5.0.1/repository/conf"
PREFIX="stratos_cartridge_demo"
AXIS_2_DIR="axis2"
AXIS_2_XML="axis2.xml"
CARBON_XML="carbon.xml"
DATASOURCE_DIR="datasources"
DATASOURCE_XML="master-datasources.xml"
JAVA_HOME=/opt/jdk1.6.0_33
export JAVA_HOME
PATH=$PATH:$JAVA_HOME/bin
export PATH

LOCAL_MEMBER_BIND_ADDRESS="$(/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')"

echo "Reading payload.." >> $LOG
FILENAME="${instance_path}/payload/launch-params"
LINE=$(head -n 1 $FILENAME)
spaced_line=`echo "$LINE" | tr ',' ' '`

line=$spaced_line
  for keyvalue in $line; do
    key=${keyvalue%%=*}
    value=${keyvalue##*=}
    echo "exporting $key=$value"
    export $key=$value
  done

echo "Setting values ..." >> $LOG
CARBON_HOST_NAME_VALUE=${CARBON_HOST_NAME}
CLUSTER_DOMAIN_VALUE=${CLUSTER_DOMAIN}
ELB_IP_VALUE=${ELB_IP}
ELB_PORT_VALUE=${ELB_PORT}
ADC_IP_VALUE=${ADC_IP}
ADC_PORT_VALUE=${ADC_PORT}
GIT_HOST_NAME_VALUE=${GIT_HOST_NAME}
GIT_IP_VALUE=${GIT_IP}
REPO_INFO_EPR_VALUE=${REPO_INFO_EPR}
USERSTORE_URL_VALUE=${USERSTORE_URL}
USERSTORE_USERNAME_VALUE=${USERSTORE_USERNAME}
USERSTORE_PASSWORD_VALUE=${USERSTORE_PASSWORD}
CARTRIDGE_ALIAS_VALUE=${CARTRIDGE_ALIAS}

echo "Values ${CARBON_HOST_NAME_VALUE} ${ELB_IP_VALUE} ${ADC_PORT_VALUE} ${USERSTORE_URL_VALUE} ${USERSTORE_PASSWORD_VALUE}"

echo "setting HostName to ${CARBON_HOST_NAME_VALUE} in ${CARBON_XML}" >> $LOG
find ${CONF_PATH}/${CARBON_XML} | xargs sed -i "s/${PREFIX}\_host_name/${CARBON_HOST_NAME_VALUE}/"

echo "Setting RepoInfoServiceEPR to ${REPO_INFO_EPR_VALUE} in ${CARBON_XML}" >> $LOG
find ${CONF_PATH}/${CARBON_XML} | xargs sed -i "s/${PREFIX}\_repo_info_epr/${REPO_INFO_EPR_VALUE}/"

echo "Setting Cartridge Alias to ${CARTRIDGE_ALIAS_VALUE} in ${CARBON_XML}" >> $LOG
find ${CONF_PATH}/${CARBON_XML} | xargs sed -i "s/${PREFIX}\_cartridge_alias/${CARTRIDGE_ALIAS_VALUE}/"

echo "setting WSDLEPRPrefix entry in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_host_name/${CARBON_HOST_NAME_VALUE}/"

echo "setting domain to ${CLUSTER_DOMAIN_VALUE}  in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_cluster_domain/${CLUSTER_DOMAIN_VALUE}/"

echo "setting localMemberHost to ${PUBLIC_IP} in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_local_member_host/${PUBLIC_IP}/"

echo "setting localMemberBindAddress to ${LOCAL_MEMBER_BIND_ADDRESS} in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_local_member_bind_address/${LOCAL_MEMBER_BIND_ADDRESS}/"

echo "setting member hostName to ${ELB_IP_VALUE} in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_elb_ip/${ELB_IP_VALUE}/"

echo "setting member port to $ELB_PORT_VALUE in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_elb_port/${ELB_PORT_VALUE}/"

echo "setting member hostName to ${ADC_IP_VALUE} in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_adc_ip/${ADC_IP_VALUE}/"

echo "setting member port to $ADC_PORT_VALUE in ${AXIS_2_XML}" >> $LOG
find ${CONF_PATH}/${AXIS_2_DIR}/${AXIS_2_XML} | xargs sed -i "s/${PREFIX}\_adc_port/${ADC_PORT_VALUE}/"

echo "setting user store connection parameters in ${DATASOURCE_XML} " >> $LOG
find ${CONF_PATH}/${DATASOURCE_DIR}/${DATASOURCE_XML} | xargs sed -i "s/${PREFIX}\_db_url/${USERSTORE_URL_VALUE}/"
find ${CONF_PATH}/${DATASOURCE_DIR}/${DATASOURCE_XML} | xargs sed -i "s/${PREFIX}\_db_username/${USERSTORE_USERNAME_VALUE}/"
find ${CONF_PATH}/${DATASOURCE_DIR}/${DATASOURCE_XML} | xargs sed -i "s/${PREFIX}\_db_password/${USERSTORE_PASSWORD_VALUE}/"

echo "adding etc/host entries.." >> $LOG
echo "${ELB_IP_VALUE}  ${CARBON_HOST_NAME_VALUE}" >> /etc/hosts
echo "${GIT_IP_VALUE}  ${GIT_HOST_NAME_VALUE}" >> /etc/hosts

echo "configuration customization complete..! " >> $LOG

sleep 1

if [[ "$(pidof java)" ]]; then
   # process was found
   echo "An already running java process is found. Exiting..." >> $LOG
   # if [[ "$(pidof cron)" ]]; then
    #    crontab -r
    #fi
   exit 0;
else
   # process not found
   echo "No java process found. starting java " >> $LOG 
   echo "starting carbon..." >> $LOG
   /opt/wso2as-5.0.1/bin/wso2server.sh
fi
