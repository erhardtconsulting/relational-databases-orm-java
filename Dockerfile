# -----------------------------
# Stage 1: Maven Build
# -----------------------------
FROM docker.io/library/maven:3.9.9-eclipse-temurin-21@sha256:3a4ab3276a087bf276f79cae96b1af04f53731bec53fb2e651aca79e4b10211e AS maven-build

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
FROM docker.io/library/eclipse-temurin:21-jre@sha256:7a65df4b22d2de92d4e04056e884f3b9122d70b21e2847fd66084278bd0ce037

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