# Stats NZ Time Series Data Service - Play Data Service

This repository contains a basic data service for time series data sourced from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/).  You will need [`sbt`](https://www.scala-sbt.org/) to build the project.  To build it, simply run:

```
sbt dist
```

This will yield a file called something like:

```
target/universal/snzts-0.1.0.zip
```

This archive can be unzipped on any machine with a Java runtime, and the service started by running the `bin/snzts` script (or `snzts.bat` on Windows).  Alternatively, the service can easily be run in development mode via:

```
sbt run
```

Of course, the service also needs a companion database to be running.  A Docker container is provided in `docker/backend` for this purpose.