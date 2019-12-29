options(warn = -1)

# load libraries ---------------------------------------------------------------
message("loading packages...")
library(fs)
library(dplyr)
library(data.table)
library(sqldf)

# globals ----------------------------------------------------------------------
message("getting workspace ready...")
dlpath <- "work/downloads"
datpath <- "/data"
outpath <- "work/output"
workpath <- "work"

create_if_not <- function(dname) {
  if (dir_exists(dname)) TRUE
  else dir_create(dname, recurse = TRUE)
}

create_if_not(outpath)
create_if_not(sprintf("%s/csv", outpath))
create_if_not(workpath)

# utilities --------------------------------------------------------------------
`%p%` <- function(x, y) {
  sprintf("%s/%s", x, y)
}

is_zip <- function(x) {
  tolower(tail(strsplit(x, '.', fixed = TRUE)[[1]], 1)) == "zip"
}

# extract zip files ------------------------------------------------------------
message("extracting zip files...")
extract_zip_files <- function() {
  ext <- function(x) {
    tolower(tail(strsplit(x, '.', fixed = TRUE)[[1]], 1))
  }
  
  f <- dir(dlpath, full.names = TRUE)
  is_zip <- sapply(f, function(x) ext(x) == "zip") %>% setNames(NULL)
  
  lapply(f[is_zip], 
       function(x) {
         unzip(x, exdir = dlpath)
         file_delete(x)
       })
}

extract_zip_files()

# flatten and remove directories -----------------------------------------------
message("flattening downloads...")
flatten <- function() {
  f <- dir(dlpath, full.names = TRUE)
  
  is_dir <- sapply(f, function(x) is_dir(x)) %>% 
    setNames(NULL)
  
  lapply(f[is_dir], function(x) {
    sapply(dir(x, full.names = TRUE), function(x) {
      file_move(x, dlpath)
    })
    dir_delete(x)
  })
}

flatten()

# import csv files -------------------------------------------------------------
message("importing csv data...")
import_tsm <- function() {
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
    res <- fread(fname, verbose = FALSE, showProgress = FALSE, integer64 = "numeric") %>%
      setNames(tolower(colnames(.))) %>%
      mutate(data_value = as.numeric(data_value))
    col <- colnames(res)
    col <- col[tolower(col) %in% 
                 c("series_reference", "period", "data_value", "status", "units", 
                   "magnitude", "magntude")]
    res <- res %>% 
      select(!!!rlang::syms(col)) %>% 
      setNames(tolower(colnames(.))) %>%
      mutate(
        data_value = as.numeric(data_value),
        status = toupper(substr(status, 1, 1))
      )
    if ("magntude" %in% tolower(col))
      res <- res %>%
        rename(magnitude = magntude)
    if (!"magnitude" %in% colnames(res))
      res <- res %>%
        mutate(magnitude = NA)
    if (!is.numeric(res$data_value)) 
      res <- res %>%
        mutate(data_value = 
                 ifelse(tolower(data_value) == "Not Available", NA, data_value) %>% 
                 as.numeric)
    res <- res %>% 
      select(series_reference, period, data_value, status, units, magnitude) %>%
      mutate(series_reference = toupper(series_reference),
             status = toupper(status),
             units = toupper(units))
  }
  
  f <- dir(dlpath, pattern = "*.csv", full.names = TRUE)
  f_tsm <- f[sapply(f, is_tsm)]
  f_nontsm <- f[!sapply(f, is_tsm)]
  warning(sprintf("Skipping the following: \n\t%s", paste(f_nontsm, collapse = "\n\t")))
  res <- lapply(f_tsm, 
         function(x) {
           message(sprintf("\t%s...", x))
           tryCatch(read_tsm(x), 
                    error = function(e) cat(sprintf("\t\toooops - '%s' failed.\n", x)))}) %>% 
    bind_rows %>% 
    arrange(series_reference, period)
  
  q <- "
  select 
    *, 
    row_number() over(partition by series_reference, period 
      order by series_reference, period) as n 
  from 
    res"
  
  res <- sqldf(q, drv = "SQLite") %>% 
    filter(n == 1) %>% 
    select(-n) %>%
    mutate(period = sprintf("%.2f", period)) %>%
    group_by(series_reference) %>%
    mutate(m = row_number(),
           n = n() - row_number() + 1) %>%
    ungroup(series_reference) %>%
    rename(series_code = series_reference, value = data_value) %>%
    select(series_code, period, value, status, m, n)
  
}

# import csv files -------------------------------------------------------------
data <- import_tsm()

# import metadata --------------------------------------------------------------
message("importing metadata...")
subject <- readRDS(datpath %p% "subject.rds")
family <- readRDS(datpath %p% "family.rds")
series <- readRDS(datpath %p% "series.rds") 

# subset data and metadata -----------------------------------------------------
message("removing data rows without corresponding metadata...")
refs <- data %>% select(series_code) %>% unique
refs <- refs %>% inner_join(series %>% select(series_code), by = "series_code")
data <- data %>% inner_join(refs, by = "series_code") %>% arrange(series_code, period)

message("removing metadata rows without corresponding data...")
series <- series %>% 
  inner_join(refs, by = "series_code") %>% 
  arrange(series_code) %>%
  mutate(unit_text = toupper(unit_text)) %>%
  mutate(
    unit_text = case_when(
      unit_text %in% c("DOLALRS", "DOLLAR", "DOLLLARS") ~ "DOLLARS", 
      unit_text %in% c("M3", "CU METRE", "CUBIC M") ~ "M^3",
      unit_text %in% c("/1000", "/ 1000") ~ "PER MILLE",
      unit_text %in% c("/ WOMAN") ~ "PER WOMAN",
      unit_text %in% c("HECTARES") ~ "HA",
      TRUE ~ unit_text
    )
  )
     
family_codes <- unique(series %>% select(family_code, family_nbr))
subject_codes <- unique(series %>% select(subject_code))
family <- family %>% inner_join(family_codes, by = c("family_code", "family_nbr")) %>% arrange(family_code, family_nbr)
subject <- subject %>% inner_join(subject_codes, by = c("subject_code")) %>% arrange(subject_code)

# write to disk ----------------------------------------------------------------
message("saving and archiving csv files...")
write_csv <- function(x, file, ...) 
  write.csv(x, file = file, row.names = FALSE, na = "", ...)

write_csv(subject, 
          file = outpath %p% "csv/subject.csv")
write_csv(family, 
          file = outpath %p% "csv/family.csv")
write_csv(series, 
          file = outpath %p% "csv/series.csv")
write_csv(data,
          file = outpath %p% "csv/data.csv", quote = FALSE)

zip(workpath %p% "csv.zip", dir(outpath %p% "csv", full.names = TRUE), flags = '-rj')

message("tidying up...")
dir_delete(dlpath)
dir_delete(outpath)

# tidy up ----------------------------------------------------------------------
rm(list=ls())
gc()

assign("last.warning", NULL, envir = baseenv())

message("all done!")
