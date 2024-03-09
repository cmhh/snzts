FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && \
  apt-get --no-install-recommends -y install \
    gnupg software-properties-common dirmngr unzip libxml2-dev libssl-dev libcurl4-openssl-dev lsb-release wget && \
  apt-get clean && \
  rm -fR /var/lib/apt/lists/* 

RUN wget -qO- https://cran.rstudio.com/bin/linux/ubuntu/marutter_pubkey.asc | tee -a /etc/apt/trusted.gpg.d/cran_ubuntu_key.asc && \
  add-apt-repository "deb https://cran.rstudio.com/bin/linux/ubuntu $(lsb_release -cs)-cran40/" && \
  apt-get update && \
  apt-get --no-install-recommends -y install r-base r-base-core r-base-dev r-recommended && \
  apt-get clean && \
  rm -fR /var/lib/apt/lists/* 

RUN echo "MAKE = make -j8" >> /etc/R/Makeconf && \
  R -e "install.packages(c('data.table', 'bit64', 'rvest', 'jsonlite', 'bit64', 'config', 'zip'), repos='https://cran.rstudio.com')" 
