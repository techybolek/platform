 #!/bin/bash
function setup_bam {
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
 
#configure appfactory
pack_dir=$working_dir/bam/
mkdir -p $pack_dir
echo "Setting up BAM........"
/usr/bin/unzip  -q $resorce_dir/packs/wso2bam-${version}.zip   -d $pack_dir
BAM_HOME=$pack_dir/wso2bam-${version}

cp $resorce_dir/configs/bam-user-mgt.xml $BAM_HOME/repository/conf/user-mgt.xml
cat $resorce_dir/configs/bam-carbon.xml | sed -e "s@OFFSET@$offset@g" > $BAM_HOME/repository/conf/carbon.xml
cp -r $resorce_dir/precreated/dashboards/* $BAM_HOME/repository/deployment/server/jaggeryapps/
cp $resorce_dir/precreated/datafiles/* $BAM_HOME/repository/deployment/server/jaggeryapps/
cp -r $resorce_dir/precreated/gadgets/* $BAM_HOME/repository/deployment/server/jaggeryapps/portal/gadgets/
cp $resorce_dir/lib/mysql-connector-java-5.1.12-bin.jar $BAM_HOME/repository/components/lib

cp -f $resorce_dir/patches/bam/registry-space.js $BAM_HOME/modules/carbon/scripts/user/

cd $resorce_dir
cd ..


}
