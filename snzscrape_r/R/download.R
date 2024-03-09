library(rvest)
library(jsonlite)

# import utils -----------------------------------------------------------------
source("R/utils.R")

# setup downloads folder -------------------------------------------------------
wd <- config::get("download_directory")

create_if_not(wd)
unlink(dir(wd, full.names = TRUE), recursive = TRUE)

# globals ----------------------------------------------------------------------
url <- config::get("url")

# utils ------------------------------------------------------------------------
get_links <- function(block) {
  if (!"BlockDocuments" %in% names(block)) {
    c()
  } else {
    sapply(block$BlockDocuments, \(x) {
      x$DocumentLink
    }, USE.NAMES = FALSE)
  }
}

# get links --------------------------------------------------------------------
links <- rvest::read_html(url) |> 
  rvest::html_element(css = "#pageViewData") |>
  rvest::html_attr("data-value") |>
  jsonlite::fromJSON(simplifyDataFrame = FALSE) |>
  (\(x){
    x$PageBlocks
  })() |>
  (\(x){
    lapply(x, get_links)
  })()|> 
  unlist() |> 
  sort() |> 
  unique()

# download links ---------------------------------------------------------------
for (link in links) {
  download.file(
    sprintf("https://www.stats.govt.nz/%s", link), 
    destfile = sprintf("downloads/%s", filename(link))
  )
}

