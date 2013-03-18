#!/bin/bash

STRATOS2_ZIP=wso2-stratos-2.0.0-beta1.zip
STRATOS2_ZIP_MD5=$STRATOS2_ZIP.md5
STRATOS2_ZIP_ASC=$STRATOS2_ZIP.asc
DEMO_OVA=s2demo.ova
DEMO_OVA_MD5=$DEMO_OVA.md5
DEMO_OVA_ASC=$DEMO_OVA.asc
DOCS_ZIP=stratos2_docs.zip
DOCS_ZIP_MD5=$DOCS_ZIP.md5
DOCS_ZIP_ASC=$DOCS_ZIP.asc
AS_CARTRIDGE_TARZ=precise-server-AS-cloudimg-amd64.tar.gz
AS_CARTRIDGE_TARZ_MD5=$AS_CARTRIDGE_TARZ.md5
AS_CARTRIDGE_TARZ_ASC=$AS_CARTRIDGE_TARZ.asc

echo "Deleting existing if any"
if [[ -f $STRATOS2_ZIP_MD5 ]]; then
    rm -f $STRATOS2_ZIP_MD5
fi
if [[ -f $STRATOS2_ZIP_ASC ]]; then
    rm -f $STRATOS2_ZIP_ASC
fi
if [[ -f $DEMO_OVA_MD5 ]]; then
    rm -f $DEMO_OVA_MD5
fi
if [[ -f $DEMO_OVA_ASC ]]; then
    rm -f $DEMO_OVA_ASC
fi
if [[ -f $DOCS_ZIP_MD5 ]]; then
    rm -f $DOCS_ZIP_MD5
fi
if [[ -f $DOCS_ZIP_ASC ]]; then
    rm -f $DOCS_ZIP_ASC
fi
if [[ -f $AS_CARTRIDGE_TARZ_MD5 ]]; then
    rm -f $AS_CARTRIDGE_TARZ_MD5
fi
if [[ -f $AS_CARTRIDGE_TARZ_ASC ]]; then
    rm -f $AS_CARTRIDGE_TARZ_ASC
fi

echo "Creating MD5"
openssl md5 < $DEMO_OVA > $DEMO_OVA_MD5
openssl md5 < $STRATOS2_ZIP > $STRATOS2_ZIP_MD5
openssl md5 < $DOCS_ZIP > $DOCS_ZIP_MD5
openssl md5 < $AS_CARTRIDGE_TARZ > $AS_CARTRIDGE_TARZ_MD5

echo "To sign please enter password for the private key"
gpg --armor --output $DEMO_OVA_ASC --detach-sig $DEMO_OVA
gpg --armor --output $STRATOS2_ZIP_ASC --detach-sig $STRATOS2_ZIP
gpg --armor --output $DOCS_ZIP_ASC --detach-sig $DOCS_ZIP
gpg --armor --output $AS_CARTRIDGE_TARZ_ASC --detach-sig $AS_CARTRIDGE_TARZ

echo "Copying to KEYS file"
gpg --armor --export damitha@wso2.com > KEYS
echo "DONE"
