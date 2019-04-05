#!/usr/bin/env bash

java -jar snzscrape.jar work/downloads
R -e "source('process.R')"
chmod ugo+rwx /work/csv.zip