#!/bin/bash

set -e

bin_dir=$(cd "$(dirname "$0")"; pwd)
source "$bin_dir"/_common.sh 

cd "$bin_dir"

echo "Removing downloaded and generated files..."
[[ -f "$DATA_DIR"/CPCSchemeXML*.zip ]] && cp "$DATA_DIR"/CPCSchemeXML*.zip "$BACKUP_DIR/" 
[[ -f "$DATA_DIR"/ipc_scheme_*.zip ]] && cp "$DATA_DIR"/ipc_scheme_*.zip "$BACKUP_DIR/"
rm -f  "$DATA_DIR"/{classdefs-patched.zip,CPCSchemeXML*.zip,ipc_scheme_*.zip} 

for file in cpcIndex cpcIndexExactSug cpcIndexFuzzySug \
	ipcIndex ipcIndexExactSug ipcIndexFuzzySug \
	uspcIndex uspcIndexExactSug uspcIndexFuzzySug \
	patClasDb.h2.db; do
rm -rf "$DATA_DIR/$file"
done

echo
echo "Update the CPCSchemeXML*.zip, ipc_scheme_*.zip US classdefs-patched.zip data files either by copying back from the $BACKUP_DIR to the $DATA_DIR or by setting the filnames to the required version in $bin_dir/_common.sh and running the download script."
echo
echo "Done."
