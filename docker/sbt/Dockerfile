FROM ubuntu:23.10

ARG SBT_VERSION=1.9.9
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
  apt-get --no-install-recommends -y install \
    wget unzip ca-certificates openjdk-11-jdk && \
  wget https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz && \
  tar -xvf sbt-${SBT_VERSION}.tgz && \
  ln -s /sbt/bin/sbt /usr/bin/sbt && \
  rm sbt-${SBT_VERSION}.tgz 

  CMD ["sbt", "assembly"]