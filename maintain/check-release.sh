#!/bin/bash

#----------
# Checks projects' state to see if they are ready for release.
#----------

startDir=`pwd`

selfDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiError="$( printf '\e[1;31m' )"
ansiWarning="$( printf '\e[1;33m' )"
ansiReset="$( printf '\e[0m' )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}"
    
    if [ -d '.git' ]; then
        gitChangedFiles="$( git diff --name-only; git diff --staged --name-only; )"
        if [ -n "$gitChangedFiles" ]; then
            echo "${ansiError}${projectName}: Uncommitted changes${ansiReset}"
        fi
        pushedHash="$( git rev-parse '@{push}' )"
        headHash="$( git rev-parse 'HEAD' )"
        if [ "${pushedHash}" != "${headHash}" ]; then
            echo "${ansiError}${projectName}: Unpushed commits${ansiReset}"
        fi
    fi
    
    if [ -f 'gradlew' ]; then
        if ./gradlew clean build --quiet >/dev/null 2>/dev/null; then
            dependencyTasks="$( ./gradlew tasks --all --quiet --console=plain | egrep '^[^> :]+:dependencies\b' | sed 's/ .*$//' )"
            subprojectsWithSnapshot=''
            for dependencyTask in $dependencyTasks; do
                subprojectName="$( echo "${dependencyTask}" | sed -E 's/:.*$//' )"
                snapshotDependencies="$( ./gradlew "${dependencyTask}" --quiet --console=plain | egrep '^\W+ [^ :]+:[^ :]+:[^ :\-]+\-SNAPSHOT' | sed -E 's/^\W+ //' | sed -E 's/ .*$//' | sort -u )"
                if [ -n "$snapshotDependencies" ]; then
                    if [ -z "$subprojectsWithSnapshot" ]; then
                        subprojectsWithSnapshot="$subprojectName"
                    else
                        subprojectsWithSnapshot="${subprojectsWithSnapshot}, ${subprojectName}"
                    fi
                fi
            done
            if [ -n "$subprojectsWithSnapshot" ]; then
                echo "${ansiWarning}${projectName}: SNAPSHOT dependencies ($subprojectsWithSnapshot)${ansiReset}"
            fi
        else
            echo "${ansiError}${projectName}: Failed build${ansiReset}"
        fi
    fi
    
    if egrep '^\s*FROM\s+\S+-SNAPSHOT(\s|$)' -q -R . --include='Dockerfile'; then
        echo "${ansiError}${projectName}: Some Dockerfile contains SNAPSHOT FROM${ansiReset}"
    fi
    
done

cd "${startDir}"
