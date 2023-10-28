#!/bin/sh

set -e

selfDir="$( dirname -- "$( realpath "$0" )" )"
name="$( cat "${selfDir}/name.txt" )"

docker build -t miniconnect/"${name}" "${selfDir}"
