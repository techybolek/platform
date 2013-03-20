#!/bin/bash
# This script create a Stratos2 release pack from svn and binaries

if [[ -d ./wso2s2-1.0.0 ]]; then
    rm -rf ./wso2s2-1.0.0
fi
if [[ -f ./wso2s2-1.0.0.zip ]]; then
    rm -f ./wso2s2-1.0.0.zip
fi
cp -rf ./setup ./wso2s2-1.0.0
cd ./wso2s2-1.0.0

# Copy release binaries
cp -rf ../binaries/wso2sc-1.0.0.zip ./
cp -rf ../binaries/wso2cc-1.0.0.zip ./
cp -rf ../binaries/wso2elb-2.0.4.zip ./
cp -rf ../binaries/wso2s2agent-1.0.0.zip ./
cp -rf ../binaries/wso2s2cli-1.0.1.zip ./

# Copy release docs
cp -f ~/Downloads/Stratos2UserGuide.pdf ./docs
cp -f ~/Downloads/Stratos2.0InstallationGuide.pdf ./docs
cp -f ~/Downloads/Stratos2ArchitectureGuide.pdf ./docs
cp -f ~/Downloads/Stratos2CartridgeDevelopmentGuide.pdf ./docs


find ./ -name "*.svn"|xargs rm -rf

cd ../
zip -rq ./wso2s2-1.0.0.zip ./wso2s2-1.0.0/

