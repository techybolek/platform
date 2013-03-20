#!/bin/bash

# Make sure the user is running as root.
if [ "$UID" -ne "0" ]; then
	echo ; echo "  You must be root to run $0.  (Try running 'sudo bash' first.)" ; echo 
	exit 69
fi

echo "Please make sure that you have deleted all running vm's(Using horizon UI or nova CLI) and /var/lib/nova/instances/instances-* does not exist"
echo "Optionally you may also remove the stratos-demo key from nova openstack using the horizon UI"


su - wso2 -c "echo '' > ~/.ssh/known_hosts"

./clean.sh


#remove any keys in /home/wso2/.ssh/authorized_keys
su - wso2 -c "echo '' > /home/wso2/.ssh/authorized_keys"

#remove all files in /var/log/nova/
rm -f /var/log/nova/*

echo "Now exit from this vm instance and create the image"

exit 0
