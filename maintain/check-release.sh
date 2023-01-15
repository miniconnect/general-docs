#!/bin/bash

startDir=`pwd`

selfDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiError="$( printf '\e[1;31m' )"
ansiReset="$( printf '\e[0m' )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}"
    
    if [ -d '.git' ]; then
        gitChangedFiles="$( git diff --name-only; git diff --staged --name-only; )"
        if [ -n "$gitChangedFiles" ]; then
            echo "${ansiError}Uncommitted changes in: ${projectName}${ansiReset}"
        fi
        pushedHash="$( git rev-parse '@{push}' )"
        headHash="$( git rev-parse 'HEAD' )"
        if [ "${pushedHash}" != "${headHash}" ]; then
            echo "${ansiError}Unpushed commits in: ${projectName}${ansiReset}"
        fi
    fi
    
    if [ -f 'gradlew' ]; then
        if ./gradlew clean build --quiet >/dev/null 2>/dev/null; then
            dependencyTasks="$( ./gradlew tasks --all --quiet --console=plain | egrep '^[^> :]+:dependencies\b' | sed 's/ .*$//' )"
            for dependencyTask in $dependencyTasks; do
                subprojectName="$( echo "${dependencyTask}" | sed -E 's/:.*$//' )"
                snapshotDependencies="$( ./gradlew "${dependencyTask}" --quiet --console=plain | egrep '^\W+ [^ :]+:[^ :]+:[^ :\-]+\-SNAPSHOT' | sed -E 's/^\W+ //' | sed -E 's/ .*$//' | sort -u )"
                if [ -n "$snapshotDependencies" ]; then
                    echo "${ansiError}SNAPSHOT dependencies in: ${projectName}:${subprojectName}${ansiReset}"
                fi
            done
        else
            echo "${ansiError}Failed build in: ${projectName}${ansiReset}"
        fi
    fi
done

cd "${startDir}"
