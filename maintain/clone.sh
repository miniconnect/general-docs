#!/bin/bash

#----------
# Clones all missing projects.
#----------

urlMatch='github.com/miniconnect/'
urlPrefix='https://github.com/miniconnect/'
urlSuffix='.git'

startDir=`pwd`

selfDir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
rootDir="${selfDir}/../.."

cd "${rootDir}"

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiBold="$( printf '\e[1m' )"
ansiSuccess="$( printf '\e[1;32m' )"
ansiError="$( printf '\e[1;31m' )"
ansiReset="$( printf '\e[0m' )"

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"
    
    
    if [ -d "${projectDirectory}/.git" ] && git -C "${projectDirectory}" remote -v | fgrep -q "${urlMatch}"; then
        echo "${ansiSuccess}FOUND${ansiReset}: ${projectName}"
    elif [ -e "${projectDirectory}" ]; then
        echo "${ansiError}OCCUPIED${ansiReset}: ${projectName}"
    else
        echo "${ansiBold}NOT FOUND${ansiReset}: ${projectName}"
        echo " clone..."
        echo
        git clone "${urlPrefix}${projectName}${urlSuffix}" "${projectDirectory}"
        echo
    fi
done

cd "${startDir}"
