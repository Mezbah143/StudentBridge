# syntax=docker/dockerfile:1

FROM tomcat:10.1-jdk17-temurin AS build

WORKDIR /app

COPY Backend ./Backend
COPY frontend ./frontend
COPY index.html ./index.html
COPY server/mysql-connector-j-9.3.0.jar ./server/mysql-connector-j-9.3.0.jar

RUN mkdir -p /tmp/studentbridge/WEB-INF/classes /tmp/studentbridge/WEB-INF/lib \
    && javac -encoding UTF-8 \
        -cp "/usr/local/tomcat/lib/servlet-api.jar:server/mysql-connector-j-9.3.0.jar" \
        -d /tmp/studentbridge/WEB-INF/classes \
        Backend/*.java \
    && cp index.html /tmp/studentbridge/index.html \
    && cp -R frontend /tmp/studentbridge/frontend \
    && cp server/mysql-connector-j-9.3.0.jar /tmp/studentbridge/WEB-INF/lib/

FROM tomcat:10.1-jdk17-temurin

ENV CATALINA_HOME=/usr/local/tomcat
ENV PORT=8080
ENV CATALINA_OPTS="-Dfile.encoding=UTF-8"

RUN rm -rf "$CATALINA_HOME"/webapps/*

COPY --from=build /tmp/studentbridge "$CATALINA_HOME"/webapps/ROOT
COPY render-start.sh /usr/local/bin/render-start.sh

RUN chmod +x /usr/local/bin/render-start.sh

EXPOSE 8080

CMD ["/usr/local/bin/render-start.sh"]

