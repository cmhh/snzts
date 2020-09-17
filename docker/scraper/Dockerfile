FROM ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get --no-install-recommends -y install gnupg software-properties-common && \
  apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9 && \
  add-apt-repository 'deb https://cloud.r-project.org/bin/linux/ubuntu focal-cran40/' && \
  apt-get update && \
  apt-get --no-install-recommends -y install \
    wget libssl-dev libssh2-1-dev libssl1.1 libcurl4-gnutls-dev libgit2-dev \
    openjdk-8-jre \
    libxml2-dev \
    firefox \
    r-base r-base-core r-base-dev r-recommended && \
  apt-get clean && \
  wget https://github.com/mozilla/geckodriver/releases/download/v0.27.0/geckodriver-v0.27.0-linux64.tar.gz -O geckodriver.tar.gz && \
  tar -xvf geckodriver.tar.gz && \
  mv geckodriver /usr/local/bin/ && \
  chmod +x /usr/local/bin/geckodriver && \
  rm -fR gecko* 

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