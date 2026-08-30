FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:0552cdd6e413cafaeced409aa5d9cff954ea7128864504d26ce0abfd67e63be4

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

COPY build/libs/dp-manuell-behandling-all.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]