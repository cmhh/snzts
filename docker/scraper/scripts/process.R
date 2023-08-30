options(warn = -1)

# load libraries ---------------------------------------------------------------
message("loading packages...")

library(data.table)
library(bit64)

# globals ----------------------------------------------------------------------
args <- commandArgs(trailingOnly=TRUE)

paths <- list(
  downloads = args[1],
  data = args[2],
  work = tempdir(),
  out = args[3]
)

# make sure working folders exist ----------------------------------------------
message("getting workspace ready...")

create_if_not <- function(dname) {
  if (dir.exists(dname)) TRUE
  else dir.create(dname, recursive = TRUE)
}

create_if_not(paths$out)
create_if_not(paths$work)

# utilities --------------------------------------------------------------------
`%p%` <- function(x, y) {
  sprintf("%s/%s", x, y)
}

standardise_column_names <- function(x) {
  oldcols <- colnames(x)
  newcols <- gsub("\\s+", "_", tolower(colnames(x)))
  newcols[grepl("status_code", newcols)] <- "status"
  setnames(x, newcols)
}

is_zip <- function(x) {
  tolower(tail(strsplit(x, '.', fixed = TRUE)[[1]], 1)) == "zip"
}

is_tsm <- function(fname) {
  tryCatch({
    x <- fread(fname, nrows = 10)
    cols <- tolower(colnames(x))
    all(c("series_reference", "period", "data_value") %in% cols)
  }, error = function(e) {
    FALSE
  })
}

read_tsm <- function(fname) {
  res <- fread(
    fname, verbose = FALSE, showProgress = FALSE, integer64 = "numeric"
  ) |>
    standardise_column_names()
  
  col <- colnames(res)
  
  col <- 
    col[tolower(col) %in% c(
      "series_reference", "period", "data_value", "status"
    )] |> (\(x) x[!duplicated(x)])()
  
  res[,
    `:=` (
      series_reference = toupper(series_reference),
      data_value = as.numeric(data_value), 
      status = toupper(substr(status, 1, 1))
    )
  ]
  
  res[, ..col]
}

# extract zip files ------------------------------------------------------------
message("extracting zip files...")

extract_zip_files <- function(path) {
  ext <- function(x) {
    tolower(tail(strsplit(x, '.', fixed = TRUE)[[1]], 1))
  }
  
  f <- dir(path, full.names = TRUE)
  zips <- f[sapply(f, function(x) ext(x) == "zip", USE.NAMES = FALSE)]
  
  for (z in zips) {
    unzip(z, exdir = path)
    unlink(z, recursive = TRUE)
  }
}

extract_zip_files(paths$downloads)

# flatten and remove directories -----------------------------------------------
message("flattening downloads...")

flatten <- function(path) {
  f <- dir(path, full.names = TRUE)
  
  is_dir <- sapply(f, \(x) file.info(x)$isdir, USE.NAMES = FALSE) 
  
  for (d in f[is_dir]) {
    for (x in dir(d, full.names = TRUE)) {
      name <- strsplit(x, "/")[[1]] |> tail(1)
      file.rename(x, sprintf("%s/%s", path, name))
    }
    unlink(d, recursive = TRUE)
  }
}

flatten(paths$downloads)

# import csv files -------------------------------------------------------------
message("importing csv data...")

import_tsm <- function(path) {
  f <- dir(path, pattern = "*.csv", full.names = TRUE)
  f_tsm <- f[sapply(f, is_tsm)]
  f_nontsm <- f[!sapply(f, is_tsm)]
  
  warning(
    sprintf(
      "Skipping the following: \n\t%s", 
      paste(f_nontsm, collapse = "\n\t")
    )
  )

  res <- 
    lapply(
      f_tsm, 
      function(x) {
        message(sprintf("\t%s...", x))
        tryCatch(
          read_tsm(x), 
          error = function(e) cat(sprintf("\t\toooops - '%s' failed.\n", x)
        ))
      }
    ) |> 
    rbindlist(use.names = TRUE)
  
  res <- res[rowidv(res, cols = c("series_reference", "period")) == 1]
  setkeyv(res, c("series_reference", "period"))
  
  res[, 
    `:=` (m = seq_len(.N), n = .N - seq_len(.N) + 1), by = series_reference
  ]
  
  setnames(res, c("series_reference", "data_value"), c("series_code", "value"))
  
  res
}

data <- import_tsm(paths$downloads)

# import metadata --------------------------------------------------------------
message("importing metadata...")

subject <- readRDS(paths$data %p% "subject.rds") |> setDT()
setkey(subject, subject_code)

family  <- readRDS(paths$data %p% "family.rds") |> setDT()
setkey(family, subject_code, family_code, family_nbr)

series  <- readRDS(paths$data %p% "series.rds") |> setDT()
setkey(series, subject_code, family_code, family_nbr, series_code)

# subset data and metadata -----------------------------------------------------
message("removing data rows without corresponding metadata...")

refs <- 
  data[
    !duplicated(series_code), "series_code"
  ][
    series[, series_code], 
    on = "series_code", nomatch = 0
  ]

data <- data[refs, on = "series_code", nomatch = 0]
setkey(data, series_code, period)

message("removing metadata rows without corresponding data...")

units <- function(x) {
  x <- toupper(x)
  if (x %in% c("DOLALRS", "DOLLAR", "DOLLARS")) "DOLLARS"
  else if (x %in% c("M3", "CU METRE", "CUBIC M")) "M^3"
  else if (x %in% c("/1000", "/ 1000")) "PER MILLE"
  else if (x %in% c("/ WOMAN")) "PER WOMAN"
  else if (x %in% c("HECTARES")) "HA"
  else x
}

series <- series[refs, on = "series_code"][, 
  unit_text := sapply(unit_text, units, USE.NAMES = FALSE)
]
     
family_codes <- series[, .(family_code, family_nbr)] |> unique()
setkey(family_codes, family_code, family_nbr)

subject_codes <- series[, .(subject_code)] |> unique()
setkey(subject_codes, subject_code)

family <- 
  family[family_codes, on = c("family_code", "family_nbr"), nomatch = 0]
setkey(family, family_code, family_nbr)

subject <- subject[subject_codes, on = "subject_code", nomatch = 0]
setkey(subject, subject_code)

# write to disk ----------------------------------------------------------------
message("saving and archiving csv files...")
write_csv <- function(x, file, ...) 
  fwrite(x, file = file, row.names = FALSE, na = "", ...)

write_csv(subject, file = paths$work %p% "subject.csv.gz")
write_csv(family, file = paths$work %p% "family.csv.gz")
write_csv(series, file = paths$work %p% "series.csv.gz")
write_csv(data, file = paths$work %p% "data.csv.gz", quote = FALSE)

zip(
  paths$out %p% "csv.zip",
  files = dir(paths$work, "*.csv.gz", full.names = TRUE),
  flags = '-rj'
)

message("tidying up...")
unlink(paths$downloads, recursive = TRUE)
unlink(paths$work, recursive = TRUE)

# tidy up ----------------------------------------------------------------------
rm(list=ls())
gc()

message("all done!")
