#!/bin/sh

#----------
# Rebuilds projects and publishes their artifacts locally.
#----------


startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath "$0" )" )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}" || {
        echo "Failed to cd to projectDirectory=${projectDirectory}"
        exit 1
    }
    
    if [ -f 'gradlew' ]; then
        rm -Rf ./projects/*/bin
        ./gradlew clean build
        ./gradlew -p . publishToMavenLocal --warning-mode none
        
        if ./gradlew tasks | grep -E '\bjibDockerBuild\b' -q; then
            ./gradlew jibDockerBuild
        fi
    fi
done

cd "${startDir}" || {
    echo "Failed to cd to startDir=${startDir}"
    exit 1
}
