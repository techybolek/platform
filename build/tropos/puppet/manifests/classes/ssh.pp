class ssh ( $port=22, $bind_address, $root_login=no, $pubkey_auth=yes, $password_auth=yes, $use_pam=yes ) {
	
	$service      = "ssh"
       	$template     = "sshd_config.erb"
        $provider     = "apt"

	package { "openssh-server":
		provider	=> $provider,
                ensure 		=> installed,
        }

        service { $service:
                ensure    	=> running,
                enable    	=> true,
                subscribe 	=> File["/etc/ssh/sshd_config"],
                require   	=> Package["openssh-server"],
        }

        file { "/etc/ssh/sshd_config":
                ensure  	=> present,
                content 	=> template("ssh/${template}"),
                require 	=> Package["openssh-server"],
                notify  	=> Service[$service];

		"/etc/pam.d/sshd":
                ensure  	=> present,
                source  	=> "puppet:///ssh/pam/sshd",
                require 	=> Package["openssh-server"],
                notify  	=> Service[$service];
	
		"/home/ubuntu/.ssh/authorized_keys":
		ensure 		=> present,
		source		=> "puppet:///ssh/authorized_keys",
		require		=> Package["openssh-server"];
        }
}

