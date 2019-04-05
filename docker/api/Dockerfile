FROM openjdk:8-jre

COPY snzts-0.1.0.zip snzts.zip

RUN unzip snzts.zip && rm -fR snzts.zip

EXPOSE 9000

ENTRYPOINT ["/snzts-0.1.0/bin/snzts"]