FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
#EXPOSE 8761

ADD ./target/ms-products-0.0.1-SNAPSHOT.jar ms-products.jar

ENTRYPOINT [ "java", "-jar", "ms-products.jar" ]