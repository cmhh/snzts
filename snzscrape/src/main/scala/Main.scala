package org.cmhh

import org.openqa.selenium.{WebDriver, WebElement}

// import org.openqa.selenium.firefox.FirefoxDriver
// import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver

import java.util.concurrent.TimeUnit
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.{Path, Paths, Files}
import java.nio.channels.{Channels, FileChannel, ReadableByteChannel}

import scala.collection.mutable.{Set => MSet}

object Utils {
  def getURL(url: String, path: String) {
    val inc: ReadableByteChannel = Channels.newChannel(new URL(url).openStream())
    val outs: FileOutputStream = new FileOutputStream(path)
    val outc: FileChannel = outs.getChannel()
    outc.transferFrom(inc, 0, Long.MaxValue)
    outc.close()
    outs.close()
  }
}

object Main extends App {
  if (args.length == 0) {
    println("No output folder provided.")
    System.exit(1)
  }

  val outPath = Paths.get(args(0))
  if (!Files.exists(outPath)) {
    try {
      Files.createDirectories(outPath)
    } catch {
      case _: Throwable => {
        println(s"Couldn't create folder ${args(0)}.")
        System.exit(1)
      }
    }
  }

  val s: MSet[String] = MSet[String]()

  // get URLs
  // val driver: FirefoxDriver = new FirefoxDriver()
  // val driver: ChromeDriver = new ChromeDriver()
  val driver: PhantomJSDriver = new PhantomJSDriver()
  driver.get("https://www.stats.govt.nz/large-datasets/csv-files-for-download/")
  driver.manage().timeouts ().implicitlyWait (10, TimeUnit.SECONDS)
  driver.manage().window().maximize()
  val links = driver.findElementsByCssSelector("a[download]")
  links.stream().forEach((e: WebElement) => s.add(e.getAttribute("href")))
  driver.close()

  // download files.
  println("")
  s.foreach((url: String) => {
    val fname: String = url.split("/").takeRight(1)(0)
    print(s"\nDownloading ${fname}... ")
    Utils.getURL(url, s"${args(0)}/$fname")
    print("ok.")
  })
  println("")
}
