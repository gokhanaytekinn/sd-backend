# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Proje bağımlılıklarını ve kaynak kodlarını kopyala
COPY pom.xml .
COPY src ./src

# Projeyi derle ve testleri atla (hızlı build için)
RUN mvn clean package -DskipTests

# Run Stage (Çalıştırma Aşaması)
FROM eclipse-temurin:17-jre
WORKDIR /app

# İlk aşamadan oluşan jar dosyasını kopyala
COPY --from=build /app/target/sd-backend-1.0.0.jar app.jar

# Uygulamanın çalışacağı port
EXPOSE 8080

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]
