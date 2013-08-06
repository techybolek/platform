#!/bin/bash
function setup_sc {
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
pack_dir=$working_dir/${environment}-cloud/s2/
mkdir -p $pack_dir
echo "Setting up ${environment} Controller........"
/usr/bin/unzip  -q $resorce_dir/packs/wso2sc-${version}.zip -d $pack_dir
S2_SC_HOME=$pack_dir/wso2sc-${version}
cp $resorce_dir/configs/cloud-manager-user-mgt.xml $S2_SC_HOME/repository/conf/user-mgt.xml
cat $resorce_dir/configs/cloud-manager-axis2.xml | sed -e "s@AF_HOST@$af_host_name@g" > $S2_SC_HOME/repository/conf/axis2/axis2.xml
cp $resorce_dir/configs/${environment}-cloud-registry.xml $S2_SC_HOME/repository/conf/registry.xml
cat $resorce_dir/configs/cloud-manager-carbon.xml | sed -e "s@AF_HOST@$af_host_name@g" | sed -e "s@OFFSET@$offset@g" > $S2_SC_HOME/repository/conf/carbon.xml
#cp resources/configs/cartridge-config.properties $S2_SC_HOME/repository/conf/cartridge-config.properties
#cp $resorce_dir/configs/cloud-manager-tenant-mgt.xml $S2_SC_HOME/repository/conf/tenant-mgt.xml
cp $resorce_dir/configs/cloud-manager-stratos.xml $S2_SC_HOME/repository/conf/multitenancy/stratos.xml

#mkdir $S2_SC_HOME/repository/conf/appfactory
#cp $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml $S2_SC_HOME/repository/conf/appfactory

#cp $APPFACTORY_HOME/repository/components/plugins/org.wso2.carbon.appfactory.common_1.0.2.jar $S2_SC_HOME/repository/components/dropins
#cp resources/lib/org.wso2.carbon.appfactory.tenant.roles-1.0.2.jar $S2_SC_HOME/repository/components/dropins
#cp resources/lib/org.wso2.carbon.appfactory.tenant.mgt.stub-1.0.0.jar $S2_SC_HOME/repository/components/dropins
#cp $APPFACTORY_HOME/repository/components/plugins/org.wso2.carbon.appfactory.userstore_1.0.2.jar $S2_SC_HOME/repository/components/lib
cp $resorce_dir/lib/mysql-connector-java-5.1.12-bin.jar $S2_SC_HOME/repository/components/lib
cp -r $resorce_dir/patches/sc/*  $S2_SC_HOME/repository/components/patches

}
