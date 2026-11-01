FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace/src/store

COPY src/store/ ./
COPY src/contracts/proto/ /workspace/src/contracts/proto/

RUN sed -i 's/\r$//' gradlew
RUN --mount=type=cache,target=/root/.gradle \
    sh ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app app
WORKDIR /app
RUN mkdir -p /app/logs && chown -R app:app /app
COPY --from=build --chown=app:app /workspace/src/store/build/libs/*.jar ./app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
# sh -c + exec: подстановка JAVA_OPTS и корректная доставка SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
