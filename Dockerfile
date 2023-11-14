FROM arm64v8/openjdk:19

ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} gateway.jar

ENTRYPOINT ["java","-jar","/gateway.jar"]
