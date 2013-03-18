class php_cartridge (syslog,docroot,samlalias){
	
	$packages = ["nano","zip","build-essential","mysql-client","apache2","php5","php5-cli","libapache2-mod-php5","php5-gd","php5-mysql","php-db","php-pear","php5-curl","curl","wget","php5-ldap","php5-adodb","mailutils","php5-imap","php5-sqlite","php5-xmlrpc","php5-xsl","openssl","ssl-cert","ldap-utils","php5-mcrypt","mcrypt","ufw","fail2ban","git","libboost-all-dev","ruby"]

        file { "/etc/apt/apt.conf.d/90forceyes":
                ensure  => present,
                source  => "puppet:///apt/90forceyes";
        }

        exec { "update-apt":
                path    => ['/bin', '/usr/bin'],
                command => "apt-get update > /dev/null 2>&1 &",
                require => File["/etc/apt/apt.conf.d/90forceyes"],
        }

        package { $packages:
                provider        => apt,
                ensure          => installed,
                require         => Exec["update-apt"],
        }	
	
	file { "/etc/apache2/apache2.conf":
        	owner   => root,
        	group   => root,
        	mode    => 775,
        	content => template("php_cartridge/etc/apache2/apache2.conf.erb"),
                require         => Exec["update-apt"];
    	
	        "/etc/apache2/sites-available/default":
        	owner   => root,
        	group   => root,
        	mode    => 775,
        	content => template("php_cartridge/etc/apache2/sites-available/default.erb"),
                require         => File["/etc/apache2/apache2.conf"];
	
		"/etc/apache2/sites-available/default-ssl":
        	owner   => root,
        	group   => root,
        	mode    => 775,
        	content => template("php_cartridge/etc/apache2/sites-available/default-ssl.erb"),
                require         => File["/etc/apache2/sites-available/default"];
    	}
	
	exec { "enable ssl module":
                path    => ['/bin', '/usr/bin','/usr/sbin/'],
                command => "a2enmod ssl",
                require         => File["/etc/apache2/sites-available/default-ssl"];
	
		"enable ssl":
                path    => ['/bin', '/usr/bin','/usr/sbin/'],
                command => "a2enmod ssl",
                require         => Exec["enable ssl module"];
		
		"apache2 restart":
                path    => ['/bin', '/usr/bin','/usr/sbin/'],
                command => "/etc/init.d/apache2 restart",
                require         => Exec["enable ssl"];
	}

#	file {  "/opt/thrift-0.8.0.tar.gz":
#        	source          => "puppet:///php_cartridge/pack/thrift-0.8.0.tar.gz",
#                ensure          => present,
#		require         => Exec["apache2 restart"];
#        }

#        exec {  "unzip_thrift":
#                path            => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
#		cwd		=> "/opt",
#                command         => "tar -zxf thrift-0.8.0.tar.gz",
#                require         => [File["/opt/thrift-0.8.0.tar.gz"]];
              
#		"install_thrift":
#		path            => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/thrift-0.8.0"],
#		cwd		=> "/opt/thrift-0.8.0",
#		timeout         => 0,
#                command         => "./configure --libdir=/usr/lib;make;make install",
#                require         => [ Exec["unzip_thrift"]];

#	}	

	
	file {  "/opt/cartridge_data_publisher_1.0.0.tgz":
        	source          => "puppet:///php_cartridge/pack/cartridge_data_publisher_1.0.0.tgz",
                ensure          => present,
		require         => Exec["apache2 restart"];
        }

	exec {  "unzip_data_publisher":
                path            => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
                cwd             => "/opt",
                command         => "tar -zxf cartridge_data_publisher_1.0.0.tgz",
                require         => [File["/opt/cartridge_data_publisher_1.0.0.tgz"]];

       		"install_data_publisher":
      		path            => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/thrift-0.8.0"],
       		cwd             => "/opt/cartridge_data_publisher_1.0.0",
       		timeout         => 0,
       		command         => "make",
       		require         => [ Exec["unzip_data_publisher"]];

	}
	
	file {  "/etc/rc.local":
        	source          => "puppet:///php_cartridge/etc/rc.local",
                ensure          => present,
		require         => Exec["apache2 restart"];
	}

	file {  "/opt/get-launch-params.rb":
        	source          => "puppet:///php_cartridge/opt/get-launch-params.rb",
                ensure          => present,
		require         => Exec["apache2 restart"];
	}
	file {  "/opt/wso2-cartridge-init.sh":
        	source          => "puppet:///php_cartridge/opt/wso2-cartridge-init.sh",
                ensure          => present,
		require         => Exec["apache2 restart"];
	}



}
