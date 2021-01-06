FROM oracle/graalvm-ce:20.3.0-java8 

COPY snzts-0.3.0.zip snzts.zip

RUN yum install -y unzip && unzip snzts.zip && rm -fR snzts.zip && yum -y remove unzip

EXPOSE 9000

ENTRYPOINT ["/snzts-0.3.0/bin/snzts"]