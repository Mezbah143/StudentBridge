#!/bin/bash

cp -R frontend/* "/Users/mezbahuddin/Desktop/apache-tomcat-10.1.54/webapps/StudentBridge/"

javac -cp "./docs/mysql-connector-j-9.3.0.jar:./docs/servlet-api.jar" -d bin Backend/*.java

cp bin/*.class "/Users/mezbahuddin/Desktop/apache-tomcat-10.1.54/webapps/StudentBridge/WEB-INF/classes/"

echo "Deployment Complete!"



