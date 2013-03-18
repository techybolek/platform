#!/bin/bash
# Die on any error:
set -e
product_list=$1
export LOG=/var/log/s2/s2.log
SLEEP=60

if [[ -f ./conf/setup.conf ]]; then
    source "./conf/setup.conf"
fi

function help {
    echo ""
    echo "Give one or more of the servers to be setup in this machine. The available servers are"
    echo "cc, elb, agent, sc, all. 'all' means you need to setup all servers in this machine."
    echo "usage:"
    echo "setup.sh -p\"<product list>\""
    echo "eg."
    echo "setup.sh -p\"cc elb\""
    echo ""
}

while getopts p: opts
do
  case $opts in
    p)
        product_list=${OPTARG}
        echo $product_list
        ;;
    *)
        help
        exit 1
        ;;
  esac
done
arr=$(echo $product_list | tr ";" "\n")

for x in $arr
do
    if [[ $x = "cc" ]]; then
        cc="true"
    fi
    if [[ $x = "elb" ]]; then
        elb="true"
    fi
    if [[ $x = "agent" ]]; then
        agent="true"
    fi
    if [[ $x = "sc" ]]; then
        sc="true"
    fi
    if [[ $x = "all" ]]; then
        cc="true"
        elb="true"
        agent="true"
        sc="true"
    fi
    if [[ $x = "demo" ]]; then
        demo="true"
        cc="true"
        elb="true"
        agent="true"
        sc="true"
    fi
done
product_list=`echo $product_list | sed 's/^ *//g' | sed 's/ *$//g'`
if [[ -z $product_list || $product_list = "" ]]; then
    help
    exit 1
fi

if [[ $sc = "true" ]]; then
    
    echo ${sc_path}

    echo "Starting SC server ..." >> $LOG
    nohup ${sc_path}/bin/wso2server.sh -DportOffset=$sc_port_offset &
    echo "SC server started" >> $LOG
    sleep $SLEEP

fi

if [[ $cc = "true" ]]; then
    echo ${cc_path}

    echo "Starting CC server ..." >> $LOG
    nohup ${cc_path}/bin/wso2server.sh -DportOffset=$cc_port_offset &
    echo "CC server started" >> $LOG
    sleep $SLEEP
fi

if [[ $elb = "true" ]]; then
    echo ${elb_path} 

    echo "Starting ELB server ..." >> $LOG
    nohup ${elb_path}/bin/wso2server.sh -DportOffset=$elb_port_offset &
    echo "ELB server started" >> $LOG
    sleep $SLEEP
fi

if [[ $agent = "true" ]]; then
    echo ${agent_path}

    echo "Starting AGENT server ..." >> $LOG
    nohup ${agent_path}/bin/agent.sh &
    echo "AGENT server started" >> $LOG
    sleep $SLEEP
fi


