# FROM maven:3.8.4-openjdk-17-slim AS build
# WORKDIR /app
# COPY shopapp-backend /app/shopapp-backend
# RUN mvn package -f /app/shopapp-backend/pom.xml
# Sử dụng OpenJDK để chạy ứng dụng
FROM openjdk:17-slim

# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép file JAR đã tạo sẵn từ hệ thống của bạn vào Docker image
COPY target/shopapp-0.0.1-SNAPSHOT.jar app.jar
# COPY --from=build shopapp-backend/target/shopapp-0.0.1-SNAPSHOT.jar app.jar

# COPY --from=build shopapp-backend/uploads uploads
# Mở cổng 8088 để truy cập ứng dụng
EXPOSE 8088

# Lệnh để chạy ứng dụng
CMD ["java", "-jar", "app.jar"]


#docker build -t shopapp-spring:1.0.0 -f ./Dockerfile .
#docker login
#create vansinl/shopapp-spring:1.0.0 repositoy on DockerHub
#docker tag shopapp-spring:1.0.0 vansinl/shopapp-spring:1.1.6
#docker push vansinl/shopapp-spring:1.1.6