#! /usr/bin/env bash

echo "GID=$(id -g)" > .env
echo "UID=$(id -u)" >> .env
echo "APPLICATION_SECRET=\"$(head -c 32 /dev/urandom | base64)\"" >> .env
echo "PG_PASS=\"$(head -c 8 /dev/urandom | base64)\"" >> .env
echo "PG_UID=webuser" >> .env
echo "PG_PWD=webuser" >> .env 
echo "POSTGRAPHILE_DB=postgres://webuser:webuser@backend:5432/snzts" >> .env
echo "POSTGRAPHILE_BASE=/snztsgql" >> .env
echo "DB_URI=postgres://webuser:webuser@backend:5432/snzts" >> .env
echo "DB_SCHEMA=public" >> .env
echo "DB_ANON_ROLE=webuser" >> .env
chmod 600 .env