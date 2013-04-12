#!/bin/sh
rm -rf wso2is-4.5.0-SNAPSHOT
unzip modules/distribution/target/wso2is-4.5.0-SNAPSHOT.zip
cp usecases/common/drivers/informix/*.jar  wso2is-4.5.0-SNAPSHOT/repository/components/lib/
cp usecases/common/drivers/mysql/*.jar  wso2is-4.5.0-SNAPSHOT/repository/components/lib/
cp usecases/common/client-truststore.jks wso2is-4.5.0-SNAPSHOT/repository/resources/security/
cp usecases/common/informix-um/*.jar wso2is-4.5.0-SNAPSHOT/repository/components/lib/
cp usecases/$1/master-datasources.xml wso2is-4.5.0-SNAPSHOT/repository/conf/datasources/
cp usecases/$1/user-mgt.xml wso2is-4.5.0-SNAPSHOT/repository/conf/
cat usecases/$1/readme
