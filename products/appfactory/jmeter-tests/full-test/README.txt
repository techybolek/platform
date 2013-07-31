Following parameters must be passed to the test in proir to start the test.
otherwise it will take its default values and may not be working in your environment.
Description of the each parameter is mentioned below.

appfactory_hostname - host of the appfactory default value is 	appfactorypreview.wso2.com
appfactory_port	-port that appfactory runs. default value is 443
username	-user name to perform the test, default value asankad@wso2.com
password	-password of the user 
waiting_time	-this value is in milliseconds.This has been introduced because the app creation time may be varied depending on the environment.
		  so in a case of faliure increase the value of this and try again.default value is 40000. (but make sure to change the appkeys and names at 
		app_factory_5_app_data.csv file.


