library(data.table)

# miscellaneous functions ------------------------------------------------------
sapply_ <- function(x, f) sapply(x, f, USE.NAMES = FALSE)

# file-related functions -------------------------------------------------------
filename <- function(x) {
  strsplit(x, '/', fixed = TRUE)[[1]] |> tail(1)
}

extension <- function(x) {
  strsplit(x, '.', fixed = TRUE)[[1]] |> tail(1) |> tolower()
}

write_csv <- function(x, file, ...) {
  fwrite(x, file = file, row.names = FALSE, na = "", ...)
}

create_if_not <- function(dname) {
  if (!dir.exists(dname)) dir.create(dname, recursive = TRUE)
  dir.exists(dname)
}

is_dir <- function(f) {
  file.info(f)$isdir
}

`%p%` <- function(x, y) sprintf("%s/%s", x, y)

`%+%` <- function(x, y) sprintf("%s%s", x, y)

extract_zip_files <- function(wd) {
  files <- dir(wd, full.names = TRUE)
  is_zip <- sapply_(files, \(x) extension(x) == "zip")
  
  for(file in files[is_zip]) {
    unzip(file, exdir = wd)
    unlink(file)
  }
}

flatten <- function(wd) {
  files <- dir(wd, full.names = TRUE)
  isdir <- sapply_(files, \(x) is_dir(x))
  
  for (d in files[isdir]) {
    fs <- dir(d, full.names = FALSE)
    for (f in fs) {
      file.rename(
        sprintf("%s/%s", d, f),
        sprintf("%s/%s", wd, f)
      )
    }
    
    unlink(d, recursive = TRUE)
  }
}

# tsm-related functions --------------------------------------------------------
is_tsm <- function(f) {
  tryCatch({
    x <- fread(f, nrows = 10)
    cols <- tolower(colnames(x))
    all(c("series_reference", "period", "data_value") %in% cols)
  }, error = function(e) {
    FALSE
  })
}

read_tsm <- function(f) {
  data <- data.table::fread(
    f, verbose = FALSE, showProgress = FALSE, integer64 = "numeric"
  ) 
  
  setnames(data, colnames(data), gsub("\\s+", "_", tolower(colnames(data))))
  
  if ("status_code" %in% colnames(data)) setnames(data, "status_code", "status")
  if (!"status" %in% colnames(data)) data[,status := NA]
  if ("magntude" %in% colnames(data)) setnames(data, "magntude", "magnitude")
  if (!"magnitude" %in% colnames(data)) data[,magnitude := NA]
  if(!"units" %in% colnames(data)) data[,units := NA]
  
  data[,data_value := as.numeric(data_value)]
  data[,status := toupper(substr(status, 1, 1))]
  data[,series_reference := toupper(series_reference)]
  data[,units := toupper(units)]
  
  data[, c(
    "series_reference", "period", "data_value", "status", "units", 
    "magnitude"
  )][]
}

recode_unit <- function(x) {
  x <- toupper(x)
  if (x %in% c("DOLALRS", "DOLLAR", "DOLLLARS")) {
    "DOLLARS"
  } else if (x %in% c("M3", "CU METRE", "CUBIC M")) {
    "M^3"
  } else if (x %in% c("/1000", "/ 1000")) {
    "PER MILLE"
  } else if (x %in% c("/ WOMAN")) {
    "PER WOMAN"
  } else if (x %in% c("HECTARES")) {
    "HA"
  } else x
}

import_tsm <- function(wd) {
  fs <- dir(wd, pattern = "*.csv", full.names = TRUE)
  is_tsm_ <- sapply_(fs, is_tsm)
  f_tsm <- fs[is_tsm_]
  f_nontsm <- fs[!is_tsm_]
  
  warning(sprintf(
    "Skipping the following: \n\t%s", 
    paste(f_nontsm, collapse = "\n\t")
  ))
  
  res <- lapply(f_tsm, \(x) {
    message(sprintf("\t%s...", x))
    
    tryCatch(
      read_tsm(x),
      error = function(e) message(sprintf("\t\toooops - '%s' failed.", x))
    )
  }) |> rbindlist()
  
  setkeyv(res, c("series_reference", "period"))
  
  res <- res[, n := 1:.N, by = c("series_reference", "period")][n == 1]
  res$n <- NULL
  
  setkeyv(res, c("series_reference", "period"))
  
  res[, `:=`("m" = 1:.N, "n" = .N:1), by = "series_reference"]
  
  setnames(res, "series_reference", "series_code")
  setnames(res, "data_value", "value")
  
  res[, c("series_code", "period", "value", "status", "m", "n")]
}