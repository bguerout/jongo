FROM maven:3.5.2-jdk-8
VOLUME /opt/jongo/maven

COPY . /opt/jongo/sources

WORKDIR /opt/jongo/sources
