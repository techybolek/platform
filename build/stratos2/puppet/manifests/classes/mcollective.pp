class mcollective {

	$package_list = ["ruby","rubygems","libstomp-ruby"]

	package { $package_list:
                ensure 		=> installed;

		"mcollective-server":
                provider 	=> dpkg,
                ensure 		=> installed,
                source 		=> "/opt/mcollective/mcollective_2.0.0-2_all.deb",
                require 	=> [ File["/opt/mcollective/"], Package["mcollective-common"], Class["apt"] ];

                "mcollective-common":
                provider 	=> dpkg,
                ensure 		=> installed,
                source 		=> "/opt/mcollective/mcollective-common_2.0.0-2_all.deb",
                require 	=> [ File["/opt/mcollective/"], Class["apt"] ],

        }
	
        file {  "/etc/mcollective/server.cfg":
                ensure  	=> present,
                source 		=> "puppet:///mcollective/config/server.cfg",
                require 	=> [ Package["mcollective-server"], Package["mcollective-common"] ];

		"/opt/mcollective/":
                owner   	=> root,
                group   	=> root,
                source  	=> "puppet:///mcollective/packages/",
		ignore		=> ".svn",
                recurse 	=> true,
                ensure  	=> present,
                require 	=> Package[$package_list];

		"/etc/mcollective/facts.yaml":
                owner    	=> root,
                group    	=> root,
                mode     	=> 400,
                content  	=> inline_template("<%= scope.to_hash.reject { |k,v| k.to_s =~ /(uptime_seconds|timestamp|free)/ }.to_yaml %>"),
                require 	=> [ Package["mcollective-server"], Package["mcollective-common"] ];

		"/usr/share/mcollective/plugins/mcollective/agent/":
                owner   	=> root,
                group   	=> root,
		mode		=> 644,
                source  	=> ["puppet:///mcollective/agents/puppet/",
                		    "puppet:///mcollective/agents/package/",
                		    "puppet:///mcollective/agents/nettest/",
                		    "puppet:///mcollective/agents/process/"],
		sourceselect	=> all,
		ignore		=> ".svn",
                recurse 	=> true,
                ensure  	=> present,
                require 	=> [ File["/opt/mcollective/"], Package["mcollective-common"], Package["mcollective-server"] ],

        }

	exec { "restart_mcollective":
		command 	=> "/etc/init.d/mcollective restart",
		require 	=> [ File["/etc/mcollective/server.cfg"], Package["mcollective-server"], Package["mcollective-common"] ],
	}
}
