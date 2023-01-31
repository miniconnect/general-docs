#!/bin/bash

#----------
# Builds and releases projects, publishes them to online repositories.
# (check-release.sh should be run first to check if everything is OK)
#----------


startDir=`pwd`

selfDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
rootDir="${selfDir}/../.."

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiSuccess="$( printf '\e[1;32m' )"
ansiManual="$( printf '\e[1;34m' )"
ansiWarning="$( printf '\e[1;33m' )"
ansiError="$( printf '\e[1;31m' )"
ansiReset="$( printf '\e[0m' )"
nl="
"

report=''

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    cd "${projectDirectory}"
    
    if [ -f 'gradlew' ]; then
        version="$( ./gradlew printVersion --quiet --console=plain )"
        firstPrefix="$( printf '%-25s | %-20s |' "${projectName}" "${version}" )"
        prefix="$( printf '%-25s | %-20s |' '' '' )"
        messageFormat=' %-10s | %-20s  | %-10s |\n'
        line='- - - - - - -'
        
        report="${report}${line}${nl}"
        
        buildStatusOk=""
        
        rm -Rf ./projects/*/bin
        if ./gradlew clean build; then
            report="${report}${ansiSuccess}${firstPrefix}$( printf "${messageFormat}" '- - -' 'Gradle build' 'SUCCESS' )${ansiReset}${nl}"
            buildStatusOk="OK"
        else
            report="${report}${ansiError}${firstPrefix}$( printf "${messageFormat}" '- - -' 'Gradle build' 'FAILED' )${ansiReset}${nl}"
        fi
        
        if [ -n "${buildStatusOk}" ]; then
            if ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository; then
                report="${report}${ansiSuccess}${prefix}$( printf "${messageFormat}" '- - -' 'Sonatype publish' 'SUCCESS' )${ansiReset}${nl}"
            else
                report="${report}${ansiError}${prefix}$( printf "${messageFormat}" '- - -' 'Sonatype publish' 'FAILED' )${ansiReset}${nl}"
            fi
        else
            report="${report}${ansiWarning}${prefix}$( printf "${messageFormat}" '- - -' 'Sonatype publish' 'SKIPPED' )${ansiReset}${nl}"
        fi
        
        buildDebTasks="$( ./gradlew tasks --all --quiet --console=plain | egrep '^[^> :]+:buildDeb\b' | sed 's/ .*$//' )"
        for buildDebTask in $buildDebTasks; do
            subprojectName="$( echo "${buildDebTask}" | sed -E 's/:.*$//' )"
            if [ -n "${buildStatusOk}" ]; then
                if ./gradlew "${buildDebTask}"; then
                    report="${report}${ansiSuccess}${prefix}$( printf "${messageFormat}" "${subprojectName}" 'Deb package build' 'SUCCESS' )${ansiReset}${nl}"
                else
                    report="${report}${ansiError}${prefix}$( printf "${messageFormat}" "${subprojectName}" 'Deb package build' 'FAILED' )${ansiReset}${nl}"
                fi
            else
                report="${report}${ansiWarning}${prefix}$( printf "${messageFormat}" "${subprojectName}" 'Deb package build' 'SKIPPED' )${ansiReset}${nl}"
            fi
            report="${report}${ansiManual}${prefix}$( printf "${messageFormat}" "${subprojectName}" 'Deb package publish' 'MANUAL' )${ansiReset}${nl}"
        done
    fi
done

cd "${startDir}"

echo
echo "-------------------------------------------------------"
echo

echo "${report}"
