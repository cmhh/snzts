FROM ghcr.io/graalvm/graalvm-ce:java11

COPY ./snzts/target/universal/snzts-0.3.0.zip snzts.zip

RUN microdnf install -y unzip && \ 
  unzip snzts.zip && \
  chmod ugo+rwx /snzts-0.3.0 && \
  rm -fR snzts.zip && \
  microdnf remove unzip

EXPOSE 9000

ENTRYPOINT ["/snzts-0.3.0/bin/snzts"]
