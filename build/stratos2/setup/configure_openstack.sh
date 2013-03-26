#!/usr/bin/env bash

# Die on any error:
set -e

# This script will configure Openstack IaaS for Stratos2.

SLEEP=90
export LOG=/var/log/s2/s2-openstack.log

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

nova secgroup-add-rule default icmp -1 -1  0.0.0.0/0
nova secgroup-add-rule default tcp 22 22  0.0.0.0/0
nova secgroup-add-rule default tcp 80 80  0.0.0.0/0
nova secgroup-add-rule default tcp 443 443  0.0.0.0/0
nova secgroup-add-rule default tcp 8080 8080  0.0.0.0/0
nova secgroup-add-rule default tcp 4103 4103  0.0.0.0/0
nova secgroup-add-rule default tcp 53 53  0.0.0.0/0

echo ""
echo "Now open a browser with your Openstack setup public dns and log into the Openstack dashboard with following credentials."
echo "You need to import the $s2_user.pub into openstack(With the same name mentioned in /opt/wso2cc-1.0.0/repository/deployment/server/cartridges/<cartridge name.xml>)"
echo "Click the 'project' tab and then 'Access & Security' menu. Under 'Keypairs' section click 'Import Keypairs'. If there is a key already with the"
echo "same name, first delete it before importing your key"
echo "Open another command line interface into SC node and"
echo "cat /home/$s2_user/$s2_user.pub" 
echo "Cut and paste the output into the box that you get when execute import keys of the openstack dashboard."
echo "When you are ready press any key to continue"
echo ""
read

nova secgroup-add-rule default tcp $s2_db_port $s2_db_port  0.0.0.0/0
nova secgroup-add-rule default tcp $mb_listen_port $mb_listen_port  0.0.0.0/0
nova secgroup-add-rule default tcp $sc_http_port $sc_http_port  0.0.0.0/0
nova secgroup-add-rule default tcp $sc_https_port $sc_https_port  0.0.0.0/0
nova secgroup-add-rule default tcp $agent_http_port $agent_http_port  0.0.0.0/0
nova secgroup-add-rule default tcp $cc_https_port $cc_https_port  0.0.0.0/0
nova secgroup-add-rule default tcp $bam_port $bam_port  0.0.0.0/0
nova secgroup-add-rule default tcp $mb_cassandra_port $mb_cassandra_port  0.0.0.0/0
nova secgroup-add-rule default tcp $elb_port $elb_port  0.0.0.0/0

sleep $SLEEP

nova secgroup-add-rule default tcp $agent_clustering_port $agent_clustering_port  0.0.0.0/0
nova secgroup-add-rule default tcp $sc_cluster_port $sc_cluster_port  0.0.0.0/0
nova secgroup-add-rule default tcp $elb_cluster_port $elb_cluster_port  0.0.0.0/0
nova secgroup-add-rule default tcp $rabbitmq_port $rabbitmq_port  0.0.0.0/0
nova secgroup-add-rule default tcp $cassandra_port1 $cassandra_port1  0.0.0.0/0
nova secgroup-add-rule default tcp $cassandra_port2 $cassandra_port2  0.0.0.0/0
nova secgroup-add-rule default tcp $hadoop_port1 $hadoop_port1  0.0.0.0/0
nova secgroup-add-rule default tcp $hadoop_port2 $hadoop_port2  0.0.0.0/0

#Install Puppet and nginx ( puppetmaster 2.7 , mongrel, nginx)
apt-get install puppetmaster mongrel nginx

#Stop puppet
/etc/init.d/puppetmaster stop

#Stop nginx
/etc/init.d/nginx stop

# Copy puppet master and nginx configuration files
cp -f config/puppet_master/auth.conf $puppet_config_path/
cp -f config/puppet_master/autosign.conf $puppet_config_path/
cp -f config/puppet_master/fileserver.conf $puppet_config_path/
cp -f config/puppet_master/puppet.conf $puppet_config_path/
cp -f config/puppet_master/puppetmaster $puppet_config_path/

# Configure puppet master configuration files
pushd $puppet_config_path
sed -i "s/STRATOS2_PUPPET_AGENT_CERT/${stratos2_puppet_agent_cert}/g" $puppet_config_path/auth.conf
sed -i "s/STRATOS2_DOMAIN/${stratos2_domain}/g" $puppet_config_path/autosign.conf
sed -i "s/STRATOS2_PUPPET_CONFIG_BASE/${stratos2_puppet_config_base}/g" $puppet_config_path/fileserver.conf
sed -i "s/STRATOS2_PUPPET_CONFIG_BASE/${stratos2_puppet_config_base}/g" $puppet_config_path/puppet.conf
popd

# Configure nginx configuration files
cp -f config/puppet_master/nginx.conf $nginx_config_path/
pushd $nginx_config_path
sed -i "s/STRATOS2_PUPPET_MASTER_KEY/${stratos2_puppet_master_key}/g" $nginx_config_path/nginx.conf
popd

# Configure Stratos2 puppet manifests, templates and modules
cp -f ./config/puppet/nodes.pp $stratos2_puppet_config_base/manifests/
cp -f ./config/puppet/hosts.erb $stratos2_puppet_config_base/templates/

#sed -i "s//${}/g" $stratos2_puppet_config_base/manifests/nodes.pp
#sed -i "s//${}/g" $stratos2_puppet_config_base/templates/hosts.erb


#Upload the cartridges
rm -rf /tmp/__upload
if [[ -f ./cartridges/php-cartridge-amd64-disk1.img ]]; then
./imageupload.sh -a $host_user -p $host_user_password -t $openstack_tenant -C $hostname -x amd64 -y ubuntu -w 12.04 -z ./cartridges/php-cartridge-amd64-disk1.img -n php-cartridge-amd64
fi
echo "php cartridge image uploaded"
rm -rf /tmp/__upload
if [[ -f ./cartridges/mysql-cartridge-amd64-disk1.img ]]; then
./imageupload.sh -a $host_user -p $host_user_password -t $openstack_tenant -C $hostname -x amd64 -y ubuntu -w 12.04 -z ./cartridges/mysql-cartridge-amd64-disk1.img -n mysql-cartridge-amd64
fi
echo "mysql cartridge image uploaded"

rm -rf /tmp/__upload
if [[ -f ./cartridges/carbon-cartridge-amd64-disk1.img ]]; then
./imageupload.sh -a $host_user -p $host_user_password -t $openstack_tenant -C $hostname -x amd64 -y ubuntu -w 12.04 -z ./cartridges/carbon-cartridge-amd64-disk1.img -n carbon-cartridge-amd64
fi
echo "carbon cartridge image uploaded"
echo "Please execute command 'nova image-list' to list the uploaded cartridges"

#Set server host name in /etc/hosts (Make sure to make this conform with nginx ssl settings)
#Start puppet
/etc/init.d/puppetmaster start

#Start nginx
/etc/init.d/nginx start




