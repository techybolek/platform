#!/bin/bash
function setup_elb {
unset OPTIND
while getopts w:r:e:v:h:o: option
do
        case "${option}"
        in
                w) working_dir=${OPTARG};;
                r) resorce_dir=${OPTARG};;
                e) environment=${OPTARG};;
                v) version=$OPTARG;;
                h) af_host_name=$OPTARG;;
                o) offset=$OPTARG;;
        esac
done

pack_dir=$working_dir/elb
mkdir -p $pack_dir
echo "Setting up ELB........"

/usr/bin/unzip  -q $resorce_dir/packs/wso2elb-${version}.zip -d $pack_dir
ELB_HOME=$pack_dir/wso2elb-${version}

cat $resorce_dir/configs/elb-carbon.xml | sed -e "s@OFFSET@$offset@g" > $ELB_HOME/repository/conf/carbon.xml
cp $resorce_dir/configs/elb-axis2.xml $ELB_HOME/repository/conf/axis2/axis2.xml
cat $resorce_dir/configs/loadbalancer.conf | sed -e "s@AF_HOST@$af_host_name@g" >  $ELB_HOME/repository/conf/loadbalancer.conf

}
