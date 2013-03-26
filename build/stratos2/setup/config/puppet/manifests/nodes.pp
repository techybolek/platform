stage { 'configure': require => Stage['main'] }
stage { 'deploy': require => Stage['configure'] }

node basenode {
				$depsync_svn_repo 	= "https://sn.stratoslive.wso2.com/wso2/repo/slive/"
				$local_package_dir	= "/mnt/packs/"
				$deploy_new_packs	= "true"
}

node confignode inherits basenode  {
				## Service subdomains
				$stratos_domain 	= "stratoslive.wso2.com"
				$as_subdomain 		= "appserver"
				$mb_subdomain 		= "messaging"
				$governance_subdomain	= "governance"
				$management_subdomain   = "mgt"
				$bam_subdomain          = "monitor"
				$bps_subdomain          = "process"
				$brs_subdomain          = "rule"
				$cep_subdomain          = "cep"
				$dss_subdomain          = "data"
				$esb_subdomain          = "esb"
				$gs_subdomain           = "gadget"
				$cg_subdomain           = "cloudgateway"
				$ts_subdomain           = "task"
				$is_subdomain           = "identity"
				$ms_subdomain           = "mashup"
				$ss_subdomain           = "storage"
				$am_subdomain           = "api"
				$cc_subdomain		= "cc"
				$sc_subdomain		= "sc"
				$git_subdomain		= "git"
				$elb_subdomain		= "elb"


				## SC configs
				$git_host_ip		= "10.68.202.233"
				$sc_script_path		= ""

				## ADC
				$adc_host			= "sc.wso2.com"
				$adc_port			= "9443"
				$adc_tribes_port	= "5001"
				$adc_mysql_host		= "sc.wso2.com"
				$adc_mysql_username	= "root"
				$adc_mysql_password	= "root123"
				$adc_mysql_db		= "s2_foundation"
				$adc_truststore_path	= "/opt/wso2scc-1.0.0/repository/resources/security/wso2carbon.jks"

				$elb_ip			= "192.168.4.150"
				$bam_ip			= "192.168.4.150"
				$bam_port		= "7771"

				## CC configs
				$cc_https_port		= "9444"

				# IaaS settings
				$iaas_scaler_identity	= "AKIAJDWG6MI42UHQP55Q"
				$iaas_scaler_credentials= "IQEjQfolrbHtPBCmMWMBK59tgTH+vXY1tmPTaWRF"
				$ec2_owner_id		= "6109-6823-6798"
				$ec2_availability_zone	= "us-east-1c"
				
				# comma seperated string "s2slive-all,default"
				$ec2_security_groups	= "S2SLive-ALL"
				$ec2_instance_type	= "m1.large"
				$ec2_keypair		= "S2SLive-keypair"
				$ec2_image_id		= "us-east-1/ami-b341"

				$cloudcontroller_identity= "demo:demo"
				$cloudcontrlller_creds	= "openstack"
				$jclouds_endpoint	= "http://192.168.16.20:5000/"
				$jcoulds_floating_ips	= "false"
				$openstack_image_id	= "nova/dab37f0e-cf6f-4812-86fc-733acf22d5e6"

				##MB config
				$mb_port		= "5673"



				## Server details for billing
				$time_zone		= "GMT-8:00"

				## Userstore MySQL server configuration details
				$mysql_server_1         = "mysql1.stratoslive.wso2.com"
				$mysql_server_2         = "mysql1.stratoslive.wso2.com"
				$mysql_port             = "3306"
				$max_connections        = "100000"
				$max_active             = "150"
				$max_wait               = "360000"
	
				## User store config Database detilas
				$registry_user          = "registry"
				$registry_password      = "registry123"
				$registry_database      = "governance"

				$rss_database           = "rss_db"
				$rss_user               = "rss_db"
				$rss_password           = "rss123"
				$rss_instance_user      = "wso2admin"
				$rss_instance_password  = "wso2admin123"


				$userstore_user         = "userstore"
				$userstore_password     = "userstore123"
				$userstore_database     = "userstore"

				$billing_user           = "billing"
				$billing_password       = "billing123"
				$billing_database       = "billing"
				$billing_datasource     = "WSO2BillingDS"

				## Cassandra details
				$css0_subdomain         = "node0.cassandra"
				$css1_subdomain         = "node1.cassandra"
				$css2_subdomain         = "node2.cassandra"
				$css_cluster_name       = "Stratos Dev Setup"
				$css_port               = "9160"
				$cassandra_username     = "cassandra"
				$cassandra_password     = "cassandra123"
				$hdfs_url               = "hadoop0"
				$hdfs_port              = "9000"
				$hdfs_job_tracker_port  = "9001"

				$super_admin_email      = "sanjaya@wso2.com"
				$notification_email     = "damitha@wso2.com"
				$finance_email          = "amilam@wso2.com"
				$stratos_admin_user     = "stratos"
				$stratos_admin_password = "stratos123"

				## LOGEVENT configurations
				$receiver_url           = "receiver.stratoslive.wso2.com"
				$receiver_port          = "7617"
				$receiver_secure_port   = "7717"
				$receiver_username      = "cassandra"
				$receiver_password      = "cassandra123"

				## Deployment synchronizer
				$repository_type 	= "svn"
				$svn_user 		= "wso2"
				$svn_password 		= "wso2123"	



}

### PUPPET-DEV STRATOS NODES IN LK VMs ####

node 'puppet.novalocal' inherits confignode {
		$server_ip 	= $ipaddress

## Automatic failover
#        $virtual_ip     = "192.168.4.250"
#        $interface      = "eth0"
#        $check_interval = "2"
#        $priority       = "100"
#        $state          = "MASTER"

		include system_config



		class {"stratos::mb":								
				version            => "2.0.1",
				offset             => 1,
				css_port            => 9161,
				maintenance_mode   => "true",
				owner              => "ubuntu",
				group              => "ubuntu",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
		}

		class {"stratos::cc":
				version            => "1.0.0",
				offset             => 4,
				mb_host		   => "localhost",
				mb_port		   => "5673",
				maintenance_mode   => "true",
				owner              => "ubuntu",
				group              => "ubuntu",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
		}
}


node 'php.stratoslive.wso2.com'{

		class {"php_cartridge":
				syslog            => "syslog:local2",
				docroot		  => "/var/www/www",
			  	samlalias	  => "/var/www/simplesamlphp/www",
		}

}


node 'mgt.appserver.stratoslive.wso2.com' inherits confignode{
		$server_ip      = $ec2_local_ipv4

		include system_config

		class {"stratos::appserver":

				version            => "5.0.2",
				offset             => 1,
				tribes_port        => 4100,
				config_db          => "appserver_config",
				maintenance_mode   => "false",
				depsync            => "true",
				sub_cluster_domain => "mgt",
				owner              => "kurumba",
				group              => "kurumba",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
				repository_type    => "git",
				cartridge_type	   => "appserver",
		}

}

node 'worker.appserver.stratoslive.wso2.com' inherits confignode{
		$server_ip      = $ec2_local_ipv4

		include system_config

		class {"stratos::appserver":
				version            => "5.0.2",
				offset             => 1,
				tribes_port        => 4100,
				config_db          => "appserver_config",
				maintenance_mode   => "false",
				depsync            => "true",
				sub_cluster_domain => "worker",
				owner              => "kurumba",
				group              => "kurumba",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
				repository_type    => "git",
				cartridge_type	   => "appserver",

		}

}

node 'esb.stratoslive.wso2.com' inherits confignode{
		$server_ip      = $ec2_local_ipv4

		include system_config

		class {"stratos::esb":
				version            => "4.6.0",
				offset             => 2,
				tribes_port        => 4200,
				config_db          => "esb_config",
				maintenance_mode   => "true",
				depsync            => "true",
				sub_cluster_domain => "worker",
				owner		   => "kurumba",
				group		   => "kurumba",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
		}
}

node 's2demo.wso2.com' inherits confignode{
		$server_ip      = "192.168.4.150"

		include system_config

		class {"stratos::sc":
				version            => "1.0.0",
			   	offset             => 2,
				tribes_port        => 5001,
				maintenance_mode   => "true",
			  	owner		   => "wso2",
				group		   => "wso2",
			   	target             => "/mnt/${server_ip}",
			  	stage              => "deploy",
		}

		class {"stratos::elb":
				services           =>  ["identity,*,mgt",
								   "governance,*,mgt"],
				version            => "2.0.4",
				maintenance_mode   => "true",
				auto_scaler        => "false",
				auto_failover      => "false",
				owner		   => "root",
				group		   => "root",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
		}

		class {"stratos::cc":
				version            => "1.0.0",
				offset             => 1,
			  	tribes_port        => 5001,
				maintenance_mode   => "true",
				owner		   => "wso2",
				group		   => "wso2",
				target             => "/mnt/${server_ip}",
				stage              => "deploy",
		}

		class {"stratos::s2agent":
				version            => "1.0.0",
				offset             => 1,
				tribes_port        => 4025,
				maintenance_mode   => "true",
			   	owner		   => "wso2",
			   	group		   => "wso2",
			   	target             => "/mnt/${server_ip}",
			   	stage              => "deploy",
		}

}

