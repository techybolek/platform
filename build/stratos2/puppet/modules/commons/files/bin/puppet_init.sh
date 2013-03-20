#!/bin/bash

WHICH=`/usr/bin/which which` 
PUPPETD=`${WHICH} puppetd`
GREP=`${WHICH} grep`
PS=`${WHICH} ps`
SLEEP=`${WHICH} sleep`

if [ $# -eq 1 ]; then 
	SERVICES=$@; COMMAND="${PUPPETD} -vt --tags ${SERVICES}"
elif [ $# -eq 0 ]; then
	COMMAND="${PUPPETD} -vt"
fi

read_master() {
	local COUNT=0
	[ ${COUNT} == 10 ] && exit 1 || true
	${COMMAND}
	${PS} -auwx | ${GREP} java | ${GREP} wso2
#	[ $? == 0 ] && true || (read_master ; COUNT=`expr ${COUNT} + 1`)
}

echo "Asia/Colombo" > /etc/timezone
dpkg-reconfigure --frontend noninteractive tzdata

${PUPPETD} --enable

read_master

${PUPPETD} --disable

exit 0

