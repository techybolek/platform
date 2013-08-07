#!/bin/bash
function setup_as {
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
#configure dev cloud
mkdir -p  $working_dir/${environment}-cloud
echo "Setting up $environment Cloud........"
/usr/bin/unzip  -q $resorce_dir/packs/wso2as-${version} -d $working_dir/${environment}-cloud
carbon_home=$working_dir/${environment}-cloud/wso2as-${version}
cp $resorce_dir/configs/cloud-manager-user-mgt.xml $carbon_home/repository/conf/user-mgt.xml
cp $resorce_dir/configs/${environment}-cloud-registry.xml $carbon_home/repository/conf/registry.xml
cat $resorce_dir/configs/${environment}-cloud-carbon.xml | sed -e "s@AF_HOST@$af_host_name@g" | sed -e "s@OFFSET@$offset@g" > $carbon_home/repository/conf/carbon.xml
cat  $resorce_dir/configs/${environment}-cloud-axis2.xml | sed -e "s@AF_HOST@$af_host_name@g" > $carbon_home/repository/conf/axis2/axis2.xml
cp $resorce_dir/configs/catalina-server.xml $carbon_home/repository/conf/tomcat/catalina-server.xml

cp $resorce_dir/lib/mysql-connector-java-5.1.12-bin.jar $carbon_home/repository/components/lib
cp $resorce_dir/configs/as-wso2server.sh $carbon_home/bin
#cp $resorce_dir/lib/org.wso2.carbon.appfactory.tenant.mgt-1.0.0.jar $carbon_home/repository/components/dropins
#cp $resorce_dir/lib/org.wso2.carbon.tenant.mgt.core-2.1.0.jar  $carbon_home/repository/components/plugins/org.wso2.carbon.tenant.mgt.core_2.1.0.jar 

}
