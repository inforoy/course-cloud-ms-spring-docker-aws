FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
#EXPOSE 8005

ADD ./target/ms-users-0.0.1-SNAPSHOT.jar ms-users.jar

ENTRYPOINT [ "java", "-jar", "ms-users.jar" ]