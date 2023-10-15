FROM eclipse-temurin:17-jre
ARG SLACK_TOKEN
ENV SLACK_TOKEN=${SLACK_TOKEN}
WORKDIR /app
COPY app/build/libs/app-all.jar .
EXPOSE 8080
CMD ["java", "-jar", "app-all.jar"]