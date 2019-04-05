#! /usr/bin/env bash
docker build -t postgis postgis
docker build -t snzts-scraper scraper
docker build -t snzts-backend backend
docker build -t snzts-api api
