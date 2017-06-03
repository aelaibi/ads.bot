#!/bin/sh

DIR=`dirname $0`
cd $DIR
java -Xms128m -Xmx512m -Xss512k -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -cp .:./config:./lib/* Application --spring.profiles.active=prod $*
