#!/bin/sh

startDir="$( pwd )"
cd "$( dirname "$0" )"
./gradlew build
docker build -t miniconnect/holodb-example-customized .
cd "$startDir"
