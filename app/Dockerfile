# Build stage - Amazon Corretto 21 + Maven
FROM amazoncorretto:21-al2023-headless AS builder
WORKDIR /build

# Copy Maven wrapper or use system Maven
COPY pom.xml .
RUN dnf install -y maven && dnf clean all

# Download dependencies (cached layer)
RUN mvn -B dependency:go-offline -DskipTests || true

COPY src ./src
RUN mvn -B package -DskipTests -q

# Runtime stage - Amazon Corretto 21 Alpine
FROM amazoncorretto:21-alpine
WORKDIR /app

RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

COPY --from=builder /build/target/app-*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
