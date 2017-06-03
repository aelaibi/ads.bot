#!/bin/bash

#vnc_connection
VNC_PORT="590"${DISPLAY:1}

##change vnc password
echo "change vnc password!"
(echo $VNC_PW && echo $VNC_PW) | vncpasswd

##start vncserver and noVNC webclient

vncserver -kill :1 && rm -rfv /tmp/.X* ; echo "remove old vnc locks to be a reattachable container"
vncserver $DISPLAY -depth $VNC_COL_DEPTH -geometry $VNC_RESOLUTION
sleep 1

pwd
cron
#crontab settings
echo "00 */1 * * * find /tmp/screenshot* -mmin +20 -delete" >> mycron \
    && echo "00 */4 * * * find /tmp/anonymous*  -mmin +180 -exec rm -rf {} \;" >> mycron \
    && echo "00 */1 * * * find /root/ROBOT/$ROBOT_BIN/media/*  -mmin +20 -exec rm -rf {} \;" >> mycron \
    && echo "* * * * * echo \$(date) >> /root/cron.log" >> mycron \
    && echo "* 01 * * * rm -rf /root/cron.log" >> mycron \
    && echo "*/5 * * * * export DISPLAY=$DISPLAY && cd /root/ROBOT/$ROBOT_BIN && bash /root/scripts/pixiJavaup.sh >> /var/log/autoStartRobot.log 2>&1" >> mycron \
    && crontab mycron && rm mycron

#Filebeat restart
service filebeat restart

#Download ROBOT
cd /root && rm -rf ROBOT && mkdir -p ROBOT && cd ROBOT && wget "$ARTIFACTS_HOST/$ROBOT_BIN-application.zip" && unzip "$ROBOT_BIN*"

#Run ROBOT
cd "/root/ROBOT/$ROBOT_BIN" && sed -i "s/robot.id=./robot.id=$ROBOT_ID/" config/application.properties
#cd "/root/ROBOT/$ROBOT_BIN" && sed -i "s/localhost/$ROBOT_DB_HOST/" config/application.properties
#if [ -z ${EAJMX+x} ]; then cd "/root/ROBOT/$ROBOT_BIN" && ./start-with-libs.sh; else cd "/root/ROBOT/$ROBOT_BIN" && ./start-with-jmx.sh; fi
cd "/root/ROBOT/$ROBOT_BIN" &&  (nohup ./start-with-libs.sh > /dev/null 2>&1 &)
sleep 15
pgrep java
##log connect options
echo -e "\n------------------ VNC environment started ------------------"
echo -e "\nVNCSERVER started on DISPLAY= $DISPLAY \n\t=> connect via VNC viewer with $VNC_IP:$VNC_PORT"
#echo -e "\nnoVNC HTML client started:\n\t=> connect via http://$VNC_IP:$NO_VNC_PORT/vnc_auto.html?password=..."

for i in "$@"
do
case $i in
    # if option `-t` or `--tail-log` block the execution and tail the ROBOT log
    -t|--tail-log)
    ls /root/ROBOT/$ROBOT_BIN/logs/
    tail -f /root/ROBOT/$ROBOT_BIN/logs/robot.log
    tail -f /root/ROBOT/config/application.properties
    ;;
    *)
    # unknown option ==> call command
    exec $i
    ;;
esac
done