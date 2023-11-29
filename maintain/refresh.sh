#!/bin/bash

#----------
# Rebuilds projects and publishes their artifacts locally.
#----------


startDir=`pwd`

selfDir="$( dirname -- "$( realpath "$0" )" )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}"
    
    if [ -f 'gradlew' ]; then
        rm -Rf ./projects/*/bin
        ./gradlew clean build
        ./gradlew -p . publishToMavenLocal -x signMavenJavaPublication --warning-mode none
        
        if ./gradlew tasks | egrep '\bjibDockerBuild\b' -q; then
            ./gradlew jibDockerBuild
        fi
    fi
done

cd "${startDir}"
