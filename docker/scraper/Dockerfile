FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
  apt-get --no-install-recommends -y install gnupg software-properties-common dirmngr wget  \
    wget curl libssl-dev libssh2-1-dev libssl3 libcurl4-gnutls-dev libgit2-dev unzip \
    openjdk-11-jre \
    libxml2-dev && \
  wget -qO- https://cloud.r-project.org/bin/linux/ubuntu/marutter_pubkey.asc | tee -a /etc/apt/trusted.gpg.d/cran_ubuntu_key.asc && \
  add-apt-repository "deb https://cloud.r-project.org/bin/linux/ubuntu $(lsb_release -cs)-cran40/" && \
  apt-get update && \
  apt-get --no-install-recommends -y install r-base r-base-core r-base-dev r-recommended && \
  wget https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_116.0.5845.110-1_amd64.deb -O chrome.deb && \
  wget https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/116.0.5845.96/linux64/chromedriver-linux64.zip && \
  apt install -y ./chrome.deb && \
  unzip chromedriver-linux64.zip && \
  mv chromedriver-linux64/chromedriver /usr/local/bin/ && \
  rm -fR /chrome* && \
  apt-get clean && \
  rm -fR /var/lib/apt/lists/* 

RUN echo "MAKE = make -j8" >> /etc/R/Makeconf && \
  R -e "install.packages(c('data.table', 'bit64'), repos='https://cran.rstudio.com')" 

COPY ./docker/scraper/data /data

COPY ./snzscrape/target/scala-2.13/snzscrape.jar snzscrape.jar

COPY ./docker/scraper/scripts/process.R process.R

COPY ./docker/scraper/scripts/scrape.sh scrape.sh

RUN chmod ugo+x scrape.sh

CMD ["/scrape.sh"]