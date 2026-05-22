FROM --platform=linux/amd64 amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 8761

ADD ./target/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar

ENTRYPOINT [ "java", "-jar", "eureka-server.jar" ]