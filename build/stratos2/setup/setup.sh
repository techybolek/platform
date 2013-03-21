#!/usr/bin/env bash

# Die on any error:
set -e

# This script will setup the Stratos2.

SLEEP=60
export LOG=/var/log/s2/s2.log

source "./conf/setup.conf"

cc="false"
elb="false"
agent="false"
sc="false"
demo="false"
product_list=""

function help {
    echo ""
    echo "Give one or more of the servers to be setup in this machine. The available servers are"
    echo "cc, elb, agent, sc, all or demo. 'all' means you need to setup all servers in this machine."
    echo "demo means you will setup a demo server of S2 in a single physical machine which has Openstack installed."
    echo "This demo server include all S2 related packs."
    echo "usage:"
    echo "setup.sh -u<stratos2 user name> -p\"<product list>\""
    echo "eg."
    echo "sudo JAVA_HOME=/opt/jdk1.6.0_24 ./setup.sh -uwso2 -p\"cc elb\""
    echo "sudo JAVA_HOME=/opt/jdk1.6.0_24 ./setup.sh -uwso2 -p\"demo\""
    echo ""
}

while getopts u:p: opts
do
  case $opts in
    u)
        s2_user=${OPTARG}
        echo "stratos user is:$s2_user"
        ;;
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
arr=$(echo $product_list | tr " " "\n")

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
if [[ -z $s2_user || -z $product_list || $product_list = "" ]]; then
    help
    exit 1
fi

function helpsetup {
    echo ""
    echo "Set up the environment variables correctly in conf/setup.conf"
    echo ""
}

function setup_validate {

if [[ ( -z $hostname || -z $hostip ) ]]; then
    helpsetup
    exit 1
fi

if [[ $sc = "true" ]]; then
    if [[ ( -z $git_user || -z $email|| -z $s2_db_user || -z $s2_db_pass || -z $hostname
        || -z $sc_path || -z $axis2c_path ) ]]; then
        helpsetup
        exit 1
    fi
fi

if [[ $cc = "true" ]]; then
    if [[ ( -z $hostname || -z $cc_path ) ]]; then
        helpsetup
        exit 1
    fi
fi

if [[ $elb = "true" ]]; then
    if [[ ( -z $hostname || -z $elb_path ) ]]; then
        helpsetup
        exit 1
    fi
fi

if [[ $agent = "true" ]]; then
    if [[ ( -z $hostname || -z $agent_path ) ]]; then
        helpsetup
        exit 1
    fi
fi

if [[ ! -f ./$mysql_connector_jar ]]; then
    echo "Please copy the mysql connector jar into the same folder as this command(stratos2 release pack folder) and update conf/setup.conf file"
    exit 1
fi

if [[ ! -d $JAVA_HOME ]]; then
    echo "Please set the JAVA_HOME environment variable for the running user"
    exit 1
fi
export JAVA_HOME=$JAVA_HOME

if [[ ! -d $setup_dir ]]; then
    echo "Please set the parameter setup_dir to your Stratos2 download directory path in conf/setup.conf"
    echo "eg:setup_dir=/home/wso2/wso2s2-1.0.0"
    exit 1
fi

}

setup_validate

# Make sure the user is running as root.
if [ "$UID" -ne "0" ]; then
	echo ; echo "  You must be root to run $0.  (Try running 'sudo bash' first.)" ; echo 
	exit 69
fi

if [[ ! -d /var/log/s2 ]]; then
    mkdir -p /var/log/s2
fi

echo ""
echo "For all the questions asked while during executing the script please just press the enter button"
echo ""

echo "$hostip    git.$stratos_domain" >> /etc/hosts

if [[ $sc = "true" ]]; then
    if [[ ! -d $resource_path ]]; then
        cp -rf ./resources /opt/
    fi

    if [[ ! -d $script_path ]]; then
        cp -rf ./scripts /opt/
    fi

    if [[ ! -d $lib_path ]]; then
        cp -rf ./lib /opt/
    fi
    if [[ ! -d $sc_path ]]; then
        unzip ./$sc_pack -d /opt
    fi
    if [[ ! -d $axis2c_path ]]; then
        unzip ./$axis2c_pack -d /opt
    fi
fi
if [[ $elb = "true" ]]; then
    if [[ ! -d $elb_path ]]; then
        unzip ./$elb_pack -d /opt
    fi
fi
if [[ $cc = "true" ]]; then
    if [[ ! -d $cc_path ]]; then
        unzip ./$cc_pack -d /opt
    fi
fi
if [[ $agent = "true" ]]; then
    if [[ ! -d $agent_path ]]; then
        unzip ./$agent_pack -d /opt
    fi
fi

if [[ $sc = "true" ]]; then
    ##
    mysql -u${userstore_db_user} -p${userstore_db_pass} -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%'   IDENTIFIED BY '${userstore_db_pass}' WITH GRANT OPTION;flush privileges;"

    #Set up git repo creation related stuff. This will be done for demo setup only
    ###############################################################################################
    if [[ $demo = "true" ]]; then
        echo "Setup git" >> $LOG
        # create git user
        echo "Creating git user...."

        adduser git

        rm -fr /home/git/*
        rm -fr /home/$s2_user/gitolite-admin
        rm -fr /home/git/.git*
        # create ssh keypair for git
        echo "do ssh-keygen without password for git user" >> $LOG
        echo "do ssh-keygen without password for git user. press enter to continue..."
        read
        su - git -c "ssh-keygen -t rsa"

        # copy public key to $HOME
        su - git -c "cp -a /home/git/.ssh/id_rsa.pub /home/git/git.pub"

        # Install software
        apt-get install git gitolite gitweb apache2-suexec apache2 bind9 zip

        # Now execute the following command to create .gitolite.rc file
        echo "Adding entries to gitolite... Please do :wq after gitolite.rc file open. Press enter to continue... "
        read
        su - git -c "gl-setup ./git.pub"

        # Add following line in /home/git/ .gitolite.rcfile:
            echo "
        \$GL_GITCONFIG_KEYS = \"gitweb.url receive.denyNonFastforwards receive.denyDeletes\";
            " >> /home/git/.gitolite.rc


        echo "StrictHostKeyChecking no" >> /home/git/.ssh/config
        su - git -c "git clone git@localhost:gitolite-admin"

        # Do the (keygen) for stratos2 user as well and copy it to home.
        echo "do ssh-keygen without password for stratos2 user" >> $LOG
        echo "do ssh-keygen without password for stratos2 user. press enter to continue..."
        su - $s2_user -c "ssh-keygen -t rsa"
        su - $s2_user -c "cp -a /home/$s2_user/.ssh/id_rsa.pub /home/$s2_user/$s2_user.pub"

        cp -f /home/$s2_user/.ssh/id_rsa $sc_path/

        # Add $s2_user.pub key to /home/git/gitolite-admin/keydir/
        cp -f /home/$s2_user/$s2_user.pub /home/git/gitolite-admin/keydir/

        # Add some config entries in gitolite.conf file along with entry for daemon user.
        su - git -c "mkdir -p gitolite-admin/conf/repos"
        su - git -c "
            echo '
        repo    gitolite-admin
              RW+     =   git daemon
              RW+     =   $s2_user
        include \"repos/*.conf\"
            ' > ./gitolite-admin/conf/gitolite.conf"

        su - git -c "
            echo \"
        repo    testingt
              RW+     =   @all daemon
            \" > ./gitolite-admin/conf/repos/testing.conf"


        echo "Do git add,commit and push for gitolite-admin" >> $LOG
        su - git -c "cd gitolite-admin;git add conf/gitolite.conf;git add conf/repos/testing.conf;git add keydir;git config --global user.email $email;git config --global user.name $git_user;git commit -a -m \"Check in by $git_user\";git pull;git push"

        # To set stratos2 user sudo with passwordless
            echo "
        %$s2_user ALL=(ALL) NOPASSWD: ALL
            " >> /etc/sudoers

        echo "StrictHostKeyChecking no" >> /home/$s2_user/.ssh/config
        #Get git clone of the gitolite admin (@ /home/$s2_user/)
        su - $s2_user -c "git clone git@localhost:gitolite-admin"
        su - $s2_user -c "git config --global user.email \"you@example.com\""
        su - $s2_user -c "git config --global user.name \"Your Name\""
        
        
        cp -f $lib_path/mod_appfactory_svnauth.so /etc/apache2/mods-available/

        echo "LoadModule appfactory_svnauth_module mods-available/mod_appfactory_svnauth.so" > /etc/apache2/mods-available/appfactory.load

        #Now create symlink for this file in /etc/apache2/mods-enabled/appfactory.load
        if [[ ! -L /etc/apache2/mods-enabled/appfactory.load ]]; then
            ln -s /etc/apache2/mods-available/appfactory.load /etc/apache2/mods-enabled/appfactory.load
        fi

        # Copy https://svn.wso2.org/repos/wso2/scratch/hosting/products/hosting/mod_ads_gitauth-1.0.0/resources/git file 
        # into /etc/apache2/sites-available/ folder

        #Make sure that you have following entries edited correctly
        #Axis2RepoPath /opt/axis2c
        #AppfactorySVNAuthEPR https://SERVER_IP:9445/services/AuthenticationAdmin
        #CredentialCache on
        #CacheMaxAge 5
        #CacheMaxEntries 100

        cp -f ./resources/git /etc/apache2/sites-available/git.orig
        cat /etc/apache2/sites-available/git.orig | sed -e "s@IS_HOSTNAME:IS_PORT@$sc_hostname:$sc_https_port@g" | sed -e "s@STRATOS_DOMAIN@$stratos_domain@g" > /etc/apache2/sites-available/git


        echo "Now to check whether paths set to /var/www execute" >> $LOG
        /usr/lib/apache2/suexec -V

        # And also add the symlink
        if [[ ! -L /etc/apache2/mods-enabled/suexec.load ]]; then
            ln -s /etc/apache2/mods-available/suexec.load /etc/apache2/mods-enabled/suexec.load
        fi

        #Add following into /etc/apache2/apache2.conf

            echo "
        Axis2LogFile /tmp/axis2.log
        Axis2LogLevel debug
        Axis2MaxLogFileSize 10
            " >> /etc/apache2/apache2.conf

        cp -rf /usr/share/gitweb/ /var/www/
        chown -R git.git /var/www/gitweb/

        mkdir -p /var/www/bin
        cp -f ./resources/gitolite-suexec-wrapper.sh /var/www/bin/
        chown -R git.git /var/www/bin


        echo "Enable git virtual host" >> $LOG
        if [[ ! -L /etc/apache2/sites-enabled/git ]]; then
            ln -s /etc/apache2/sites-available/git /etc/apache2/sites-enabled/git
        fi

    fi # git repo setup stuff
    #End set repo setup related stuff
    ###############################################################################################


    mkdir -p /var/www/notify
    cp -f ./resources/notify.php /var/www/notify/index.php
    echo "Now restart apache2 and if start successfull you configuration upto this point is OK." >> $LOG
    echo "Now restart apache2 and if start successfull you configuration upto this point is OK."
    apachectl stop
    sleep 5
    apachectl start

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


    #Setup SC
    ##########
    echo "Setup SC" >> $LOG
    echo "Configuring the SC"

    cp -f ./config/sc/repository/conf/cartridge-config.properties $sc_path/repository/conf/
    cp -f ./config/sc/bin/wso2server.sh $sc_path/bin/
    cp -f ./config/sc/repository/conf/datasources/master-datasources.xml $sc_path/repository/conf/datasources/
    cp -f ./$mysql_connector_jar $sc_path/repository/components/lib/
    pushd $sc_path

    echo "Set mb hostname and mb port in bin/wso2server.sh." >> $LOG
    cp -f ./bin/wso2server.sh bin/wso2server.sh.orig
    cat bin/wso2server.sh.orig | sed -e "s@CC_HOSTNAME:MB_LISTEN_PORT@$cc_hostname:$mb_listen_port@g" > bin/wso2server.sh

    echo "Change CC hostname in repository/conf/cartridge-config.properties" >> $LOG

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@CC_HOSTNAME:CC_PORT@$cc_hostname:$cc_https_port@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@AGENT_HOSTNAME:AGENT_PORT@$agent_hostname:$agent_http_port@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@STRATOS_DOMAIN@$stratos_domain@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@GIT_IP@$git_ip@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@SC_HOSTNAME:SC_HTTPS_PORT@$sc_hostname:$sc_https_port@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@S2_DB_HOSTNAME:S2_DB_PORT@$s2_db_hostname:$s2_db_port@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@S2_DB_USER@$s2_db_user@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@S2_DB_PASS@$s2_db_pass@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@S2_DB_SCHEMA@$s2_db_schema@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@CC_HOSTNAME:MB_LISTEN_PORT@$cc_hostname:$mb_listen_port@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@ELB_IP@$elb_ip@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@BAM_IP@$bam_ip@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@BAM_PORT@$bam_port@g" > repository/conf/cartridge-config.properties

    cp -f repository/conf/cartridge-config.properties repository/conf/cartridge-config.properties.orig
    cat repository/conf/cartridge-config.properties.orig | sed -e "s@SCRIPT_PATH@$script_path@g" > repository/conf/cartridge-config.properties

    echo "Change mysql password in repository/conf/datasources/master-datasources.xml" >> $LOG
    cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
    cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@USERSTORE_DB_HOSTNAME@$userstore_db_hostname@g" > repository/conf/datasources/master-datasources.xml

    cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
    cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@USERSTORE_DB_PORT@$userstore_db_port@g" > repository/conf/datasources/master-datasources.xml

    cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
    cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@USERSTORE_DB_SCHEMA@$userstore_db_schema@g" > repository/conf/datasources/master-datasources.xml

    cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
    cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@USERSTORE_DB_USER@$userstore_db_user@g" > repository/conf/datasources/master-datasources.xml

    cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
    cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@USERSTORE_DB_PASS@$userstore_db_pass@g" > repository/conf/datasources/master-datasources.xml

    sed -i "s/SC_LOCAL_MEMBER_HOST/${sc_ip}/g" repository/conf/axis2/axis2.xml

    popd # sc_path




    #Database Configuration
    #######################
    echo "Create and configure MySql Database" >> $LOG

    mysql -u$userstore_db_user -p$userstore_db_pass < $resource_path/userstore.sql
    #mysql -u$userstore_db_user -p$userstore_db_pass < $resource_path/registry.sql   #registry schema is only for AF

    mysql -u$s2_db_user -p$s2_db_pass < $resource_path/s2foundation_schema.sql

    #Namespace Binding
    ##################
    echo "bind Namespaces" >> $LOG

    #Copy the https://svn.wso2.org/repos/wso2/scratch/hosting/build/tropos/resources/db.stratos.com file into /etc/bind. Edit it as necessary
    cp -f ./resources/db.stratos.com $resource_path/db.$stratos_domain
    echo "Set ELb Hostname in /etc/bind/db.stratos.com" >> $LOG
    cat $resource_path/db.$stratos_domain | sed -e "s@SC_IP@$sc_ip@g" | sed -e "s@ELB_IP@$elb_ip@g" | sed -e "s@STRATOS_DOMAIN@$stratos_domain@g" > /etc/bind/db.$stratos_domain

    echo "Add the following content to /etc/bind/named.conf.local" >> $LOG
    echo "zone \"$stratos_domain\" {" >> /etc/bind/named.conf.local
    echo "      type master;" >> /etc/bind/named.conf.local
    echo "      file \"/etc/bind/db.$stratos_domain\";" >> /etc/bind/named.conf.local
    echo "};" >> /etc/bind/named.conf.local

    #Copy https://svn.wso2.org/repos/wso2/scratch/hosting/build/tropos/resources/append_zone_file.sh into /opt/scripts folder
    cp -f ./scripts/add_entry_zone_file.sh $script_path/
    cp -f ./scripts/remove_entry_zone_file.sh $script_path/


    chmod 600 $sc_path/id_rsa
    chown $s2_user.$s2_user $sc_path/id_rsa
    echo "End configuring the SC"
fi #End SC server installation





if [[ $cc = "true" ]]; then
    #Setup CC
    #########
    echo "Setup CC" >> $LOG
    echo "Configuring the Cloud Controller"
    if [[ $demo = "true" ]]; then
        cp -f /home/$s2_user/.ssh/id_rsa $cc_path/
    else
        echo "Copy the /home/$s2_user/.ssh/id_rsa in SC host machine into $cc_path"
        echo "When you are ready press any key to continue"
        read
    fi
    echo "Creating payload directory ... " >> $LOG
    mkdir -p $cc_path/repository/resources/payload

    # Copy the cartridge specific configuration files into the CC
    if [ "$(find ./cartridges/ -type f)" ]; then
        cp -f ./cartridges/*.xml $cc_path/repository/deployment/server/cartridges/
    fi
    if [ "$(find ./cartridges/payload/ -type f)" ]; then
        cp -f ./cartridges/payload/*.txt $cc_path/repository/resources/payload/
    fi
    if [ "$(find ./cartridges/services/ -type f)" ]; then
        cp -f ./cartridges/services/*.xml $cc_path/repository/deployment/server/services/
    fi

    cp -f ./config/cc/repository/conf/cloud-controller.xml $cc_path/repository/conf/
    cp -f ./config/cc/repository/conf/carbon.xml $cc_path/repository/conf/

    #MB specific file copying
    cp -f ./config/cc/repository/conf/advanced/qpid-virtualhosts.xml $cc_path/repository/conf/advanced/
    cp -f ./config/cc/repository/conf/carbon.xml $cc_path/repository/conf/
    #End MB specific file copying

    pushd $cc_path



    echo "Set openstack controller ip in repository/conf/cloud-controller.xml" >> $LOG



    #   <topologySync enable="true">
    #       <!-- MB server info -->
    #       <mbServerUrl>{IP}:5672</mbServerUrl>
    #       <cron>1 * * * * ? *</cron>
    #   </topologySync>
    #
    #<iaasProvider type="openstack" name="openstack specific details">
    #          <className>org.wso2.carbon.stratos.cloud.controller.iaases.OpenstackNovaIaas</className>
    #                      <provider>openstack-nova</provider>
    #                      <identity svns:secretAlias="elastic.scaler.openstack.identity">demo:demo</identity>
    #                      <credential svns:secretAlias="elastic.scaler.openstack.credential">openstack</credential>
    #                      <property name="jclouds.endpoint" value="http://192.168.16.20:5000/" />
    #          <property name="jclouds.openstack-nova.auto-create-floating-ips" value="false"/>
    #                      <property name="jclouds.api-version" value="2.0/" />
    #                      <scaleUpOrder>2</scaleUpOrder>
    #                      <scaleDownOrder>3</scaleDownOrder>
    #                      <property name="X" value="x" />
    #                      <property name="Y" value="y" />
    #                      <imageId>nova/dab37f0e-cf6f-4812-86fc-733acf22d5e6</imageId>
    #              </iaasProvider>
    #      </iaasProviders>


    

    
    
    
# Demo specific stuff

if [[ $demo = "true" ]]; then
  
    cp -f repository/conf/cloud-controller.xml repository/conf/cloud-controller.xml.orig
    cat repository/conf/cloud-controller.xml.orig | sed -e "s@<identity svns:secretAlias=\"elastic.scaler.openstack.identity\">*.*</identity>@<identity svns:secretAlias=\"elastic.scaler.openstack.identity\">$nova_projectid:$nova_user</identity>@g" > repository/conf/cloud-controller.xml

    cp -f repository/conf/cloud-controller.xml repository/conf/cloud-controller.xml.orig
    cat repository/conf/cloud-controller.xml.orig | sed -e "s@<credential svns:secretAlias=\"elastic.scaler.openstack.credential\">*.*</credential>@<credential svns:secretAlias=\"elastic.scaler.openstack.credential\">$nova_pass</credential>@g" > repository/conf/cloud-controller.xml


echo "Change mysql image id in repository/deployment/server/cartridges/mysql.xml" >> $LOG
#<iaasProvider type="openstack" >
#              <imageId>nova/d6e5dbe9-f781-460d-b554-23a133a887cd</imageId>
#              <property name="keyPair" value="stratos-demo"/>
#              <property name="instanceType" value="nova/1"/>
#              <property name="securityGroups" value="default"/>
#              <!--<property name="payload" value="resources/as.txt"/>-->
#          </iaasProvider>
 

cp -f repository/deployment/server/cartridges/mysql.xml repository/deployment/server/cartridges/mysql.xml.orig
cat repository/deployment/server/cartridges/mysql.xml.orig | sed -e "s@<property name=\"keyPair\" value=\"*.*\"/>@<property name=\"keyPair\" value=\"$keypair\"/>@g" > repository/deployment/server/cartridges/mysql.xml

cp -f repository/deployment/server/cartridges/mysql.xml repository/deployment/server/cartridges/mysql.xml.orig
cat repository/deployment/server/cartridges/mysql.xml.orig | sed -e "s@<property name=\"instanceType\" value=\"*.*\"/>@<property name=\"instanceType\" value=\"$instance_type\"/>@g" > repository/deployment/server/cartridges/mysql.xml

cp -f repository/deployment/server/cartridges/mysql.xml repository/deployment/server/cartridges/mysql.xml.orig
cat repository/deployment/server/cartridges/mysql.xml.orig | sed -e "s@<property name=\"securityGroup\" value=\"*.*\"/>@<property name=\"securityGroup\" value=\"$security_group\"/>@g" > repository/deployment/server/cartridges/mysql.xml

cp -f repository/deployment/server/cartridges/mysql.xml repository/deployment/server/cartridges/mysql.xml.orig
cat repository/deployment/server/cartridges/mysql.xml.orig | sed -e "s@<imageId>*.*</imageId>@<imageId>nova/$mysql_image_id</imageId>@g" > repository/deployment/server/cartridges/mysql.xml

cp -f repository/deployment/server/cartridges/mysql.xml repository/deployment/server/cartridges/mysql.xml.orig
cat repository/deployment/server/cartridges/mysql.xml.orig | sed -e "s@STRATOS_DOMAIN@$stratos_domain@g" > repository/deployment/server/cartridges/mysql.xml



echo "Change php image id in repository/deployment/server/cartridges/php.xml" >> $LOG
#<iaasProvider type="openstack" >
#              <imageId>nova/250cd0bb-96a3-4ce8-bec8-7f9c1efea1e6</imageId>
#              <property name="keyPair" value="stratos-demo"/>
#              <property name="instanceType" value="nova/1"/>
#              <property name="securityGroups" value="default"/>
#              <!--<property name="payload" value="resources/as.txt"/>-->
#          </iaasProvider>

cp -f repository/deployment/server/cartridges/php.xml repository/deployment/server/cartridges/php.xml.orig
cat repository/deployment/server/cartridges/php.xml.orig | sed -e "s@<property name=\"keyPair\" value=\"*.*\"/>@<property name=\"keyPair\" value=\"$keypair\"/>@g" > repository/deployment/server/cartridges/php.xml

cp -f repository/deployment/server/cartridges/php.xml repository/deployment/server/cartridges/php.xml.orig
cat repository/deployment/server/cartridges/php.xml.orig | sed -e "s@<property name=\"instanceType\" value=\"*.*\"/>@<property name=\"instanceType\" value=\"$instance_type\"/>@g" > repository/deployment/server/cartridges/php.xml

cp -f repository/deployment/server/cartridges/php.xml repository/deployment/server/cartridges/php.xml.orig
cat repository/deployment/server/cartridges/php.xml.orig | sed -e "s@<property name=\"securityGroup\" value=\"*.*\"/>@<property name=\"securityGroup\" value=\"$security_group\"/>@g" > repository/deployment/server/cartridges/php.xml

cp -f repository/deployment/server/cartridges/php.xml repository/deployment/server/cartridges/php.xml.orig
cat repository/deployment/server/cartridges/php.xml.orig | sed -e "s@<imageId>*.*</imageId>@<imageId>nova/$php_image_id</imageId>@g" > repository/deployment/server/cartridges/php.xml

cp -f repository/deployment/server/cartridges/php.xml repository/deployment/server/cartridges/php.xml.orig
cat repository/deployment/server/cartridges/php.xml.orig | sed -e "s@STRATOS_DOMAIN@$stratos_domain@g" > repository/deployment/server/cartridges/php.xml

echo "Change image id in repository/deployment/server/cartridges/as1.xml" >> $LOG

#<iaasProvider type="openstack" >
#              <imageId>nova/d6e5dbe9-f781-460d-b554-23a133a887cd</imageId>
#              <property name="keyPair" value="stratos-demo"/>
#              <property name="instanceType" value="nova/1"/>
#              <property name="securityGroups" value="default"/>
#              <!--<property name="payload" value="resources/as.txt"/>-->
#          </iaasProvider>

cp -f repository/deployment/server/cartridges/as1.xml repository/deployment/server/cartridges/as1.xml.orig
cat repository/deployment/server/cartridges/as1.xml.orig | sed -e "s@<imageId>*.*</imageId>@<imageId>nova/$appserver_image_id</imageId>@g" > repository/deployment/server/cartridges/as1.xml

cp -f repository/deployment/server/cartridges/as1.xml repository/deployment/server/cartridges/as1.xml.orig
cat repository/deployment/server/cartridges/as1.xml.orig | sed -e "s@<property name=\"instanceType\" value=\"*.*\"/>@<property name=\"instanceType\" value=\"$as_instance_type\"/>@g" > repository/deployment/server/cartridges/as1.xml


sed -i "s/AS_CARTRIDGE_TYPE/${appserver_cartridge_type}/g" repository/deployment/server/cartridges/as1.xml
sed -i "s/AS_SERVICE_HOST/${appserver_service_host}/g" repository/deployment/server/cartridges/as1.xml

echo "Set AS service and payload" >> $LOG

sed -i "s/AS_SERVICE_HOST/${appserver_service_host}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/AS_SERVICE_DOMAIN/${appserver_service_domain}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_ELBHOST/${elb_ip}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_ELBPORT/${elb_cluster_port}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_SCHOST/${sc_ip}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_SC_CLUSTER_PORT/${sc_cluster_port}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_SCPORT/${sc_https_port}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_GITHOSTNAME/git.${stratos_domain}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_GITIP/${git_ip}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_USERSTORE_DBHOST/${hostip}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_USERSTORE_DB_PORT/${userstore_db_port}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_USERSTORE_USERNAME/${userstore_db_user}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_USERSTORE_PASSWORD/${userstore_db_pass}/g" repository/resources/payload/appserver_as_001.txt
sed -i "s/PAYLOAD_CARTRIDGE_ALIAS/${appserver_cartridge_type}/g" repository/resources/payload/appserver_as_001.txt

#copy key
echo "Copying keys.." >> $LOG
cp -f id_rsa repository/resources/payload/id_rsa
pushd repository/resources/payload
cp -f appserver_as_001.txt launch-params
zip -r appserver_as_zip_001.zip id_rsa launch-params
popd #payload dir


sed -i "s/AS_SERVICE_DOMAIN/${appserver_service_domain}/g" repository/deployment/server/services/appserver1_as.xml
sed -i "s/AS_SERVICE_SUBDOMAIN/${appserver_service_subdomain}/g" repository/deployment/server/services/appserver1_as.xml
sed -i "s/TENANT_RANGE/${tenant_range}/g" repository/deployment/server/services/appserver1_as.xml
sed -i "s/AS_CARTRIDGE_TYPE/${appserver_cartridge_type}/g" repository/deployment/server/services/appserver1_as.xml
sed -i "s/AS_SERVICE_HOST/${appserver_service_host}/g" repository/deployment/server/services/appserver1_as.xml
sed -i "s/AS_PAYLOAD_PATH/${appserver_payload_path}/g" repository/deployment/server/services/appserver1_as.xml


fi # End Demo specific stuff




    cp -f repository/conf/cloud-controller.xml repository/conf/cloud-controller.xml.orig
    cat repository/conf/cloud-controller.xml.orig | sed -e "s@<property name=\"jclouds.endpoint\" value=\"*.*\" />@<property name=\"jclouds.endpoint\" value=\"http://$nova_controller_hostname:5000/\" />@g" > repository/conf/cloud-controller.xml

    cp -f repository/conf/cloud-controller.xml repository/conf/cloud-controller.xml.orig
    cat repository/conf/cloud-controller.xml.orig | sed -e "s@CC_HOSTNAME:MB_LISTEN_PORT@$cc_hostname:$mb_listen_port@g" > repository/conf/cloud-controller.xml

    echo "In repository/conf/carbon.xml"
    cp -f repository/conf/carbon.xml repository/conf/carbon.xml.orig
    cat repository/conf/carbon.xml.orig | sed -e "s@BAM_HOSTNAME:BAM_PORT@$bam_hostname:$bam_port@g" > repository/conf/carbon.xml

    #Before starting CC we need to delete
    #rm ./repository/conf/service-topology.conf
    #rm ./repository/conf/service-topology.conf.back


    #Setup MB
    #########
    echo "Setup MB" >> $LOG
    echo "Set settings in cc/repository/conf/advanced/qpid-virtualhosts.xml" >> $LOG
    cp -f repository/conf/advanced/qpid-virtualhosts.xml repository/conf/advanced/qpid-virtualhosts.xml.orig
    cat repository/conf/advanced/qpid-virtualhosts.xml.orig | sed -e "s@MB_CASSANDRA_PORT@$mb_cassandra_port@g" > repository/conf/advanced/qpid-virtualhosts.xml

    echo "Set settings in mb/repository/conf/carbon.xml" >> $LOG
    cp -f repository/conf/carbon.xml repository/conf/carbon.xml.orig
    cat repository/conf/carbon.xml.orig | sed -e "s@OFFSET@$cc_port_offset@g" > repository/conf/carbon.xml
    #Before starting sc delete rm -rf tmp/ at mb root folder
    rm -rf ./tmp


    popd #cc_path
    echo "End configuring the Cloud Controller"
fi



if [[ $elb = "true" ]]; then
    #Setup ELB
    ##########
    echo "Setup ELB" >> $LOG
    echo "Configuring the ELB"

    cp -f ./config/elb/repository/conf/loadbalancer.conf $elb_path/repository/conf/
    cp -f ./config/elb/repository/conf/axis2/axis2.xml $elb_path/repository/conf/axis2/
    cp -f ./config/elb/repository/conf/datasources/master-datasources.xml $elb_path/repository/conf/datasources/
    cp -f ./$mysql_connector_jar $elb_path/repository/components/lib/

    pushd $elb_path
    #If autoscaling enabled
    echo "Set CC host and port in repository/conf/loadbalancer.conf" >> $LOG
    # autoscaler_service_epr  https://CC_HOSTNAME:CC_PORT/services/CloudControllerService;
    cp -f repository/conf/loadbalancer.conf repository/conf/loadbalancer.conf.orig
    cat repository/conf/loadbalancer.conf.orig | sed -e "s@CC_HOSTNAME:CC_PORT@$cc_hostname:$cc_https_port@g" > repository/conf/loadbalancer.conf

    cp -f repository/conf/loadbalancer.conf repository/conf/loadbalancer.conf.orig
    cat repository/conf/loadbalancer.conf.orig | sed -e "s@ENABLE_AUTOSCALER@$enable_autoscaler@g" > repository/conf/loadbalancer.conf

    cp -f repository/conf/loadbalancer.conf repository/conf/loadbalancer.conf.orig
    cat repository/conf/loadbalancer.conf.orig | sed -e "s@CC_HOSTNAME:MB_LISTEN_PORT@$cc_hostname:$mb_listen_port@g" > repository/conf/loadbalancer.conf

    echo "Set hostname of the machine where elb run, in repository/conf/axis2/axis2.xml" >> $LOG
    #<!--parameter name="localMemberHost">ELB_HOSTNAME</parameter-->
    cp -f repository/conf/axis2/axis2.xml repository/conf/axis2/axis2.xml.orig
    cat repository/conf/axis2/axis2.xml.orig | sed -e "s@ELB_HOSTNAME@$elb_hostname@g" > repository/conf/axis2/axis2.xml


    echo "Set hostname of the machine where elb run, in repository/conf/etc/jmx.xml" >> $LOG
    cp -f repository/conf/etc/jmx.xml repository/conf/etc/jmx.xml.orig
    cat repository/conf/etc/jmx.xml.orig | sed -e "s@ELB_HOSTNAME@$elb_hostname@g" > repository/conf/etc/jmx.xml


    echo "Change mysql password in repository/conf/datasources/master-datasources.xml" >> $LOG
    cp -f ./repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
    cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@USERSTORE_DB_HOSTNAME@$userstore_db_hostname@g" | sed -e "s@USERSTORE_DB_PORT@$userstore_db_port@g"| sed -e "s@USERSTORE_DB_SCHEMA@$userstore_db_schema@g"|sed -e "s@USERSTORE_DB_USER@$userstore_db_user@g" |sed -e "s@USERSTORE_DB_PASS@$userstore_db_pass@g" > repository/conf/datasources/master-datasources.xml

    popd #elb_path
    echo "End configuring the ELB"
fi



if [[ $agent = "true" ]]; then
    #Setup Agent
    ############
    echo "Setup Agent" >> $LOG
    echo "Configuring the Agent"
    cp -f ./config/agent/conf/agent.properties $agent_path/conf/
    pushd $agent_path

    rm -rf registrants/

    #Set agent host and ELb host in conf/agent.properties.
    #Note that loadBalancerDomain=wso2.carbon.lb.domain should be same as elb/repository/conf/axis2/axis2.xml
    #<parameter name="domain">wso2.carbon.lb.domain</parameter>
    echo "Set agent hostname in conf/agent.properties." >> $LOG
    cp -f ./conf/agent.properties conf/agent.properties.orig
    cat conf/agent.properties.orig | sed -e "s@AGENT_HOSTNAME@$agent_hostname@g" > conf/agent.properties

    cp -f ./conf/agent.properties conf/agent.properties.orig
    cat conf/agent.properties.orig | sed -e "s@AGENT_CLUSTERING_PORT@$agent_clustering_port@g" > conf/agent.properties

    echo "Set ELB hostname in conf/agent.properties." >> $LOG
    cp -f ./conf/agent.properties conf/agent.properties.orig
    cat conf/agent.properties.orig | sed -e "s@ELB_HOSTNAME@$elb_hostname@g" > conf/agent.properties

    echo "Set SC_PATH in conf/agent.properties." >> $LOG
    cp -f ./conf/agent.properties conf/agent.properties.orig
    cat conf/agent.properties.orig | sed -e "s@SC_PATH@$sc_path@g" > conf/agent.properties

    echo "Set SC_HOST in conf/agent.properties." >> $LOG
    cp -f ./conf/agent.properties conf/agent.properties.orig
    cat conf/agent.properties.orig | sed -e "s@SC_HOSTNAME@$sc_hostname@g" > conf/agent.properties

    echo "Set SC_HTTPS_PORT in conf/agent.properties." >> $LOG
    cp -f ./conf/agent.properties conf/agent.properties.orig
    cat conf/agent.properties.orig | sed -e "s@SC_HTTPS_PORT@$sc_https_port@g" > conf/agent.properties


    popd #agent_path
    echo "End configuring the Agent"
fi









#Starting the servers
#####################
echo "Starting the servers" >> $LOG
#Starting the servers in the following order is recommended
#mb, cc, elb, is, agent, sc

chown $s2_user:$s2_user -R /opt

echo "Starting up servers. This may take time. Look at $LOG file for server startup details"

chown -R $s2_user.$s2_user /var/log/s2
chmod -R 777 /var/log/s2

su - $s2_user -c "source $setup_dir/conf/setup.conf;$setup_dir/start_server.sh -p$product_list >> $LOG"

echo "Servers started. Please look at $LOG file for server startup details"
if [[ $sc == "true" ]]; then
    echo "**************************************************************"
    echo "Management Console : https://$HOSTNAME:$sc_https_port/"
    echo "**************************************************************"
fi

