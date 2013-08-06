 #!/bin/bash
function setup_af {
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
pack_dir=$working_dir/appfactory/
mkdir -p $pack_dir
echo "Setting up Appfactory........"
/usr/bin/unzip  -q $resorce_dir/packs/wso2appfactory-${version}.zip   -d $pack_dir
APPFACTORY_HOME=$pack_dir/wso2appfactory-${version}
cp $resorce_dir/configs/wso2server.sh $APPFACTORY_HOME/bin
cat $resorce_dir/configs/appfactory.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/appfactory/appfactory.xml
cp $resorce_dir/configs/appfactory-user-mgt.xml $APPFACTORY_HOME/repository/conf/user-mgt.xml
cp $resorce_dir/configs/appfactory-registry.xml $APPFACTORY_HOME/repository/conf/registry.xml
cat $resorce_dir/configs/appfactory-carbon.xml | sed -e "s@AF_HOST@$af_host_name@g"  | sed -e "s@OFFSET@$offset@g" > $APPFACTORY_HOME/repository/conf/carbon.xml
cat $resorce_dir/configs/appfactory-axis2.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/axis2/axis2.xml
cat $resorce_dir/configs/appfactory-confirmation-email-config.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/email/confirmation-email-config.xml
cat $resorce_dir/configs/appfactory-invite-user-email-config.xml | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/email/invite-user-email-config.xml
cat $resorce_dir/configs/appfactory-sso-idp-config.xml  | sed -e "s@AF_HOST@$af_host_name@g" > $APPFACTORY_HOME/repository/conf/sso-idp-config.xml
cp $resorce_dir/configs/saml2.federation.properties  $APPFACTORY_HOME/repository/conf/security
cp $resorce_dir/configs/appfactory-humantask.xml $APPFACTORY_HOME/repository/conf/humantask.xml
cp $resorce_dir/configs/carbon-console-web.xml $APPFACTORY_HOME/repository/conf/tomcat/carbon/WEB-INF/web.xml
#cp -r $resorce_dir/patches/af/*  $APPFACTORY_HOME/repository/components/patches
#cp -r $resorce_dir/CreateTenant.zip  $APPFACTORY_HOME/repository/deployment/server/bpel/
#cp -r $resorce_dir/appmgt  $APPFACTORY_HOME/repository/deployment/server/jaggeryapps/
cp -r $resorce_dir/configs/endpoints  $APPFACTORY_HOME/repository/conf/appfactory

#cp resources/org.wso2.carbon.appfactory.apiManager.integration-1.0.2.jar $APPFACTORY_HOME/repository/components/plugins/org.wso2.carbon.appfactory.apiManager.integration_1.0.2.jar
#this is required for fast app page load
cp $resorce_dir/configs/tenant-mgt.xml $APPFACTORY_HOME/repository/conf/tenant-mgt.xml


cp $resorce_dir/lib/mysql-connector-java-5.1.12-bin.jar $APPFACTORY_HOME/repository/components/lib

mkdir  -p $APPFACTORY_HOME/mvn-tmp
cd $APPFACTORY_HOME/mvn-tmp
mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/af-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.archetype -DartifactId=af-archetype -Dversion=1.0.0 -Dpackaging=jar > /dev/null
mvn archetype:generate -DartifactId=afdefault -DgroupId=org.wso2.af -DarchetypeArtifactId=maven-archetype-webapp -Dversion=SNAPSHOT -DinteractiveMode=false  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/jaxrs-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.jaxrsarchetype -DartifactId=jaxrs-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null
mvn archetype:generate -DartifactId=jaxrsdefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.jaxrsarchetype -DarchetypeArtifactId=jaxrs-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local   > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/jaxws-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.jaxwsarchetype -DartifactId=jaxws-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null
mvn archetype:generate -DartifactId=jaxwsdefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.jaxwsarchetype -DarchetypeArtifactId=jaxws-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/jaggery-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.jaggeryarchetype -DartifactId=jaggery-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null

mvn archetype:generate -DartifactId=jaggerydefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.jaggeryarchetype -DarchetypeArtifactId=jaggery-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/bpel-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.bpelarchetype -DartifactId=bpel-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null

mvn archetype:generate -DartifactId=bpeldefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.bpelarchetype -DarchetypeArtifactId=bpel-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null

mvn install:install-file -Dfile=$APPFACTORY_HOME/repository/resources/maven/dbs-archetype-1.0.0.jar -DgroupId=org.wso2.carbon.appfactory.maven.dbsarchetype -DartifactId=dbs-archetype -Dversion=1.0.0 -Dpackaging=jar  > /dev/null

mvn archetype:generate -DartifactId=dbsdefault -DarchetypeGroupId=org.wso2.carbon.appfactory.maven.dbsarchetype -DarchetypeArtifactId=dbs-archetype -DarchetypeVersion=1.0.0 -DgroupId=org.wso2.af -Dversion=SNAPSHOT -DinteractiveMode=false -DarchetypeCatalog=local  > /dev/null
cd $resorce_dir
cd ..


}
