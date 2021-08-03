FROM ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get --no-install-recommends -y install gnupg software-properties-common && \
  apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9 && \
  add-apt-repository 'deb https://cloud.r-project.org/bin/linux/ubuntu focal-cran40/' && \
  apt-get update && \
  apt-get --no-install-recommends -y install \
    wget curl libssl-dev libssh2-1-dev libssl1.1 libcurl4-gnutls-dev libgit2-dev unzip \
    openjdk-11-jre \
    libxml2-dev \
    r-base r-base-core r-base-dev r-recommended && \
  wget https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_92.0.4515.107-1_amd64.deb -O chrome.deb && \
  wget https://chromedriver.storage.googleapis.com/92.0.4515.107/chromedriver_linux64.zip && \
  apt install -y ./chrome.deb && \
  unzip chromedriver_linux64.zip && \
  mv chromedriver /usr/local/bin/ && \
  rm /chrome* && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* 

RUN R -e "install.packages(c('devtools', 'dplyr', 'data.table', 'fs'), repos='https://cloud.r-project.org')" && \
  R -e "devtools::install_github('rstats-db/RSQLite')" && \
  R -e "install.packages(c('sqldf'), repos='https://cloud.r-project.org')"  

COPY ./docker/scraper/data/metadata.zip /data/

RUN cd /data && unzip metadata.zip

COPY ./snzscrape/target/scala-2.13/snzscrape.jar snzscrape.jar

COPY ./docker/scraper/scripts/process.R process.R

COPY ./docker/scraper/scripts/scrape.sh scrape.sh

RUN chmod ugo+x scrape.sh

CMD ["/scrape.sh"]