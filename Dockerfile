FROM maven:3.5.2-jdk-8
VOLUME /opt/jongo/maven

RUN apt-get update \
    && apt-get install -qy gnupg2 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /tmp/* \
    && rm -rf /var/tmp/*

COPY . /opt/jongo/sources

WORKDIR /opt/jongo/sources

ENTRYPOINT ["bash", "./bin/cli.sh"]