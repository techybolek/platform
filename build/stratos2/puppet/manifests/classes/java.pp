class java { 

	$java_home 	= "jdk1.7.0_07"
	$package 	= "jdk-7u7-linux-x64.tar.gz"

	file {  "/opt/${package}":
		owner	=> root,
		group	=> root,
		mode	=> 755,
		source	=> "puppet:///java/${package}",
		ignore	=> ".svn",
		ensure  => present;

		"/opt/java/jre/lib/security/":
                owner   => root,
                group   => root,
                source  => ["puppet:///java/jars/"],
		ignore	=> ".svn",
                ensure  => present,
                recurse => true,
                require => File["/opt/java"];

		"/opt/java":
		ensure 	=> link,
		target	=> "/opt/${java_home}",
		require	=> Exec["install_java"],
	}
    
	exec {  "install_java":
		cwd	=> "/opt",
		command => "tar xzf ${package}",
		unless	=> "/usr/bin/test -d /opt/${java_home}",
		creates	=> "/opt/${java_home}/COPYRIGHT",
		require	=> File["/opt/${package}"],
	} 

}
 
