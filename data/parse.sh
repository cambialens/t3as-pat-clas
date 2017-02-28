#!/usr/bin/env bash

set -e

script_dir=$(cd "$(dirname "$0")"; pwd)
cd "$script_dir"

parser='../pat-clas-parse/target/pat-clas-parse-*.one-jar.jar'

if ! ls $parser &> /dev/null ; then
  echo "Parser jar not found: $parser" ; exit 1
fi

echo "Parsing CPC, IPC and USPC patent classification data..."
echo
java -jar $parser
echo
echo "Done."
