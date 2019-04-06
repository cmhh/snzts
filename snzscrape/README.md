# Stats NZ Time Series Data Service - Web Scraper

This repository contains a small [sbt](https://www.scala-sbt.org/) project.  Once compiled, it provides a simple command-line utility, written in Scala, which uses [Selenium](https://www.seleniumhq.org/) to automate the download of files from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/).  [PhantomJS](http://phantomjs.org/) is used, though it is straightforward to modify the code to use [Chrome](http://chromedriver.chromium.org/) or [Firefox](https://github.com/mozilla/geckodriver).  You will need to download and install PhantomJS yourself for this to work.

To build this tool, just run:

```
sbt package
```

This will create a jar file which will work as long as `scala` is in your search path.  To build a universal jar which will run as long as you have a java runtime, run:

```
sbt assembly
```

To run using the latter, simply run:

```
java -jar <path-to-jar>/scrapeinfoshare.jar <output-folder>
```

This will download a number of csv and zip filed to `<output-folder>`.

<!--
However, the easiest approach is simply to use Docker, as described in [docker](../docker).  Simply build the container:

```
docker build -t snzts-scraper docker/scraper
```

and run something like:

```
docker run --rm -v `pwd`:/work snzts-scraper
```
-->