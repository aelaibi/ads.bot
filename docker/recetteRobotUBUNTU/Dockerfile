FROM ubuntu:16.04 
MAINTAINER AEL <abdelhaq.elaibi@labs.com>

ENV DISPLAY :1

ENV VNC_COL_DEPTH 24
ENV VNC_RESOLUTION 1600x900
ENV VNC_PW pixitrend

RUN apt-get update 
RUN apt-get install -y wget cron vim unzip bzip2 && apt-get clean


RUN wget --no-check-certificate https://ftp.mozilla.org/pub/firefox/releases/47.0.1/linux-x86_64/en-US/firefox-47.0.1.tar.bz2 \
	&& tar -xjvf   firefox-47.0.1.tar.bz2  && ls \
	&& rm -rf  /opt/firefox \
	&& mv firefox /opt/firefox47 \
	&& ln -s /opt/firefox47/firefox /usr/bin/firefox \
	#&& apt install -yq --no-install-recommends libgtk-3-0 libdbus-glib-1-2 \
	&& apt-get clean



ENV JAVA_VERSION 8u111
ENV JAVA_HOME /opt/java

RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" \
	http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jre-8u111-linux-x64.tar.gz \
	&& mkdir -p $JAVA_HOME \
	&& tar -zxf jre-8u111-linux-x64.tar.gz -C $JAVA_HOME \
	&& rm -rf jre-8u111-linux-x64.tar.gz \
	&& update-alternatives --install "/usr/bin/java" "java" "$JAVA_HOME/jre1.8.0_111/bin/java" 1 \
    && update-alternatives --install "/usr/bin/javaws" "javaws" "$JAVA_HOME/jre1.8.0_111/bin/javaws" 1 


## Install FFMPEG

RUN apt-get install -yq --no-install-recommends  ffmpeg && apt-get clean
## python is used for the user interface
RUN apt-get install -yq --no-install-recommends  python \
	&& apt-get clean
# Install YOUTUBE-DL
RUN wget --no-check-certificate https://yt-dl.org/downloads/latest/youtube-dl -O /usr/local/bin/youtube-dl \
	&& chmod a+rx /usr/local/bin/youtube-dl


RUN echo "* * * * * echo \$(date) >> /root/cron.log" >> mycron \
	&& echo "00 */1 * * * find /tmp/screenshot* -mmin +20 -delete" >> mycron \
    && echo "00 */4 * * * find /tmp/anonymous*  -mmin +180 -exec rm -rf {} \;" >> mycron \
    && echo "00 */1 * * * find /root/ROBOT/ads.robot-0.0.1-SNAPSHOT/media/*  -mmin +20 -exec rm -rf {} \;" >> mycron \
    && echo "00 1 * * * rm -rf /root/cron.log" >> mycron \
    && crontab mycron && rm mycron


RUN apt-get update 
RUN apt-get upgrade -y 
RUN apt-get install -y --no-install-recommends lxde


# some useful x apps. 
RUN apt-get install -y x11-utils

# some utils to have proper menus, mime file types etc.
RUN apt-get install -y --no-install-recommends xdg-utils
RUN apt-get install -y menu
RUN apt-get install -y menu-xdg
RUN apt-get install -y mime-support
RUN apt-get install -y desktop-file-utils

RUN apt-get install -y  vnc4server && apt-get clean

#FILEBEAT

RUN echo "deb http://packages.elastic.co/beats/apt stable main" |  tee -a /etc/apt/sources.list.d/beats.list \
    && wget -qO - https://packages.elastic.co/GPG-KEY-elasticsearch | apt-key add -
RUN apt-get update
RUN apt-get install -y filebeat && apt-get clean




ADD .vnc /root/.vnc
ADD .config /root/.config
ADD scripts /root/scripts
RUN chmod +x /root/.vnc/xstartup /etc/X11/xinit/xinitrc /root/scripts/*.sh

RUN mkdir -p /etc/pki/tls/certs \
    && mv /root/scripts/logstash-forwarder.crt /etc/pki/tls/certs/logstash-forwarder.crt \
    && mv /etc/filebeat/filebeat.yml /etc/filebeat/filebeat.yml.old \
    && mv /root/scripts/filebeat.yml /etc/filebeat/filebeat.yml \
    && service filebeat restart

ENV ARTIFACTS_HOST https://www.dropbox.com/s/2tza28rj9kc7uf2
ENV ROBOT_BIN ads.robot-0.0.1-SNAPSHOT
ENV ROBOT_ID 1000
EXPOSE 5901
ENTRYPOINT ["/root/scripts/vnc_startup.sh"]
CMD ["--tail-log"]