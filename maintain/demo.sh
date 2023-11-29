#!/bin/bash

startDir=`pwd`

selfDir="$( dirname -- "$( realpath "$0" )" )"
rootDir="${selfDir}/../.."

cd "${rootDir}/holodb"

./gradlew app:jibDockerBuild

cd "${rootDir}/general-docs/examples/holodb-standalone"

name="$( cat name.txt )"

./kill.sh && ./build.sh && ./start.sh

# TODO: add health-check
sleep 5

tmux \
    new-session 'docker logs holodb-example-standalone --follow' \; \
    split-window -vb 'miniconnect-client || sleep 3'

./kill.sh

cd "${startDir}"
