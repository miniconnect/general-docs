#!/bin/sh

set -e

selfDir="$( dirname -- "$( realpath "$0" )" )"
name="$( cat "${selfDir}/name.txt" )"

docker run --name="${name}" -p 3430:3430 -d miniconnect/"${name}":latest
