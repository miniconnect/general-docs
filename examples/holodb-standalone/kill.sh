#!/bin/sh

set -e

selfDir="$( dirname -- "$( realpath "$0" )" )"
name="$( cat "${selfDir}/name.txt" )"

docker ps --filter "ancestor=miniconnect/${name}:latest" --format='{{.ID}}' --no-trunc | \
while read id; do
    docker rm -f $id
done
