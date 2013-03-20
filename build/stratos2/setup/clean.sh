#!/bin/bash
# This script is for cleaning the host machine where one or more of the Stratos2 servers are run
# Make sure the user is running as root.
if [ "$UID" -ne "0" ]; then
	echo ; echo "  You must be root to run $0.  (Try running 'sudo bash' first.)" ; echo 
	exit 69
fi

function help {
    echo ""
    echo "Clean the host machine where one or more of the Stratos2 servers are run."
    echo "usage:"
    echo "clean.sh -a<hostname> -b<stratos user name> -c<mysql user name> -d<mysql user password>"
    echo ""
}

while getopts a:b:c:d: opts
do
  case $opts in
    a)
        hostname=${OPTARG}
        ;;
    b)
        s2_user=${OPTARG}
        ;;
    c)
        mysql_user=${OPTARG}
        ;;
    d)
        mysql_pass=${OPTARG}
        ;;
    *)
        help
        exit 1
        ;;
  esac
done

function helpclean {
    echo ""
    echo "usage:"
    echo "clean.sh -a<hostname> -b<stratos user name> -c<mysql user name> -d<mysql user password>"
    echo ""
}

function clean_validate {

if [[ ( -z $hostname || -z $s2_user || -z $mysql_user || -z $mysql_pass ) ]]; then
    helpclean
    exit 1
fi
}

clean_validate

if [[ -d /home/git ]]; then
    deluser git
    rm -fr /home/git
    mysql -u $mysql_user -p$mysql_pass -e "DROP DATABASE IF EXISTS s2_foundation;"
    mysql -u $mysql_user -p$mysql_pass -e "DROP DATABASE IF EXISTS userstore;"
fi

killall java
sleep 15
rm -rf /opt/*
rm -rf /var/log/s2/*
rm -f /home/$s2_user/.ssh/id_rsa

#remove contents of upload folder
if [[ -d /home/$s2_user/upload ]]; then
    rm -f /home/$s2_user/upload/*
fi

#clean /etc/hosts
KEYWORD='git.'
if grep -Fxq "$KEYWORD" /etc/hosts
then
    cat /etc/hosts | grep -v "$KEYWORD" > /tmp/hosts
    mv /tmp/hosts /etc/hosts
fi

#if grep -Fxq "$hostname" /etc/hosts
#then
#    cat /etc/hosts | grep -v "$hostname" > /tmp/hosts
#    mv /tmp/hosts /etc/hosts
#fi

