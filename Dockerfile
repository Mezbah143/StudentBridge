FROM tomcat:10.1-jdk17-temurin

ENV CATALINA_HOME=/usr/local/tomcat
ENV PORT=8080

WORKDIR /opt/studentbridge-build

RUN rm -rf "$CATALINA_HOME"/webapps/* \
    && mkdir -p "$CATALINA_HOME"/webapps/StudentBridge/WEB-INF/classes \
    && mkdir -p "$CATALINA_HOME"/webapps/StudentBridge/WEB-INF/lib

COPY index.html "$CATALINA_HOME"/webapps/StudentBridge/
COPY frontend "$CATALINA_HOME"/webapps/StudentBridge/frontend
COPY Backend /opt/studentbridge-build/Backend
COPY server/servlet-api.jar /opt/studentbridge-build/server/servlet-api.jar
COPY server/mysql-connector-j-9.3.0.jar "$CATALINA_HOME"/webapps/StudentBridge/WEB-INF/lib/
COPY render-start.sh /usr/local/bin/render-start.sh

RUN javac -encoding UTF-8 \
    -cp "$CATALINA_HOME/webapps/StudentBridge/WEB-INF/lib/mysql-connector-j-9.3.0.jar:/opt/studentbridge-build/server/servlet-api.jar" \
    -d "$CATALINA_HOME"/webapps/StudentBridge/WEB-INF/classes \
    /opt/studentbridge-build/Backend/*.java \
    && chmod +x /usr/local/bin/render-start.sh \
    && rm -rf /opt/studentbridge-build

EXPOSE 8080

CMD ["/usr/local/bin/render-start.sh"]
