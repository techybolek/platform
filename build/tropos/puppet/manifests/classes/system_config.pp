class system_config {

	include hosts
#	include apt
#	include nrpe
#	include ganglia
#        include java
#	include mcollective

#	class { "ssh":
#                port => 1984,
#                bind_address => $ipaddress,
#                root_login => yes,
#                pubkey_auth => yes,
#                password_auth => yes,
#                use_pam => yes,
#        }
	
#	user { "kurumba": 
#  		password   => "1Mmhh30pWiMz2N720hC4",
#  		ensure     => present,                            
# 	 	managehome => true,
#	}

	file {  "/root/bin":
                owner   => root,
                group   => root,
                ensure  => "directory";

		"/root/bin/firewall":
                owner   => root,
                group   => root,
                ensure  => "directory",
                require => File["/root/bin"];

		"/root/bin/puppet_init.sh":
                owner   => root,
                group   => root,
                mode    => 0755,
                source  => "puppet:///commons/bin/puppet_init.sh",
                require => File["/root/bin/firewall"];

		"/root/bin/puppet_clean.sh":
                owner   => root,
                group   => root,
                mode    => 0755,
                content => template("puppet_clean.sh.erb"),
                require => File["/root/bin/firewall"],
        }

        Exec {  path    => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/"  }
        #Exec {  path    => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"  }
}

