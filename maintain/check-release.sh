#!/bin/sh

#----------
# Checks projects' state to see if they are ready for release.
#----------

startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath "$0" )" )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiError="$( printf '\e[1;31m' )"
ansiWarning="$( printf '\e[1;33m' )"
ansiReset="$( printf '\e[0m' )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}" || {
            echo "Failed to cd to projectDirectory=${projectDirectory}"
            exit 1
        }
    
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
            dependencyTasks="$( ./gradlew tasks --all --quiet --console=plain | grep -E '^[^> :]+:dependencies\b' | sed 's/ .*$//' )"
            subprojectsWithSnapshot=''
            for dependencyTask in $dependencyTasks; do
                subprojectName="$( echo "${dependencyTask}" | sed -E 's/:.*$//' )"
                snapshotDependencies="$( ./gradlew "${dependencyTask}" --quiet --console=plain | grep -E '^\W+ [^ :]+:[^ :]+:[^ :\-]+\-SNAPSHOT' | sed -E 's/^\W+ //' | sed -E 's/ .*$//' | sort -u )"
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
            echo "${ansiError}${projectName}: Failed gradle build${ansiReset}"
        fi
    fi
    
    if grep -E '^\s*FROM\s+\S+-SNAPSHOT(\s|$)' -q -R . --include='Dockerfile'; then
        echo "${ansiError}${projectName}: Some Dockerfile contains SNAPSHOT FROM${ansiReset}"
    fi
    
done

exampleRootPath='general-docs/examples/'
for examplePath in "${exampleRootPath}"*; do
    exampleDirectory="${rootDir}/${examplePath}"
    
    if [ -d "$exampleDirectory" ]; then
        exampleName="$( basename "$exampleDirectory" )"
        
        cd "$exampleDirectory" || {
            echo "Failed to cd to exampleDirectory=${exampleDirectory}"
            exit 1
        }
        
        if [ -f "${exampleDirectory}/build.sh" ]; then
            if ! ./build.sh >/dev/null 2>/dev/null; then
                echo "${ansiError}${exampleRootPath}${exampleName}: Failed custom build${ansiReset}"
            fi
        elif [ -f "${exampleDirectory}/gradlew" ]; then
            if ! ./gradlew clean build --quiet >/dev/null 2>/dev/null; then
                echo "${ansiError}${exampleRootPath}${exampleName}: Failed gradle build${ansiReset}"
            fi
        fi
    fi
done

cd "${startDir}" || {
    echo "Failed to cd to startDir=${startDir}"
    exit 1
}
