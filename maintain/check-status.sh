#!/bin/bash

#----------
# Checks projects' basic status informations quickly.
#----------

startDir=`pwd`

selfDir="$( dirname -- "$( realpath "$0" )" )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiBold="$( printf '\e[1m' )"
ansiSuccess="$( printf '\e[1;32m' )"
ansiError="$( printf '\e[1;31m' )"
ansiReset="$( printf '\e[0m' )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}"
    
    echo -n "${ansiBold}${projectName}${ansiReset} ... ";
    
    gitStatusOutput="$( git status --porcelain=v1 )"
    
    headHash="$( git rev-parse 'HEAD' )"
    pushedHash="$( git rev-parse '@{push}' )"
    if [ "${pushedHash}" != "${headHash}" ]; then
        gitUnpushedOutput="Unpushed commits (HEAD(${headHash}) != "'@{push}'"(${pushedHash}))"
    fi
    
    if [ -f ./gradlew ]; then
        gradleCheckOutput="$( ./gradlew --quiet --console=plain check 2>&1 )"
    fi
    
    if [ -z "${gitStatusOutput}" ] && [ -z "${gradleCheckOutput}" ] && [ -z "${gitUnpushedOutput}" ]; then
        echo "${ansiSuccess}OK${ansiReset}"
    else
        echo "${ansiError}FAILED${ansiReset}"
    fi
    
    if [ -n "${gitStatusOutput}" ]; then
        echo "    ${ansiError}Git status output:${ansiReset}"
        echo "${gitStatusOutput}" | sed -E 's/^/        /'
        echo
    fi
    
    if [ -n "${gitUnpushedOutput}" ]; then
        echo "    ${ansiError}Git upstream output:${ansiReset}"
        echo "${gitUnpushedOutput}" | sed -E 's/^/        /'
        echo
    fi
    
    if [ -n "${gradleCheckOutput}" ]; then
        echo "    ${ansiError}Gradle check output:${ansiReset}"
        echo "${gradleCheckOutput}" | sed -E 's/^/        /'
        echo
    fi
    
done

cd "${startDir}"