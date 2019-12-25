# Data Service for Public Stats NZ Time Series Data

This repository contains a basic data service for time series data sourced from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/).  You will need [`sbt`](https://www.scala-sbt.org/) to build the project.  To build it, simply run:

```
sbt dist
```

This will yield a file called something like:

```
target/universal/snzts-0.1.0.zip
```

This archive can be unzipped on any machine with a Java runtime, and the service started by running the `bin/snzts` script (or `snzts.bat` on Windows).  Alternatively, the service can be run in development mode via:
```
sbt run
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

To easily run a local copy of the database, a Docker container is provided in `docker/backend` for this purpose.  It can be built and run as follows:

```bash
docker build -t snzts-backend docker/backend
docker run -d --rm --name backend -p 5432:5432 snzts-backend
```