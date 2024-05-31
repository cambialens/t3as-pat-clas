#! /usr/bin/env bash

set -e

bin_dir=$(cd "$(dirname "$0")"; pwd)
source "$bin_dir"/_common.sh
cd "$DATA_DIR"

ipc_version=20240101
cpc_version=202405

ipc_file="ipc_scheme_${ipc_version}.zip"
cpc_file="CPCSchemeXML${cpc_version}.zip"

echo "Downloading CPC, IPC and USPC patent classification data..."
echo

for url in "https://www.cooperativepatentclassification.org/sites/default/files/2024_05_prerelease/$cpc_file" \
      "https://www.wipo.int/ipc/itos4ipc/ITSupport_and_download_area/$ipc_version/MasterFiles/$ipc_file" ; do
  echo "Downloading $url..."
  wget --no-clobber $url
done

#http://patents.reedtech.com/downloads/PatentClassInfo/ClassData/classdefs.zip

# The USPC zip file contains a DTD "classdef.dtd" but it is not a valid XML DTD (presumably SGML?)
# The USPC data refers to a DTD "xclassdef.dtd", so this file must exist.
# Until we convert the DTD to a valid XML DTD, we use an empty file.

rsync -av --ignore-errors "$BACKUP_DIR"/classdefs-patched.zip "$DATA_DIR"/


#touch xclassdef.dtd

# The USPC zip file contains a corrupted entry for class 560.
# This reproduces a manual edit to produce a fixed version classdefs-patched.zip
#if [[ ! -f classdefs-patched.zip ]] ; then
#    echo "Patching US data..."
#    unzip classdefs.zip classdefs201502/class_560.xml
#    patch classdefs201502/class_560.xml class_560.xml.diff
#    cp classdefs.zip classdefs-patched.zip
#    zip -f classdefs-patched.zip classdefs201502/class_560.xml
#    rm -rf classdefs201502/
#else
#    echo "US data already patched"
#    echo
#fi

# While we're at it, also get the jquery-ui-fancytree widget required by pat-clas-ui.iA
if [[ ! -f jquery.fancytree-2.0.0-4.zip ]] ; then
    echo "Download and install jquery-ui-fancytree into pat-clas-ui"
    wget --no-clobber https://github.com/mar10/fancytree/releases/download/v2.0.0-4/jquery.fancytree-2.0.0-4.zip
    ft_dir="$bin_dir/../pat-clas-ui/fancytree"
    rm -rf "$ft_dir"
    mkdir -p $ft_dir
    ( cd $ft_dir; unzip $DATA_DIR/jquery.fancytree-2.0.0-4.zip )
else
    echo "jquery-ui-fancytree already installed"
fi

echo
echo "Done."
