#!/usr/bin/env bash

# Die on any error:
set -e

# This script will configure EC2 IaaS for Stratos2.

SLEEP=90
export LOG=/var/log/s2/s2-ec2.log

source "./conf/setup.conf"

if [[ ! -d /var/log/s2 ]]; then
    mkdir -p /var/log/s2
fi
   
mkdir -p /var/www/notify
cp -f ./resources/notify.php /var/www/notify/index.php
echo "Now restart apache2 and if start successfull you configuration upto this point is OK." >> $LOG
echo "Now restart apache2 and if start successfull you configuration upto this point is OK."
apachectl stop
sleep 5
apachectl start

