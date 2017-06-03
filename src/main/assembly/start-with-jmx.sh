#!/bin/sh

DIR=`dirname $0`
cd $DIR
java  -Xms128m -Xmx512m -Xss512k -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1098 -cp .:./config:./lib/* Application --spring.profiles.active=prod $*
