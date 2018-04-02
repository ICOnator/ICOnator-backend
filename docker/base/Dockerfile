FROM iconator/ubuntu-oracle-java8:latest

MAINTAINER Guilherme Sperb Machado <gsm@iconator.io>
USER root

ENV ICONATOR_WORKDIR=/ICOnator
ENV ICONATOR_WORKDIR_BIN=/ICOnator/bin
ENV ICONATOR_WORKDIR_SCRIPTS=/ICOnator/scripts
WORKDIR ${ICONATOR_WORKDIR}

ENV ICONATOR_SERVICES_CORE=services/core/build/libs/core.jar
ENV ICONATOR_SERVICES_EMAIL=services/email/build/libs/email.jar
ENV ICONATOR_SERVICES_MONITOR=services/monitor/build/libs/monitor.jar
ENV ICONATOR_SERVICES_RATES=services/rates/build/libs/rates.jar

RUN mkdir -p ${ICONATOR_WORKDIR} && \
    mkdir -p ${ICONATOR_WORKDIR_BIN} && \
    mkdir -p ${ICONATOR_WORKDIR_SCRIPTS}

COPY ${ICONATOR_SERVICES_CORE} ${ICONATOR_SERVICES_EMAIL} ${ICONATOR_SERVICES_MONITOR} ${ICONATOR_SERVICES_RATES} ${ICONATOR_WORKDIR_BIN}/

RUN ln -s ${ICONATOR_WORKDIR_BIN}/core.jar /etc/init.d/ICOnator-core && \
    ln -s ${ICONATOR_WORKDIR_BIN}/email.jar /etc/init.d/ICOnator-email && \
    ln -s ${ICONATOR_WORKDIR_BIN}/monitor.jar /etc/init.d/ICOnator-monitor && \
    ln -s ${ICONATOR_WORKDIR_BIN}/rates.jar /etc/init.d/ICOnator-rates

ENTRYPOINT ["/bin/sh", "-c"]