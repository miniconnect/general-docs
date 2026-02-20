#!/bin/sh

#----------
# Starts an instant demo on the holodb-standalone example using splitted window.
#----------

startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath -- "$0" )" )"
rootDir="$( realpath -- "${selfDir}/../.." )"

(
    cd "${selfDir}/composite-build" &&
    ./gradlew holodb:app:clean holodb:app:shadowJar &&
    ./gradlew miniconnect-client:client:clean miniconnect-client:client:shadowJar;
) || {
    echo "Failed to build shadow JARs"
    exit 1
}

tmux \
    new-session "java -jar '${rootDir}/holodb/projects/app/build/libs'/*-all.jar '${rootDir}/general-docs/examples/holodb-standalone/config.yaml'" \; \
    split-window -vb "sleep 2 && java -jar '${rootDir}/miniconnect-client/projects/client/build/libs'/*-all.jar" \
;
