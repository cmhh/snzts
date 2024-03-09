source("R/utils.R")

# globals ----------------------------------------------------------------------
wd <- config::get("download_directory")

# extract zip files ------------------------------------------------------------
extract_zip_files(wd)

# flatten and remove directories -----------------------------------------------
flatten(wd)
