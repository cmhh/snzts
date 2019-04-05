# Statistics New Zealand Time Series Data Service

This repository contains tools to:

* scrape time series data from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/)
* reshape the downloaded files
* publish the downloaded files via a webservice.

**Note: I work for Stats NZ, and it would not have been possible to reshape the data without arranging for the release of some internally-held metadata.**

## Overview

The service consists of the data service itself, but also a basic index page which can be used to search time series families, as well as API documentation.

![index](img/index)

![docs](img/docs)

![docs](img/chart)

![docs](img/dataset)


## Docker Containers

By far the easiest way to get an instance of the service up and running is to use the provided Dockerfiles.  Simply clone this repository:

```
git clone cmhh/snzts
```

Then, build the necessary docker containers:

```
cd docker
docker build -t postgis postgis
docker build -t snzts-backend backend
docker build -t snzts-api api
docker build -t snzts-scraper scraper
```

To start the service, from the `docker` folder, simpy run:

```
docker-compose up -d
```

The back-end database is created from CSV files on start-up, so takes about a minute to fully start.  Once running, the service will be available at `localhost:9000/snzts/`.

The data in the backend will date quickly.  To update it, a fresh set can be pulled from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/) via the `scraper` container.  To build and run the container:

```
docker run -d --rm -v `pwd`:/work snzts-scraper
```

This will produce a file called `csv.zip`, which can be copied to `docker/backend`, and the `snzts-backend` container rebuilt.