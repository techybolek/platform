class apt {
	$packages = ["lsof","unzip","sysstat","telnet","nmap","less","nagios-nrpe-server","ganglia-monitor"]

        file { "/etc/apt/apt.conf.d/90forceyes":
                ensure  => present,
                source  => "puppet:///apt/90forceyes";
        }

#        exec { "update-apt":
#                path    => ['/bin', '/usr/bin'],
#                command => "apt-get update > /dev/null 2>&1 &",
#                require => File["/etc/apt/apt.conf.d/90forceyes"],
#        }

        package { $packages:
                provider        => apt,
                ensure          => installed,
                require         => File["/etc/apt/apt.conf.d/90forceyes"],
        }	
}
