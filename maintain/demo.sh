#!/bin/bash

startDir=`pwd`

selfDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
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
