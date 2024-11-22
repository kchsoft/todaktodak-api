# 빌드 스테이지
FROM openjdk:21-slim AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew clean build

# --------------------------------------------------------------------

FROM openjdk:21-slim

WORKDIR /app

ARG BUILDDIR=/app/build/libs

COPY --from=build ${BUILDDIR}/*.jar ./

# jar파일의 첫번째 행을 실행시키도록 함
CMD ["sh", "-c", "java -jar *SNAPSHOT.jar"]