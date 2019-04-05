# Statistics New Zealand Time Series Data Service - R Package

This repository contains an R package which provides a wrapper for the `snzts` web service.  It can be installed directly from GitHub using `devtools`:

```{r}
devtools::install_github("cmhh/snzts/client/R")
```

Alterntively, if the repository has been cloned, the package can again be installed from R with `devtools` as:

```{r}
devtools::document()
devtools::install()
```

Obviously, for the package to be useful, there needs to be an instance of the webservice running.  The package assumes this is available via `https://cmhh.hopto.org/snzts/v1` by default.  This is very much a test server, and no guarantee is made regarding availability.  If running a local instance of the service at `http://localhost:9000/snzts/v1`, the default location can be changed:

```{r}
library(snzts)
set_server("http://localhost:9000/snzts/v1")
```
