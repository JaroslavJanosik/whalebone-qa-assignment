FROM mcr.microsoft.com/playwright/java:v1.58.0-jammy

WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
COPY testng.xml .

ENV HEADED=false
ENV MAVEN_OPTS="-Dmaven.repo.local=/root/.m2/repository"

CMD ["bash", "-lc", "set -e; status=0; mvn -q clean test -Dheaded=${HEADED} || status=$?; mvn -q allure:report || true; exit $status"]
