#!/bin/sh

set -e

selfDir="$( dirname -- "$( realpath "$0" )" )"
name="$( cat "${selfDir}/name.txt" )"

# TODO: move to Dockerfile or jib
"${selfDir}/gradlew" build -p "${selfDir}"

docker build -t miniconnect/"${name}" "${selfDir}"
