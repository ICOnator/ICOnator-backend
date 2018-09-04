FROM openjdk:8-jdk-alpine

MAINTAINER Guilherme Sperb Machado <gsm@iconator.io>

ARG JAR_FILE
ARG JAR_FILE_NAME

ENV EXEC_COMMAND="java -Djava.security.egd=file:/dev/./urandom -jar ${JAR_FILE_NAME}"

ADD ${JAR_FILE} ${JAR_FILE_NAME}

RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*

CMD [ "sh", "-c", "${EXEC_COMMAND}" ]