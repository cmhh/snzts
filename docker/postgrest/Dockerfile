FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

RUN  apt-get update && apt-get -y dist-upgrade && \
  apt-get install -y --no-install-recommends curl ca-certificates xz-utils libpq-dev && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* 

RUN curl -sL https://github.com/PostgREST/postgrest/releases/download/v9.0.0/postgrest-v9.0.0-linux-static-x64.tar.xz | \
    tar xJvf - -C /usr/local/bin 

EXPOSE 3000

CMD echo "db-uri = \"${DB_URI}\"" > /tmp/postgrest.conf && \
  echo "db-schema = \"${DB_SCHEMA}\"" >> /tmp/postgrest.conf && \
  echo "db-anon-role = \"${DB_ANON_ROLE}\"" >> /tmp/postgrest.conf && \
  echo "openapi-server-proxy-uri = \"${POSTGREST_URL}\"" >> /tmp/postgrest.conf && \
  postgrest /tmp/postgrest.conf && \
  tail -f /dev/null