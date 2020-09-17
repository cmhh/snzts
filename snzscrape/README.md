# Web Scraper for Stats NZ Time Series Data

This repository contains a small [sbt](https://www.scala-sbt.org/) project.  Once compiled, it provides a simple command-line utility, written in Scala, which uses [Selenium](https://www.seleniumhq.org/) to automate the download of files from the [Stats NZ website](https://www.stats.govt.nz/large-datasets/csv-files-for-download/).  Firefox is used throughout.  You will need to download and install Firefox and [geckodriver](https://github.com/mozilla/geckodriver) yourself for this to work.

The tool can be run from the sbt shell via:

```{bash}
run <output-folder>
```

To make a fat jar with all dependencies included, run:

```{bash}
sbt assembly
```

To run this from a terminal execute:

```{bash}
java -jar <path-to-jar>/scrapeinfoshare.jar <output-folder>
```

This will download a number of csv and zip files to `<output-folder>`.