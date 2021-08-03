#!/usr/bin/env bash

cd /tmp 
java -jar /snzscrape.jar downloads
R -e "source('/process.R')"