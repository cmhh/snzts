FROM openjdk:8-jre

COPY snzts-0.2.1.zip snzts.zip

RUN unzip snzts.zip && rm -fR snzts.zip

EXPOSE 9000

ENTRYPOINT ["/snzts-0.2.1/bin/snzts"]