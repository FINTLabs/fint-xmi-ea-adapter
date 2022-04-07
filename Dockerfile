FROM gradle:7.2-jdk17 as builder
USER root
COPY . .
ARG apiVersion
RUN gradle --no-daemon build

FROM gcr.io/distroless/java17
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-xmi-ea-adapter-*.jar /data/fint-xmi-ea-adapter.jar
CMD ["/data/fint-xmi-ea-adapter.jar"]
