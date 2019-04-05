FROM postgis

RUN mkdir -p /tmp/psqldata

COPY csv.zip /tmp/psqldata/

RUN chmod -R ugo+rwx /tmp/psqldata

COPY ./z01_gis.sh /docker-entrypoint-initdb.d/

EXPOSE 5432
