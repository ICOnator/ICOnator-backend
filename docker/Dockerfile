FROM openjdk:8-jdk-alpine

MAINTAINER Guilherme Sperb Machado <gsm@iconator.io>

ARG JAR_FILE
ARG JAR_FILE_NAME

ENV EXEC_COMMAND="java -Djava.security.egd=file:/dev/./urandom -jar ${JAR_FILE_NAME}"

ADD ${JAR_FILE} ${JAR_FILE_NAME}

ENTRYPOINT [ "sh", "-c", "${EXEC_COMMAND}" ]