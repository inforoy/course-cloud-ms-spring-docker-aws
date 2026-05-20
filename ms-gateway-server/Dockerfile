FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 8090
####192.168.1.44
ADD ./target/ms-gateway-server-0.0.1-SNAPSHOT.jar ms-gateway-server.jar

ENTRYPOINT [ "java", "-jar", "ms-gateway-server.jar" ]