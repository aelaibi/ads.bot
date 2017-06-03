
REM pre-setting
REM java -jar lib/${project.artifactId}-${project.version}.jar
java -cp .;./config;./lib/* Application --spring.profiles.active=prod $*

