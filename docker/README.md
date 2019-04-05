# Statistics New Zealand Time Series Data Service - Docker Containers

By far the easiest way to get a service up and running is to use the provided Dockerfiles.  

## Getting Started

To get up and running simply clone this repository:

```
git clone https://github.com/cmhh/snzts
```

Then, build the necessary docker containers:

```
cd docker
docker build -t postgis postgis
docker build -t snzts-backend backend
docker build -t snzts-api api
```

To start the service, from the `docker` folder, simpy run:

```
docker-compose up -d
```

If all goes well, the service will be running at `localhost:9000/snzts`.  The database is populated on startup from CSV files, and will take a minute or so to be fully up and running.

## Updating the Service

Data is included with the `backend` in the form of an archive containing several CSV files.  This data will date quickly.  To update it, you will need to get a fresh set from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/).  This can be done by building the `scraper` container:

```
docker build -t snzts-scraper scraper
```

and then running something like:

```
docker run -d --rm -v `pwd`:/work snzts-scraper
```

Once done, a file called `csv.zip` will appear in the present working directory.  Simply copy this to `docker/backend` and rebuild the `snzts-backend` container.  Note that this image will run as root, so it is likely you will need sudo access to delete or modify it.  This will likely be updated to run as an ordinary user at some point.
