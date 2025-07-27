FROM eclipse-temurin:17

WORKDIR /app

# ローカルのソースコードをコンテナの/appにコピー
COPY . .

# Gradleでビルド
RUN ./gradlew bootJar

# 起動コマンド
CMD ["java", "-jar", "build/libs/spring-0.0.1-SNAPSHOT.jar"]
