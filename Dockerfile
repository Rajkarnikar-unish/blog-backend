FROM openjdk:17-jdk

COPY target/blog-backend-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "blog-backend-0.0.1-SNAPSHOT.jar"]