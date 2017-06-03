#!/bin/bash

pwd
cron
x11vnc  -forever -usepw -create &

#Download ROBOT
cd /root && mkdir -p ROBOT && cd ROBOT && wget --no-check-certificate "$ARTIFACTS_HOST/$ROBOT_BIN-application.zip" && unzip "$ROBOT_BIN*"

#Run ROBOT
cd "/root/ROBOT/$ROBOT_BIN" && sed -i "s/robot.id=../robot.id=$ROBOT_ID/" config/application.properties
#cd "/root/ROBOT/$ROBOT_BIN" && sed -i "s/localhost/$ROBOT_DB_HOST/" config/application.properties
cd "/root/ROBOT/$ROBOT_BIN" && ./start-with-libs.sh