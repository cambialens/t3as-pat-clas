#!/bin/bash

set -e
set -o pipefail

#shellcheck source=_common.sh
bin_dir=$(cd "$(dirname "$0")"; pwd)
source "$bin_dir"/_common.sh
script_name=$0

function usage() {
    echo "$script_name -i ipc_file -c cpc_file -u us_file"
    echo "$script_name -h usage"
    echo '    -i ipc_file - the name of the IPC classification file to parse'
    echo '    -c upc_file - the name of the CPC classification file to parse'
    echo '    -u us_file - the name of the US classification file to parse'
    echo '    -h Display usage'
}

ipc_file=''
cpc_file=''
us_file=''

while getopts 'i:c:u:h' option; do
    case "${option}"
    in
        h)  usage; exit 0;;
        i)  ipc_file=${OPTARG} ;;
        c)  cpc_file=${OPTARG} ;;
        u)  us_file=${OPTARG} ;;
        \?) echo 'Invalid parameter'; usage; exit 1;;
    esac
done
shift "$((OPTIND - 1))"

params=()


if [[ ! -f $ipc_file ]] ; then 
  echo "-i ipc_file not specified or not found: $ipc_file" && usage && exit 1 
fi
params+=('-i' "$ipc_file")

if [[ ! -f $cpc_file ]] ; then  
  echo "-c cpc_file not specified or not found: $cpc_file" && usage && exit 1
fi
params+=('-c' "$cpc_file")

if [[ ! -f $us_file ]] ; then 
  echo "-u us_file not specified or not found: $us_file" && usage && exit 1
fi
params+=('-u' "$us_file")

parser="$bin_dir"/../pat-clas-parse/target/pat-clas-parse-*.one-jar.jar
echo $bin_dir
if !  ls $parser 1> /dev/null 2>&1 ; then 
  echo "Parser jar not found: $parser" && exit 1
fi  

cd "$DATA_DIR"

echo "Parsing classification data..."
java -jar $parser "${params[@]}" 2>&1
