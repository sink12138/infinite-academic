FROM openjdk:17
WORKDIR /app/
ADD target/*.jar app.jar
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
