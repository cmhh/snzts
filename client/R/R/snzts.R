.onLoad <- function(libname, pkgname){
  # options("snzts.url" = "http://localhost:9000/snzts/v1")
  options("snzts.url" = "https://cmhh.hopto.org/snzts/v1")
}

#' @keywords internal
d <- function(...) data.frame(..., stringsAsFactors = FALSE)

#' @keywords internal
clean <- function(x) lapply(x, function(x) gsub(' ', '%20', x))

#' @keywords internal
appendq <- function(q, parameter_list, parameter_name) {
  if (missing(parameter_list)) return(q)
  for (parameter in clean(parameter_list))
    q <- sprintf("%s&%s=%s", q, parameter_name, parameter)
  q
}

#' Test URL
#'
#' Test whether or not a particular URL is a valid web service or not.
#'
#' @param server Server URL.
#'
#' @export
#'
#' @examples
#' test_server("http://localhost:9000/snzts/v1")
test_server <- function(server = getOption("snzts.url")) {
  tryCatch(
    httr::content(httr::GET(sprintf("%s/hello", server))) == "Hello, World!",
    error = function(e) FALSE
  )
}

#' Set server URL
#'
#' Set option \code{snzts.url} which provides the default server address.
#'
#' @param server Server URL.
#'
#' @export
set_server <- function(server) {
  if (test_server(server)) {
    options("snzts.url" = server)
    TRUE
  } else {
    warning("Server '%s' is either not valid, or is not responding.")
    FALSE
  }
}

#' List subjects
#'
#' List available time series subjects.
#'
#' @param subject_code Subject code, e.g. \code{"HLF"}.
#' @param subject_keywords Search terms for subject.
#' @param server Server URL.
#'
#' @export
#'
#' @examples
#' get_subjects()
#' get_subjects(subject_keywords = list("household", "labour force"))
get_subjects <- function(subject_code, subject_keywords, server = getOption("snzts.url")){
  query <- sprintf("%s/subjects?format=json", server)

  query <- appendq(query, subject_code, "subjectCode")
  query <- appendq(query, subject_keywords, "subjectKeyword")

  jsonlite::fromJSON(query)
}

#' List families
#'
#' List available time series families, where families are subgroups below subject.
#'
#' @param subject_code Subject code, e.g. \code{"HLF"}.
#' @param family_code Family code, e.g. \code{"SA"}.
#' @param family_nbr Family number.
#' @param subject_keywords Search terms for subject.
#' @param family_keywords Search terms for family.
#' @param server Server URL.
#'
#' @export
#'
#' @examples
#' get_families(subject_code = "HLF", family_code = "SA", family_nbr = 7)
#' get_families(subject_code = "BLD", family_keywords = "region")
get_families <- function(subject_code, family_code, family_nbr,
                         subject_keywords, family_keywords, server = getOption("snzts.url")){
  query <- sprintf("%s/families?format=json", server)

  query <- appendq(query, subject_code, "subjectCode")
  query <- appendq(query, family_code, "familyCode")
  query <- appendq(query, family_nbr, "familyNbr")
  query <- appendq(query, subject_keywords, "subjectKeyword")
  query <- appendq(query, family_keywords, "familyKeyword")

  jsonlite::fromJSON(query)
}

#' List series
#'
#' List available series, including basic metadata.
#'
#' @param subject_codes List of subject codes, e.g. \code{list("HLF", "LCI", "BOP", "CPI")}.
#' @param family_codes List of family codes, e.g. \code{list("SA")}.
#' @param family_nbrs List of family numbers.
#' @param series_codes List of series code, e.g. \code{list("HLFQ.SAA1AZ")}
#' @param subject_keywords Search terms for subject.
#' @param family_keywords Search terms for family.
#' @param series_keywords Search terms for individual series.
#' @param interval Interval. 1 denotes monthly, 3 denotes quarterly, and 12 denotes annual.
#' @param offset Offset month. For example, 3 denotes the March quarter for quartely data.
#' @param limit Series limit. Limits the number of results returned.
#' @param drop Used in conjunction with limit to access results in batches. For example, setting limit to 100 and drop to 100 would yield results 101 through 200.
#' @param server Server URL.
#'
#' @export
#'
#' @examples
#' get_info(subject_code = "HLF", family_code = "SA",
#'          series_keywords = list("female", "not in labour force", "all ages"))
get_info <- function(subject_codes, family_codes, family_nbrs, series_codes,
                     subject_keywords, family_keywords, series_keywords,
                     interval, offset, limit = 100, drop,
                     server = getOption("snzts.url")) {
  query <- sprintf("%s/info?format=json",
                   server)

  query <- appendq(query, subject_codes, "subjectCode")
  query <- appendq(query, family_codes, "familyCode")
  query <- appendq(query, family_nbrs, "familyNbr")
  query <- appendq(query, series_codes, "seriesCode")
  query <- appendq(query, subject_keywords, "subjectKeyword")
  query <- appendq(query, family_keywords, "familyKeyword")
  query <- appendq(query, series_keywords, "seriesKeyword")
  query <- appendq(query, interval, "interval")
  query <- appendq(query, offset, "offset")
  query <- appendq(query, limit, "limit")
  query <- appendq(query, drop, "drop")

  jsonlite::fromJSON(query, simplifyVector = TRUE)
}


#' Retrieve time series by series codes
#'
#' Retrieve time series data for a list of series codes.
#'
#' @param series_codes List of series identifiers, e.g. \code{list("HLFQ.SAA3AZ", "HLFQ.S1A3S", "HLFQ.S4A3S")}.
#' @param ... Series identifiers
#' @param start Start date, e.g. \code{"1986.03"}
#' @param end End date, e.g. \code{"2019.06"}
#' @param head Number of observations to keep at head of series.
#' @param tail Number of observations to keep at tail of series.
#' @param server Server URL.
#'
#' @export
#'
#' @return
#' A data frame containing time series data in long format.  Associated metadata
#' (subject title, series title, etc.) is stored in an attribute named \code{metadata}.
#'
#' @examples
#' x <- get_series(
#'   series_codes = c("HLFQ.SAA3AZ", "HLFQ.S1A3S", "HLFQ.S4A3S"),
#'   tail = 12
#' )
#' attr(x, "metadata")
#'
#' y <- get_series("HLFQ.SAA3AZ", "HLFQ.S1A3S", "HLFQ.S4A3S", tail = 12)
get_series <- function(series_codes,
                       ...,
                       start, end, head, tail,
                       server = getOption("snzts.url")) {
  args <- list(...)
  series_codes <-
    if(missing(series_codes)) args
    else append(series_codes, args)

  if (is.null(series_codes)) return(NULL)
  parse_meta <- function(x) {
    d(series_code = x$series_code,
      subject_title = x$subject_title,
      family_code = x$family_code,
      family_nbr = x$family_nbr,
      family_title = x$family_title,
      interval = x$interval,
      magnitude = x$magnitude,
      offset = x$offset,
      units = x$units)
  }

  parse_data <- function(x) {
    vars <- x$variables
    vals <- sprintf('"%s"', x$outcomes)
    e    <- setNames(lapply(vals, rlang::parse_quo, rlang::caller_env()), vars)
    d1 <- d(series_code = x$series_code)
    d2 <- dplyr::mutate(d1, !!!e)
    d3 <- dplyr::inner_join(
      d2,
      d(
        series_code = x$series_code,
        period = as.numeric(unlist(x$period)),
        value = as.numeric(unlist(x$value)),
        status = unlist(x$status)
      ),
      by = "series_code"
    )
  }

  query <- sprintf("%s/series?format=json", server)
  query <- appendq(query, series_codes, "seriesCode")
  query <- appendq(query, start, "start")
  query <- appendq(query, end, "end")
  query <- appendq(query, head, "head")
  query <- appendq(query, tail, "tail")

  response <- jsonlite::fromJSON(query, simplifyVector = FALSE)

  res  <- dplyr::bind_rows(lapply(response, parse_data))
  meta <- dplyr::bind_rows(lapply(response, parse_meta))
  attr(res, "metadata") <- meta
  res
}

#' Retrieve time series data
#'
#' Retrieve time series data matching query parameters.
#'
#' @param subject_codes List of subject codes, e.g. \code{list("HLF", "LCI", "BOP", "CPI")}.
#' @param family_codes List of family codes, e.g. \code{list("SA")}.
#' @param family_nbrs List of family numbers.
#' @param series_codes List of series code, e.g. \code{list("HLFQ.SAA1AZ")}
#' @param subject_keywords Search terms for subject.
#' @param family_keywords Search terms for family.
#' @param series_keywords Search terms for individual series.
#' @param interval Interval. 1 denotes monthly, 3 denotes quarterly, and 12 denotes annual.
#' @param offset Offset month. For example, 3 denotes the March quarter for quartely data.
#' @param limit Series limit. Limits the number of results returned.
#' @param drop Used in conjunction with limit to access results in batches. For example, setting limit to 100 and drop to 100 would yield results 101 through 200.
#' @param head How many values at the head of each series to keep.
#' @param tail How many values at the tail of each series to keep.
#' @param server Server URL.
#'
#' @export
#'
#' @return
#' A data frame containing time series data in long format.  Associated metadata
#' (subject title, series title, etc.) is stored in an attribute named \code{metadata}.
#'
#' @examples
#' # Labour force status by sex and age--female, not in labour force, total all ages, quarterly, last 12 quarters.
#' x <- get_dataset(subject_code = "HLF", family_code = "SA",
#'                  series_keywords = list("female", "not in labour force", "all ages"),
#'                  interval = 3, tail = 12)
#' attr(x, "metadata")
get_dataset <- function(subject_codes, family_codes, family_nbrs, series_codes,
                        subject_keywords, family_keywords, series_keywords,
                        interval, offset, limit = 100, drop, head, tail,
                        server = getOption("snzts.url")) {
  parse_meta <- function(x) {
    d(series_code = x$series_code,
      subject_title = x$subject_title,
      family_code = x$family_code,
      family_nbr = x$family_nbr,
      family_title = x$family_title,
      interval = x$interval,
      magnitude = x$magnitude,
      offset = x$offset,
      units = x$units)
  }

  parse_data <- function(x) {
    vars <- x$variables
    vals <- sprintf('"%s"', x$outcomes)
    e    <- setNames(lapply(vals, rlang::parse_quo, rlang::caller_env()), vars)
    d1 <- d(series_code = x$series_code)
    d2 <- dplyr::mutate(d1, !!!e)
    d3 <- dplyr::inner_join(
      d2,
      d(
        series_code = x$series_code,
        period = as.numeric(unlist(x$period)),
        value = as.numeric(unlist(x$value)),
        status = unlist(x$status)
      ),
      by = "series_code"
    )
  }

  query <- sprintf("%s/dataset?format=json",
                   server)

  query <- appendq(query, subject_codes, "subjectCode")
  query <- appendq(query, family_codes, "familyCode")
  query <- appendq(query, family_nbrs, "familyNbr")
  query <- appendq(query, series_codes, "seriesCode")
  query <- appendq(query, subject_keywords, "subjectKeyword")
  query <- appendq(query, family_keywords, "familyKeyword")
  query <- appendq(query, series_keywords, "seriesKeyword")
  query <- appendq(query, interval, "interval")
  query <- appendq(query, offset, "offset")
  query <- appendq(query, limit, "limit")
  query <- appendq(query, drop, "drop")
  query <- appendq(query, head, "head")
  query <- appendq(query, tail, "tail")

  response <- jsonlite::fromJSON(query, simplifyVector = FALSE)

  res  <- dplyr::bind_rows(lapply(response, parse_data))
  meta <- dplyr::bind_rows(lapply(response, parse_meta))
  attr(res, "metadata") <- meta

  cols <- c(colnames(res)[!colnames(res) %in% c("period", "value", "status")], "period", "value", "status")
  dplyr::select(res, !!!rlang::syms(cols))
}
