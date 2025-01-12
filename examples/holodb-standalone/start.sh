#!/bin/sh

set -e

selfDir="$( dirname -- "$( realpath "$0" )" )"
name="$( cat "${selfDir}/name.txt" )"

docker rm -f "${name}" > /dev/null 2>&1
docker run --name="${name}" -p 3430:3430 -d miniconnect/"${name}":latest
