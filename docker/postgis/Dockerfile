FROM postgres:alpine

ENV POSTGIS_VERSION 2.4.4
ENV POSTGIS_SHA256 0663efb589210d5048d95c817e5cf29552ec8180e16d4c6ef56c94255faca8c2

RUN set -ex && \
  apk add --no-cache --virtual .fetch-deps \
    ca-certificates openssl tar && \
  wget -O postgis.tar.gz \
    "https://download.osgeo.org/postgis/source/postgis-2.4.4.tar.gz" && \
  echo "$POSTGIS_SHA256 *postgis.tar.gz" | sha256sum -c - && \
  mkdir -p /usr/src/postgis && \
  tar --extract --file postgis.tar.gz \
    --directory /usr/src/postgis --strip-components 1 && \
  rm postgis.tar.gz && \
  cd /usr/src/postgis && \
  apk add --no-cache --virtual .build-deps \
    autoconf automake cmake g++ json-c-dev libtool libxml2-dev make perl && \
  apk add --no-cache --virtual .build-deps-testing \
    --repository http://dl-cdn.alpinelinux.org/alpine/edge/testing \
    gdal-dev geos-dev proj4-dev protobuf-c-dev && \
  ./autogen.sh && \
  ./configure && make && make install && \
  apk add --no-cache --virtual .postgis-rundeps \
      json-c && \
  apk add --no-cache --virtual .postgis-rundeps-testing \
    --repository http://dl-cdn.alpinelinux.org/alpine/edge/testing \
    geos gdal proj4 protobuf-c && \
  cd / && \
  rm -rf /usr/src/postgis && \
  apk del .fetch-deps .build-deps .build-deps-testing

COPY ./initdb-postgis.sh /docker-entrypoint-initdb.d/postgis.sh
COPY ./update-postgis.sh /usr/local/bin
