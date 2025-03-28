#!/bin/sh

#----------
# Starts an instant demo on the holodb-standalone example using splitted window.
#----------

startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath -- "$0" )" )"
rootDir="$( realpath -- "${selfDir}/../.." )"

holodbDir="${rootDir}/holodb"
cd "$holodbDir" || {
    echo "Failed to cd to holodbDir=${holodbDir}"
    exit 1
}

./gradlew app:jibDockerBuild

exampleDir="${rootDir}/general-docs/examples/holodb-standalone"
cd "$exampleDir" || {
    echo "Failed to cd to holodbDir=${exampleDir}"
    exit 1
}

./kill.sh && ./build.sh && ./start.sh

# TODO: add health-check
sleep 5

tmux \
    new-session 'docker logs holodb-example-standalone --follow' \; \
    split-window -vb 'miniconnect-client || sleep 3'

./kill.sh

cd "${startDir}" || {
    echo "Failed to cd to startDir=${startDir}"
    exit 1
}
