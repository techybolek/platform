stage { 'configure': require => Stage['main'] }
stage { 'deploy': require => Stage['configure'] }

node basenode {
#        $package_repo 		= "http://puppet-stratos-packs.s3-website-us-east-1.amazonaws.com/stratos-1.6"
#        $depsync_svn_repo 	= "https://svn.stratoslive.wso2.com/wso2/repo/slive/"
	$local_package_dir	= "/mnt/packs/"
	$deploy_new_packs	= "true"
}

node confignode inherits basenode  {
	## Service subdomains
	$stratos_domain 	= "stratoslive.wso2.com"
#	$as_subdomain 		= "appserver"
	$mb_subdomain 		= "messaging"

	## Server details for billing
	$time_zone		= "GMT-8:00"
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
        

#	class {"stratos::elb":
#                services           =>  ["identity,*,mgt",
#                                        "governance,*,mgt"],
#                version            => "2.0.3",
#                maintenance_mode   => "true",
#                auto_scaler        => "false",
#                auto_failover      => "false",
#		owner		   => "root",
#		group		   => "root",
#		target             => "/mnt/${server_ip}",
#                stage              => "deploy",
#        }

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

node 'php.novalocal'{

class {"php_cartridge":
                syslog            => "syslog:local2",
		docroot		  => "/var/www/www",
		samlalias	  => "/var/www/simplesamlphp/www",
}

}


node 'as.novalocal'{

class {"stratos::as_cartridge":
                version            => "1.0.0",
                offset             => 4,
                mb_host            => "localhost",
                mb_port            => "5673",
                maintenance_mode   => "true",
                owner              => "ubuntu",
                group              => "ubuntu",
                target             => "/mnt/${server_ip}",
                stage              => "deploy",
        }

}


