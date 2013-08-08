#!/bin/bash
function setup_git_server {
unset OPTIND
while getopts w:r:v:h:o: option
do
        case "${option}"
        in
                w) working_dir=${OPTARG};;
                r) resorce_dir=${OPTARG};;
                v) version=$OPTARG;;
                h) af_host_name=$OPTARG;;
                o) offset=$OPTARG;;
        esac
done

GITBLIT_HOME=$working_dir/gitblit

. `pwd`/setup.conf

#config gitblit
echo "Setting up Gitblit ........"
mkdir $working_dir/gitblit

echo "[Gitblit] extracting archive"
tar -xf $resorce_dir/packs/gitblit-${version}.tar.gz -C $working_dir/gitblit

cat $resorce_dir/configs/gitblit.properties | sed -e "s@APPFACTORY_HOME@$APPFACTORY_HOME@g" | sed -e "s@APPFACTORY_HOST@$af_host_name@g" | sed -e "s@AF_HTTPS_PORT@$AF_HTTPS_PORT@g" | sed -e "s@JENKINS_HOST@$JENKINS_HOST@g" | sed -e "s@JENKINS_HTTP_PORT@$JENKINS_HTTP_PORT@g" > $GITBLIT_HOME/data/gitblit.properties

cat $resorce_dir/configs/jenkins.groovy | sed -e "s@JENKINS_HOST@$JENKINS_HOST@g" | sed -e "s@JENKINS_HTTP_PORT@$JENKINS_HTTP_PORT@g" > $GITBLIT_HOME/data/groovy/jenkins.groovy

#copying the jar files
cp $resorce_dir/lib/appfactory.gitblit.plugin-0.0.1-jar-with-dependencies.jar $GITBLIT_HOME/ext

echo "[Gitblit] Configuration done."
}



