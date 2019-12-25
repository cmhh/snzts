# Data Service for Public Stats NZ Time Series Data

This repository contains:

* `snzts` - source code for the data service
* `snzscrape` - a simple command-line utility for scraping the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/)
* `docker` - docker containers for easy running and deployment of the service
* `client` - prototype client libraries--just an R package for now

**Note: I work for Stats NZ, and it would not have been possible to reshape the data without arranging for the release of some internally-held metadata.  However, this is _not_ a Stats NZ product.**

## Overview

The service consists of the data service itself, but also a basic index page which can be used to search time series families, as well as API documentation.

![index](img/index.png)

![docs](img/docs.png)

![chart](img/chart.png)

![dataset](img/dataset.png)

## Running the Service

### `snzts`

The service itself is a Play Framework application.  The source is provided as an sbt project in the [`snzts`](snzts) folder.  It can be run in development mode via:

```bash
sbt run
```

The service will be available at `localhost` on port 9000 by default.  For example:

```
http://localhost:9000/snzts
http://localhost:9000/snzts/v1/dataset?format=json&seriesKeyword=Avocado&tail=12
http://localhost:9000/snzts/v1/series?format=json&seriesCode=HLFQ.SAA3AZ&seriesCode=HLFQ.S1A3S&seriesCode=HLFQ.S4A3S&tail=12
http://localhost:9000/snzts/v1/series?format=chart&seriesCode=HLFQ.SAA3AZ&seriesCode=HLFQ.S1A3S&seriesCode=HLFQ.S4A3S&tail=12
```

Alternatively, the service can be built and bundled via:

```bash
sbt dist
```

This will yield a file called `target/universal/snzts-0.1.0.zip` that can then be unzipped and run via:

```bash
./snzts-0.1.0/bin/snzts
```

The service also needs a companion database to be running with the appropriate schema.  The database connection is configured in [`conf/application.conf`](conf/application.conf), and the defaults are:

```as.is
slick.dbs.default.profile = "slick.jdbc.PostgresProfile$"
slick.dbs.default.driver = "slick.driver.PostgresqlDriver$"
slick.dbs.default.db.driver = "org.postgresql.Driver"
slick.dbs.default.db.url = "jdbc:postgresql://localhost/snzts"
slick.dbs.default.db.user = "webuser"
slick.dbs.default.db.password = "webuser"
slick.dbs.default.db.numThreads = 8
slick.dbs.default.db.maxConnections = 8
```

For example, a public database is available (with no guarantee of uptime given), and can be used by modifying the line:

```as.is
slick.dbs.default.db.url = "jdbc:postgresql://localhost/snzts"
```

to read:

```as.is
slick.dbs.default.db.url = "jdbc:postgresql://cmhh.hopto.org:5432/snzts"
```

To easily run a local copy of the database, a Docker container is provided in `docker/backend`.  It can be built and run as follows:

```bash
docker build -t snzts-backend docker/backend
docker run -d --rm --name backend -p 5432:5432 snzts-backend
```

### Docker Containers

By far the easiest way to get an instance of the service up and running from scratch is to use the provided Dockerfiles.  Simply clone this repository:

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

The data in the backend will date quickly.  To update it, a fresh set can be pulled from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/) via the `scraper` container:

```
docker run -d --rm -v `pwd`:/work snzts-scraper
```

This will produce a file called `csv.zip`, which can be copied to `docker/backend`, and the `snzts-backend` container rebuilt.