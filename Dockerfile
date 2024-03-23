FROM amazoncorretto:21

WORKDIR /app
COPY ./target/evrete-showcase.jar /app
ENTRYPOINT java -server -jar /app/evrete-showcase.jar
