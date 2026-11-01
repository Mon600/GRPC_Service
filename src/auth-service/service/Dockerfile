FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/src/auth-service/service

COPY src/auth-service/service/pom.xml ./pom.xml
COPY src/auth-service/service/src ./src
COPY src/contracts/proto/ /workspace/src/contracts/proto/

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package spring-boot:repackage

FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/src/auth-service/service/target/*.jar ./app.jar
USER app
EXPOSE 9090
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
# sh -c + exec: подстановка JAVA_OPTS и корректная доставка SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
