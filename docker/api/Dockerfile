FROM ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
  apt-get --no-install-recommends -y install openjdk-11-jre unzip

COPY ./snzts/target/universal/snzts-0.3.0.zip /snzts.zip

RUN unzip snzts.zip && \
  chmod ugo+rwx /snzts-0.3.0 && \
  rm -fR snzts.zip

EXPOSE 9000

ENTRYPOINT ["/snzts-0.3.0/bin/snzts"]
