FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 9100

ADD ./target/ms-oauth-0.0.1-SNAPSHOT.jar ms-oauth.jar

ENTRYPOINT [ "java", "-jar", "ms-oauth.jar" ]