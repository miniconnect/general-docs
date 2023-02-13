#!/bin/sh

set -e

docker build -t miniconnect/holodb-example-wikidata "$( dirname "$0" )"
