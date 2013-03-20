## Parent of a WSO2 service deployment

class stratos {

## Creates a worker node by removing all unnecessary jars. Also this will remove default services of the super tenant.

	define create_worker ( $target ) {

		exec { "remove_default_deployment_from ${name}":
	              	path            => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
	              	onlyif          => "test -d ${target}/repository/deployment/server",
	              	command         => "rm -rf ${target}/repository/deployment/server/* ; 
	              			    rm -rf ${target}/repository/deployment/server/.svn";
	
	               "remove_worker_jars_from_${name}":
	              	path            => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
	              	command         => "find ${target}/repository/components/plugins \
					   -not \\( -iname \"org.wso2.carbon.cloud.gateway.agent.stub*\" \\) \
	                                   -iname \"org.wso2.carbon.*ui_*\" -o \
	                                   -iname \"org.wso2.carbon.*stub_*\" -o \
	                                   -iname \"org.wso2.stratos.*ui_*\" -o \
	                                   -iname \"org.wso2.stratos.*stub_*\" -o \
	                                   -iname \"org.jaggeryjs.*ui_*\" -o \
	                                   -iname \"org.jaggeryjs.*stub_*\" -o \
	                                   -iname \"org.wso2.carbon.ui.menu.*\" -o \
	                                   -iname \"org.wso2.*styles_*\" -o \
	                                   -iname \"org.wso2.carbon.authenticator.proxy_*\" | xargs rm -f ",
	        }
	}

## Cleans the previous deployment. If the maintenance mode is set to true, this will only kill the running service.

#	define clean ( $mode, $target, $local_dir, $version, $service ) {	
	define clean ( $mode, $target ) {	

		exec { "remove_${name}_poop":
                	path            => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/",
	                onlyif          => "test -d ${target}/repository",
        	        command         => $mode ? {
                	                        "true"  => "kill -9 `cat ${target}/wso2carbon.pid` ; /bin/echo Killed",
                        	                "false" => "kill -9 `cat ${target}/wso2carbon.pid` ; rm -rf ${target}",
                        	                "fresh" => "kill -9 `cat ${target}/wso2carbon.pid` ; rm -rf ${target} ; rm -f ${local_dir}/wso2${service}-${version}.zip",
                                	   },
		}
	}

## Initializing the deployment by placing a customized script in /opt/bin which will download and extract the pack.

	define initialize (  $version, $service, $local_dir, $target, $mode, $owner ) {

		file {  "${local_dir}/wso2${service}-${version}.zip":
			source		=> "puppet:///${service}/pack/wso2${service}-${version}.zip",
			ensure		=> present,
			require		=> [ Exec["creating_target_for_${name}"],
					     Exec["creating_local_package_repo_for_${name}"] ];
		}

		exec {  "creating_target_for_${name}":
			path    	=> ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
			command		=> "mkdir -p ${target}";

			"creating_local_package_repo_for_${name}":
			path		=> "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/",
			unless		=> "test -d ${local_dir}",
			command		=> "mkdir -p ${local_dir}";
		
#	   		"downloading_wso2${service}-${version}.zip_for_${name}":
#			path    	=> ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
#			cwd		=> $local_dir,
#			unless		=> "test -f ${local_dir}/wso2${service}-${version}.zip",
#			command		=> "wget -q ${repo}/wso2${service}-${version}.zip",
#			logoutput 	=> "on_failure",
#			creates		=> "${local_dir}/wso2${service}-${version}.zip",
#			timeout 	=> 0,
#			require		=> Exec["creating_local_package_repo_for_${name}",
#						"creating_target_for_${name}"];
		
			"extracting_wso2${service}-${version}.zip_for_${name}":
			path    	=> ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
			cwd		=> $target,
			unless		=> "test -d ${target}/wso2${service}-${version}/repository",
			command 	=> "unzip ${local_dir}/wso2${service}-${version}.zip",
			logoutput       => "on_failure",
			creates		=> "${target}/wso2${service}-${version}/repository",
                        timeout 	=> 0,
                        require 	=> File["${local_dir}/wso2${service}-${version}.zip"];

			"setting_permission_for_${name}":
			path            => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
                        cwd             => $target,
			command         => "chown -R ${owner}:${owner} ${target}/wso2${service}-${version} ;
					    chmod -R 755 ${target}/wso2${service}-${version}",
			logoutput       => "on_failure",
			timeout         => 0,
                        require 	=> Exec["extracting_wso2${service}-${version}.zip_for_${name}"];
		}
	}

## Executes the deployment by pushing all necessary configurations and patches

	define deploy ( $service, $security, $target, $owner, $group ) {
		
		file {  $target:
	                owner           => $owner,
	                group           => $group,
	                source          => ["puppet:///commons/configs/",
	                                    "puppet:///commons/patches/",
	                                    "puppet:///${service}/configs/",
	                                    "puppet:///${service}/patches/"],
        	        sourceselect    => all,
			ignore		=> ".svn",
                	ensure          => present,
	                recurse         => true;
		
		 	"${target}/bin/wso2server.sh":
                	owner   	=> $owner,
                	group   	=> $group,
                	mode    	=> 755,
			content		=> $security ? {
						"true"	=> template("${service}/wso2server.sh.erb"),
						"false"	=> template("commons/wso2server.sh.erb"),
					   },
	        	ensure  	=> present;

		#	"${target}/newrelic/newrelic.yml":
	        #        owner   	=> $owner,
	        #        group   	=> $group,
	        #        mode    	=> 755,
	        #        content 	=> template("commons/newrelic.yml.erb"),
	        #        ensure  	=> present,
	#		require		=> File[$target],
        	}
	}

## Starts the service once the deployment is successful.

	define start ( $target, $owner ) {
		exec { "strating_${name}":
			user		=> $owner,
			path            => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/",
	                command         => "${target}/bin/wso2server.sh > /dev/null 2>&1 &",
        	        creates         => "${target}/repository/wso2carbon.log",
		}
	}
}
