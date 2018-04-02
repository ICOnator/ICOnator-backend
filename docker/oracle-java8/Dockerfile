FROM ubuntu:artful
MAINTAINER Guilherme Sperb Machado <gsm@iconator.io>
USER root
RUN apt-get -qqy update && \
    apt-get -qqy install software-properties-common && \
    add-apt-repository ppa:webupd8team/java && \
    apt-get -qqy update && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get -qqy update && \
    apt-get -qqy install oracle-java8-installer oracle-java8-set-default