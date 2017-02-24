#! /bin/sh

set -e

script_dir=$(cd "$(dirname "$0")"; pwd)
cd "$script_dir"

echo "Removing downloaded and generated files..."
rm -vrf *.zip *Index* *.dtd *.db
echo
echo "Done."