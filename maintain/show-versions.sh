#!/bin/sh

#----------
# Show project versions.
#----------

startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath -- "$0" )" )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiName="$( printf '\e[1m' )"
ansiStable="$( printf '\e[1;32m' )"
ansiSnapshot="$( printf '\e[1;33m' )"
ansiReset="$( printf '\e[0m' )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}" || {
        echo "Failed to cd to projectDirectory=${projectDirectory}"
        exit 1
    }
    
    if ! [ -f "${projectDirectory}/gradlew" ]; then
        continue
    fi
    
    projectVersion="$( ./gradlew --quiet --console=plain printVersion )"
    case "$projectVersion" in
        *-SNAPSHOT)
            projectVersionFormat="$ansiSnapshot" ;;
        *)
            projectVersionFormat="$ansiStable" ;;
    esac
    
    printf '%s%-25s%s%s%-20s%s\n' "$ansiName" "$projectName" "$ansiReset" "$projectVersionFormat" "$projectVersion" "$ansiReset"
done

cd "${startDir}" || {
    echo "Failed to cd to startDir=${startDir}"
    exit 1
}
