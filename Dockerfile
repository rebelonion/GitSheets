FROM openjdk:21-jdk-slim as builder

RUN apt-get update && \
    apt-get install -y curl git && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY gradlew ./
COPY gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts ./
COPY settings.gradle.kts ./
COPY gradle.properties ./

RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies

COPY src/ src/

RUN ./gradlew --no-daemon linkReleaseExecutableNativeApp

FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y curl git su-exec && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/bin/nativeApp/releaseExecutable/GitSheets.kexe /usr/local/bin/gitsheets
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

RUN chmod +x /usr/local/bin/gitsheets && \
    chmod +x /usr/local/bin/entrypoint.sh

WORKDIR /workspace

ENV PUID=1000
ENV PGID=1000

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]