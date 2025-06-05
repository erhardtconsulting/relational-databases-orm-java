# -----------------------------
# Stage 1: Maven Build
# -----------------------------
FROM docker.io/library/maven:3.9.9-eclipse-temurin-21@sha256:933900d8738eab72ddebb7ad971fc9bca91ae6bc4c7b6d6bbc17fb3609f5e64b AS maven-build

WORKDIR /app

# Optimize maven cache
COPY pom.xml ./

# Only download dependencies for better caching
RUN mvn dependency:go-offline -B

# Copy sources
COPY src ./src

# Maven build
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Runtime
# -----------------------------
FROM docker.io/library/eclipse-temurin:21-jre@sha256:3e08d54ec5a8780227a87ef2458a26c27c4b110e4443d25f055fbe2f96907139

# Install dependencies
RUN set -eux; \
    apt-get update; \
    apt-get -y install \
      bash \
      tini; \
    apt-get clean

WORKDIR /app

# Copy jar file & docker-run.sh
COPY --from=maven-build /app/target/transferdemo-*.jar app.jar
COPY /docker-run.sh /

# JVM-Optimierungen für Container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expose port
EXPOSE 8080

# Entrypoint & run
ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["/docker-run.sh"]