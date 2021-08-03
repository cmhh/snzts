#! /usr/bin/env bash

echo "GID=$(id -g)" > .env
echo "UID=$(id -u)" >> .env
echo "APPLICATION_SECRET=\"$(head -c 32 /dev/urandom | base64)\"" >> .env
echo "PG_PASS=\"$(head -c 8 /dev/urandom | base64)\"" >> .env
chmod 600 .env