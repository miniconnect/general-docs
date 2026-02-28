#!/bin/sh

#----------
# Prints dependencies for each subprojects
#----------


startDir="$( pwd )"

selfDir="$( dirname -- "$( realpath -- "$0" )" )"
rootDir="${selfDir}/../.."

tmp1="$( mktemp )" || {
    echo "Failed to create tmp file 1"
    exit 1
}
tmp2="$( mktemp )" ||{
    rm -f "$tmp1"
    echo "Failed to create tmp file 2"
    exit 1
}

arg="$1"

cleanup() {
    rm -f "$tmp1" "$tmp2"
}
trap cleanup EXIT INT TERM

projectNames="$( cat "${selfDir}/repositories.txt" )"

ansiBold="$( printf '\e[1m' )"
ansiOk="$( printf '\e[1;32m' )"
ansiOld="$( printf '\e[1;31m' )"
ansiFuture="$( printf '\e[1;35m' )"
ansiUnknown="$( printf '\e[0;33m' )"
ansiTransient="$( printf '\e[0;37m' )"
ansiReset="$( printf '\e[0m' )"

compareVersions() {
    version1="$1"
    version2="$2"
    if [ -z "$version1" ] || [ -z "$version2" ]; then
        echo '?'; return
    fi
    if [ "$version1" = "$version2" ]; then
        echo '='; return
    fi
    left1="$( echo "$version1"- | cut -d '-' -f 1 )"
    right1="$( echo "$version1"- | cut -d '-' -f 2 )"
    left2="$( echo "$version2"- | cut -d '-' -f 1 )"
    right2="$( echo "$version2"- | cut -d '-' -f 2 )"
    if [ "$left1" = "$left2" ]; then
        if [ -z "$left1" ]; then
            echo '<'; return
        elif [ -n "$left2" ]; then
            echo '>'; return
        elif [ "$right1" '<' "$right2" ]; then
            echo '>'; return
        else
            echo '<'; return
        fi
    elif [ "$( printf '%s\n%s\n' "$left1" "$left2" | sort -V | head -n 1 )" = "$version1" ]; then
        echo '<'; return
    else
        echo '>'; return
    fi
}

handleDependency() {
    dependencyString="$1"
    isDirect="$2"
    group="$( echo "$dependencyString" | cut -d ':' -f 1 )"
    name="$( echo "$dependencyString" | cut -d ':' -f 2 )"
    version="$( echo "$dependencyString" | cut -d ':' -f 3 )"
    onlineVersion="$( curl -s "https://search.maven.org/solrsearch/select?q=g:%22${group}%22+AND+a:%22${name}%22&rows=1&wt=json" | jq -r '.response.docs[0].latestVersion // empty' 2>/dev/null )"
    state="$( compareVersions "$version" "$onlineVersion" )"
    stateStyle="$ansiOk"
    if [ "$state" = '<' ]; then
        stateStyle="$ansiOld"
    elif [ "$state" = '>' ]; then
        stateStyle="$ansiFuture"
    elif [ "$state" = '?' ]; then
        stateStyle="$ansiUnknown"
    fi
    if [ -n "$isDirect" ]; then
        printf '    %s%s %s (%s%s)%s\n' "$stateStyle" '•' "$dependencyString" "$state" "$onlineVersion" "$ansiReset";
    else
        printf '    %s%s%s %s%s (%s%s)%s\n' "$stateStyle" '•' "$ansiReset" "$ansiTransient" "$dependencyString" "$state" "$onlineVersion" "$ansiReset";
    fi
}

for projectName in $projectNames; do
    projectDirectory="${rootDir}/${projectName}"

    cd "${projectDirectory}" || {
        echo "Failed to cd to projectDirectory=${projectDirectory}"
        exit 1
    }

    if [ -f 'gradlew' ]; then
        subprojectNames="$( ./gradlew projects --console=plain -q | grep -E '^.\-\-\- ' | sed -E "s/^[^:']+':([a-zA-Z0-9_-]+)'"'.*$/\1/' )"
        for subprojectName in $subprojectNames; do
            trap 'exit' INT
            
            printf '%s=== %s : %s ===%s\n' "$ansiBold" "$projectName" "$subprojectName" "$ansiReset"
            
            if ./gradlew "$subprojectName":tasks --console=plain -q | grep -E '^jar\b' -q; then
                dependencyOutput="$( ./gradlew "$subprojectName":dependencies --console=plain -q )"
                echo "$dependencyOutput" | awk '{ print FNR, $0 }' | grep -E '^[0-9]+ \w*[rR]untimeClasspath' | cut -d ' ' -f '1-2' | while IFS=' ' read -r line conf; do
                    
                    printf '  %s\n' "$conf"
                    
                    confDependenciesOutput="$( echo "$dependencyOutput" | tail -n +"$(( line + 1 ))" )"
                    sectionEndLine="$( echo "$confDependenciesOutput" | awk '{ print FNR, $0 }' | grep -E '^[0-9]+ *$' | head -n 1 | sed -E 's/ +$//' )"
                    if [ -n "$sectionEndLine" ]; then
                        confDependenciesOutput="$( echo "$confDependenciesOutput" | head -n "$sectionEndLine" )"
                    fi
                    
                    directDependencies="$( echo "$confDependenciesOutput" | grep -E '^.\-\-\- ' | grep -E -o '[^ :]+:[^ ]+' | sort -u )"
                    transientDependencies="$( echo "$confDependenciesOutput" | grep -E '^(\| |     \\)' | grep -E -o '[^ :]+:[^ ]+' | sort -u; printf '\n' )"
                    
                    echo "$directDependencies" > "$tmp1"
                    echo "$transientDependencies" > "$tmp2"
                    transientOnlyDependencies="$( comm -13 "$tmp1" "$tmp2" )"
                    
                    for dependency in $directDependencies; do
                        handleDependency "$dependency" '1'
                    done
                    if [ "$arg" = '--all' ]; then
                        for dependency in $transientOnlyDependencies; do
                            handleDependency "$dependency" ''
                        done
                    fi
                    
                    printf '\n'
                done
            fi
        done
    fi
done

cd "${startDir}" || {
    echo "Failed to cd to startDir=${startDir}"
    exit 1
}
