# ---- Etapa de compilacion ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Etapa de ejecucion ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system agro && adduser --system --ingroup agro agro \
    && mkdir -p /app/uploads && chown agro:agro /app/uploads
USER agro

ENV UPLOAD_DIR=/app/uploads

COPY --from=build /app/target/agroconecta-backend-1.0.0.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
