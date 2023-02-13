#!/bin/sh

set -e

docker build -t miniconnect/holodb-example-standalone "$( dirname "$0" )"
