FROM --platform=linux/amd64 amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 8005

ADD ./target/ms-items-0.0.1-SNAPSHOT.jar ms-items.jar

ENTRYPOINT [ "java", "-jar", "ms-items.jar" ]