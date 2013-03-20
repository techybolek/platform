class stratos::mb ( $version, 
		   $offset=0, 
		   $css_port=4000, 
		  # $tribes_port=4000, 
		  # $config_db=governance, 
		    $maintenance_mode=true, 
		  # $depsync=false, 
		  # $sub_cluster_domain=mgt,
		   $owner=root,
		   $group=root,
		   $target="/mnt" ) {
	
	$deployment_code	= "messaging"

	$stratos_version 	= $version
	$service_code 		= "mb"
	$carbon_home		= "${target}/wso2${service_code}-${stratos_version}"

	$service_templates 	= ["conf/carbon.xml","conf/advanced/qpid-virtualhosts.xml"]

	tag ($service_code)

        define push_templates ( $directory, $target ) {
        
                file { "${target}/repository/${name}":
                        owner   => $owner,
                        group   => $group,
                        mode    => 755,
                        content => template("${directory}/${name}.erb"),
                        ensure  => present,
                }
        }

	clean { $deployment_code:
		mode		=> $maintenance_mode,
                target          => $carbon_home,
	}

	initialize { $deployment_code:
#		repo		=> $package_repo,
		version         => $stratos_version,
		mode		=> $maintenance_mode,
		service		=> $service_code,
		local_dir       => $local_package_dir,
		owner		=> $owner,
		target   	=> $target,
		require		=> Stratos::Clean[$deployment_code],
	}

	deploy { $deployment_code:
		service		=> $service_code,	
		security	=> "false",
		owner		=> $owner,
		group		=> $group,
		target		=> $carbon_home,
		require		=> Stratos::Initialize[$deployment_code],
	}

	#if $sub_cluster_domain == "worker" {
	#	create_worker { $deployment_code:
	#		target	=> $carbon_home,
	#		require	=> Stratos::Deploy[$deployment_code],
	#	}
	#}	
	
	push_templates { 
		$service_templates: 
		target		=> $carbon_home,
		directory 	=> $service_code,
		require 	=> Stratos::Deploy[$deployment_code];
	}

	start { $deployment_code:
		owner		=> $owner,
                target          => $carbon_home,
		require		=> [ Stratos::Initialize[$deployment_code],
				     Stratos::Deploy[$deployment_code],
				     Push_templates[$service_templates],
		#		     Push_templates[$common_templates], 
				   ],
	}
}

