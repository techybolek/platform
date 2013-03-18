#!/bin/bash
source ./demorc
count=$1
for I in `seq 1 $count`; do nova delete test$I; done

count=$1
for I in `seq 1 $count`; do ./boot_instance.sh 3406e42d-6a1f-4ba4-b118-4c504e17fc30 test$I; done
