FROM ubuntu:18.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get --no-install-recommends -y install gnupg software-properties-common && \
  apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9 && \
  add-apt-repository 'deb https://cloud.r-project.org/bin/linux/ubuntu bionic-cran35/' && \
  apt-get update && \
  apt-get --no-install-recommends -y install \
    wget libssl-dev libssh2-1-dev libssl1.0.0 libcurl4-gnutls-dev libgit2-dev \
    openjdk-8-jre \
    libxml2-dev \
    r-base r-base-dev && \
  apt-get clean && \
  wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 -O phantomjs.tar.bz2 && \
  tar -xjf phantomjs.tar.bz2 && \
  mv phantomjs-*/bin/phantomjs /usr/local/bin/ && \
  rm -fR phantomjs*

RUN R -e "install.packages(c('devtools', 'dplyr', 'data.table', 'fs'), repos='https://cloud.r-project.org')" && \
  R -e "devtools::install_github('rstats-db/RSQLite')" && \
  R -e "install.packages(c('sqldf'), repos='https://cloud.r-project.org')"

COPY data/metadata.zip /data/

RUN cd /data && unzip metadata.zip

COPY ./jar/snzscrape.jar snzscrape.jar

COPY ./scripts/process.R process.R

COPY ./scripts/scrape.sh scrape.sh

RUN chmod ugo+x scrape.sh

CMD ["/scrape.sh"]
