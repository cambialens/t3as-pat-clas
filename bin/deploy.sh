#!/bin/bash

set -e

if [ `whoami` != root ]; then
    echo Please run this script as root or using sudo
    exit
fi

bin_dir=$(cd "$(dirname "$0")"; pwd)
source "$bin_dir"/_common.sh 

sudo service tomcat8 stop

backup_dir="$DEPLOYED_DATA_DIR-backup-$(date +'%Y-%m-%d-%H%M%S')"
echo "Backing up current databases to $backup_dir"
mv "$DEPLOYED_DATA_DIR" "$backup_dir"
mkdir "$DEPLOYED_DATA_DIR"
echo "Deploying new databases to prod..."
for file in cpcIndex cpcIndexExactSug cpcIndexFuzzySug \
	ipcIndex ipcIndexExactSug ipcIndexFuzzySug \
	uspcIndex uspcIndexExactSug uspcIndexFuzzySug \
	patClasDb.h2.db; do
    cp -r "$DATA_DIR/$file" "$DEPLOYED_DATA_DIR/"
done

sudo service tomcat8 start
sudo tail -f /var/log/tomcat8/catalina.out
