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
  wget https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_106.0.5249.103-1_amd64.deb -O chrome.deb && \
  wget https://chromedriver.storage.googleapis.com/106.0.5249.61/chromedriver_linux64.zip && \
  apt install -y ./chrome.deb && \
  unzip chromedriver_linux64.zip && \
  mv chromedriver /usr/local/bin/ && \
  rm /chrome* && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* 

RUN echo "MAKE = make -j8" >> /etc/R/Makeconf && \
  R -e "install.packages(c('RSQLite', 'dplyr', 'data.table', 'fs', 'sqldf'), repos='https://cran.rstudio.com')" 

COPY ./docker/scraper/data/metadata.zip /data/

RUN cd /data && unzip metadata.zip

COPY ./snzscrape/target/scala-2.13/snzscrape.jar snzscrape.jar

COPY ./docker/scraper/scripts/process.R process.R

COPY ./docker/scraper/scripts/scrape.sh scrape.sh

RUN chmod ugo+x scrape.sh

CMD ["/scrape.sh"]