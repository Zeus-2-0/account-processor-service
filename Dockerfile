FROM openjdk:21
EXPOSE 8099
# ADD target/*.jar *.jar
#ENTRYPOINT ["sh", "-c", "java -jar /*.jar"]
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.brihaspathee.zeus.AccountProcessorApplication"]