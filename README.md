# Data Service for Public Stats NZ Time Series Data

This repository contains:

* `snzts` - source code for a time series data service
* `snzscrape` - a simple command-line utility for scraping the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/)
* `docker` - Dockerfiles for easy running and deployment of the service
* `client` - prototype client libraries--just an R package for now

**Note: I work for Stats NZ, and it would not have been possible to reshape the data without arranging for the release of some internally-held metadata.  However, this is _not_ a Stats NZ product.**

# Quick Start

By far and away the easiest way to get everything up and running is to use the provided Docker Compose setup.  First, the `snzscrape` and `snzts` projects need to be built.  These are sbt projects, so sbt needs to be available.  If you don't have sbt already, it will suffice just to fetch a copy and unpack it in the source repository root:

```bash
curl -sL https://github.com/sbt/sbt/releases/download/v1.5.5/sbt-1.5.5.tgz | tar xzvf - 
```

Build `snzcrape` by issuing:

```bash
cd snzscrape && ../sbt/bin/sbt assembly && cd ..
```

Build `snzts` by issuing: 

```bash
cd snzts && ../sbt/bin/sbt dist && cd ..
```

The containers are configured to run as the current user, and to ensure this works correctly, the following environment variables need to be set:

```bash
export UID=$(id -u)
export GID=$(id -g)
```

We need to scrape the Stats NZ website so data is available to load into our back-end.  To do this, run:

```bash
docker-compose -f snzscrape.yml up -d
```

Once all prerequisites are satisfied, sa local copy of the service can be started by running:

```bash
docker-compose -f snzts.yml up -d
```

The service will then be accessible at `localhost:9000/snzts`.  Note the application can make use of a secret.  If you want to provide this, make sure the `APPLICATION_SECRET` environment variable is present:

```bash
export APPLICATION_SECRET="$(head -c 32 /dev/urandom | base64)"
```

![index](img/index.png)

![docs](img/docs.png)

![chart](img/chart.png)

![dataset](img/dataset.png)