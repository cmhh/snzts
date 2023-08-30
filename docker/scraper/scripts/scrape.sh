#!/usr/bin/env bash

tmp_dir=$(mktemp -d -t XXXXXXXXXX)
java -jar snzscrape.jar ${tmp_dir}/downloads
Rscript --vanilla /process.R ${tmp_dir}/downloads /data /work