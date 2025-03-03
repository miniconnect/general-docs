#!bin/sh

# this is a snippet collection, no resason to run it
exit 1;

# run the holodb app to debug:
JAVA_TOOL_OPTIONS='-Dlog4j2.configurationFile=./etc/log4j2/log4j2-develop.xml' ./gradlew holodb:app:run --quiet --console=plain --args='../../../general-docs/examples/holodb-standalone/config.yaml'
