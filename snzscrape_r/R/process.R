library(data.table)
library(bit64)

source("R/utils.R")

# globals ----------------------------------------------------------------------
wd <- config::get("download_directory")
od <- config::get("output_directory")

# setup ------------------------------------------------------------------------
create_if_not(od)

# import csv files -------------------------------------------------------------
message("importing raw data...")
data <- import_tsm(wd)

# import metadata --------------------------------------------------------------
message("importing metadata...")
subject <- readRDS("data/subject.rds") |> data.table::setDT()
family  <- readRDS("data/family.rds")  |> data.table::setDT()
series  <- readRDS("data/series.rds")  |> data.table::setDT()
setkeyv(series, c("subject_code", "family_code", "family_nbr", "series_code"))

# subset data and metadata -----------------------------------------------------
message("removing data rows without corresponding metadata...")
refs <- data[, .(series_code)] |> unique()
refs <- refs[series[, .(series_code)], on = "series_code", nomatch = 0]
data <- data[refs, on = "series_code", nomatch = 0] 
setkeyv(data, c("series_code", "period"))

message("removing metadata rows without corresponding data...")
series <- series[refs, on = "series_code", nomatch = 0]
series[, unit_text := sapply_(unit_text, recode_unit)]
setkey(series, series_code)

family_codes <- series[, c("family_code", "family_nbr")] |> unique()  
subject_codes <- series[, .(subject_code)] |> unique()

family <- family[family_codes, on = c("family_code", "family_nbr"), nomatch = 0]
setkeyv(family, c("family_code", "family_nbr"))
  
subject <- subject[subject_codes, on = "subject_code", nomatch = 0]
setkey(subject, subject_code)

# write to disk ----------------------------------------------------------------
message("saving output...")
data.table::fwrite(
  subject, 
  file = sprintf("%s/subject.csv.gz", od)
)

data.table::fwrite(
  family, 
  file = sprintf("%s/family.csv.gz", od)
)

data.table::fwrite(
  series, 
  file = sprintf("%s/series.csv.gz", od)
)

data.table::fwrite(
  data, 
  file = sprintf("%s/data.csv.gz", od), 
  quote = FALSE
)

zip::zip(
  sprintf("%s/csv.zip", od), 
  dir(od, pattern = "*.csv.gz", full.names = TRUE),
  include_directories = FALSE,
  mode = "cherry-pick"
)

# tidy up ----------------------------------------------------------------------
message("tidying up...")
unlink(dir("downloads", full.names = TRUE), recursive = TRUE)
rm(list=ls())
gc()

message("done!")
