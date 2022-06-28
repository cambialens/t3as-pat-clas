# Make sure this script was sourced/dotted!
[[ "$0" == *_common.sh ]] && echo "Please '.' or 'source' this script!" && exit 1

BASE_DIR=$(cd "$(dirname "$0")"../; pwd)
DATA_DIR="$BASE_DIR/data"
BACKUP_DIR="$BASE_DIR/backup"

DEPLOYED_DATA_DIR=/ebs/data/patclass
