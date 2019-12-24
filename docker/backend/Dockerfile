FROM postgres:alpine

RUN set -ex && \
  apk add --no-cache --virtual .fetch-deps zip && \
  apk del .fetch-deps 

RUN mkdir -p /tmp/psqldata 

COPY csv.zip /tmp/psqldata/

RUN chmod -R ugo+rwx /tmp/psqldata

COPY ./z01_gis.sh /docker-entrypoint-initdb.d/

EXPOSE 5432
