package controllers

import dao.SNZTS
import models._

import javax.inject.Inject
import play.api.mvc.{ AbstractController, ControllerComponents, Request, AnyContent }
import akka.stream.scaladsl.Source
import slick.lifted.TableQuery
import scala.concurrent.{ Future, Await, ExecutionContext }
import scala.concurrent.duration.Duration
import play.api.Configuration

/**
 * Controllers for application routes.
 */
class SNZTSController @Inject() (snzts: SNZTS, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext, config: Configuration) 
  extends AbstractController(controllerComponents) {
    
  private lazy val seriesLimitCSV: Option[Int] = if (config.has("seriesLimitCSV")) Some(config.get[Int]("seriesLimitCSV")) else None
    
  private lazy val seriesLimitJSON: Option[Int] = if (config.has("seriesLimitJSON")) Some(config.get[Int]("seriesLimitJSON")) else None
  
  /**
   * Basic search UI.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  /**
   * Display HTML API documentation.
   */
  def docs() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.docs())
  }

  def getLimit = Action {
    Ok(s"""{"series_limit_csv":${seriesLimitCSV.getOrElse("null")},"series_limit_json":${seriesLimitJSON.getOrElse("null")}}""").as("application/json")
  }

  /**
   * Return time series subjects.
   *
   * @param format Output format, e.g. `Option(Format.CSV)` or `Option(Format.JSON)`.
   * @param subjectCode Subject code, e.g. `Some("HLF")`.
   * @param subjectKeyword List of keywords used to subset subjects.
   */
  def getSubjects(format: Option[Format], subjectCode: Option[String], subjectKeyword: List[String]) = Action.async {
    val q: Future[Seq[Subject]] = snzts.selectSubjects(subjectCode, subjectKeyword)
    format match {
      case Some(Format.CSV) => 
        val res = snzts.subjectsCSV(q)
        res.map(x => Ok(x).as("text/csv"))
      case _ => 
        val res = snzts.subjectsJSON(q)
        res.map(x => Ok(x).as("application/json"))
    }
  }

  /**
   * Return time series families.
   *
   * @param format Output format, e.g. `Option(Format.CSV)` or `Option(Format.JSON)`.
   * @param subjectCode Time series subject code, e.g. `Some("HLF")`.
   * @param familyCode Time series family code, e.g. `Some("SA")`.
   * @param familyNbr Time series family number.
   * @param subjectKeyword List of keywords used to subset subjects.
   * @param familyKeyword List of keywords used to subset subjects.
   */
  def getFamilies(
    format: Option[Format], subjectCode: Option[String],familyCode: Option[String], familyNbr: Option[Int],
    subjectKeyword: List[String], familyKeyword: List[String]
  ) = Action.async {
    val q: Future[Seq[Family]] = snzts.selectFamilies(subjectCode, familyCode, familyNbr, subjectKeyword, familyKeyword)
    format match {
      case Some(Format.CSV) => 
        val res = snzts.familiesCSV(q)
        res.map(x => Ok(x).as("text/csv"))
      case _ => 
        val res = snzts.familiesJSON(q)
        res.map(x => Ok(x).as("application/json"))
    }
  }

  /**
   * Return number of matching time series.
   *
   * @param format Output format, e.g. `Some(Format.CSV)` or `Some(Format.JSON)`.
   * @param subjectCode  List of subject codes, e.g. `List("HLF","BLD")`.
   * @param familyCode List of family codes, e.g. `List("SA")`.
   * @param familyNbr List of family numbers.
   * @param seriesCode List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   * @param subjectKeyword List of keywords used to subset subjects.
   * @param familyKeyword List of keywords used to subset families.
   * @param seriesKeyword List of keywords used to subset series.
   * @param interval Time series interval, e.g., `Some(1)` for monthly, `Some(3)` for quarterly, and `Some(12)` for annual.
   * @param offset Start period, e.g., `interval=Some(12)` and `offset=Some(12)` would denote a December year.
   */
  def getCount(
    format: Option[Format], subjectCode: List[String], familyCode: List[String], familyNbr: List[Int], 
    seriesCode: List[String], subjectKeyword: List[String], familyKeyword: List[String], 
    seriesKeyword: List[String], interval: Option[Int], offset: Option[Int]
  ) = Action.async {
    val q: Future[Int] = 
      snzts.selectSeriesCount(
        subjectCode, familyCode.distinct, familyNbr.distinct, seriesCode.distinct,
        subjectKeyword.distinct, familyKeyword.distinct, seriesKeyword.distinct,
        interval, offset
      )

    format match {
      case Some(Format.CSV) => {
        q.map(s => {
          Ok(s"count\n${s.toString}")
        }.as("text/csv"))
      }
      case _ => {
        q.map(s => {
          Ok(s.toString)
        }.as("application/json")) 
      }
    }    
  } 

  /**
   * Return information about time series.
   *
   * @param format Output format, e.g. `Some(Format.CSV)` or `Some(Format.JSON)`.
   * @param subjectCode  List of subject codes, e.g. `List("HLF","BLD")`.
   * @param familyCode List of family codes, e.g. `List("SA")`.
   * @param familyNbr List of family numbers.
   * @param seriesCode List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   * @param subjectKeyword List of keywords used to subset subjects.
   * @param familyKeyword List of keywords used to subset families.
   * @param seriesKeyword List of keywords used to subset series.
   * @param interval Time series interval, e.g., `Some(1)` for monthly, `Some(3)` for quarterly, and `Some(12)` for annual.
   * @param offset Start period, e.g., `interval=Some(12)` and `offset=Some(12)` would denote a December year.
   * @param limit Keep only `limit` series.
   * @param drop Drop first `offset` series.  Can be used in conjunction with `limit` to emulate pagination.
   */
  def getInfo(
    format: Option[Format], subjectCode: List[String], familyCode: List[String], familyNbr: List[Int], 
    seriesCode: List[String], subjectKeyword: List[String], familyKeyword: List[String], 
    seriesKeyword: List[String], interval: Option[Int], offset: Option[Int], limit: Option[Int], drop: Option[Int]
  ) = Action.async {
    val appliedLimit: Option[Int] = format match {
      case Some(Format.CSV) => {
        (seriesLimitCSV, limit) match {
          case (Some(m), Some(n)) => Some(math.min(m, n))
          case (Some(m), None) => Some(m)
          case (None, Some(n)) => Some(n)
          case _ => None
        }
      }
      case _ => {
        (seriesLimitJSON, limit) match {
          case (Some(m), Some(n)) => Some(math.min(m, n))
          case (Some(m), None) => Some(m)
          case (None, Some(n)) => Some(n)
          case _ => None
        }
      }
    }

    val info: Future[Seq[SeriesInfo]] = 
      snzts.selectSeriesInfo2(
        subjectCode, familyCode.distinct, familyNbr.distinct, seriesCode.distinct,
        subjectKeyword.distinct, familyKeyword.distinct, seriesKeyword.distinct,
        interval, offset, appliedLimit, drop
      )

    format match {
      case Some(Format.CSV) => {
        val res = snzts.infoCSV(info)
        res.map(x => Ok(x).as("text/csv"))
      }
      case _ => {
        val res = snzts.infoJSON(info)
        res.map(x => Ok(x).as("application/json"))
      }
    }                    
  } 

  /**
   * Return one or more time series.
   *
   * @param format Output format, e.g. `Some(Format.CSV)` or `Some(Format.JSON)`.
   * @param seriesCode List of time series identifiers.
   * @param start Start date, e.g. `Some(1986.03)`.
   * @param end End date, e.g. `Some(2018.12)`.
   * @param head If supplied, include the first `head` elements of each series.
   * @param tail If supplied, include the last `tail` elements of each series.
   * @param title Optional title to be used when format is `Format.CHART`.
   */  
  def getSeries(
    format: Option[Format], seriesCode: List[String], start: Option[String], end: Option[String],
    head: Option[Int], tail: Option[Int], title: Option[String]
  ) =  Action.async {
    val series: Future[Seq[SeriesInfo]] = snzts.selectSeriesInfo1(seriesCode.distinct)
    val data: Future[Seq[Data]] = snzts.selectData1(seriesCode.distinct, start, end, head, tail)

    format match {
      case Some(Format.CSV) => {
        val res = snzts.seriesCSV(series, data)
        res.map(x => Ok.chunked(Source(x)).as("text/csv"))
      }
      case Some(Format.CHART) => {
        val res = snzts.seriesJSON(series, data)
        res.map(x => Ok(views.html.highchart(title, "[" + x.mkString + "]")))
      }
      case _ => {
        val res = snzts.seriesJSON(series, data)
        res.map(x => Ok.chunked(Source(List("[") ++ x ++ List("]"))).as("application/json"))
      }
    }    
  }

  /**
   * Return one or more time series.
   *
   * @param format Output format, e.g. `Some(Format.CSV)` or `Some(Format.JSON)`.
   * @param subjectCode Subject code, e.g. `Some(HLF)`.
   * @param familyCode Family code, e.g. `Some(SA)`. 
   * @param familyNbr Family number.
   * @param seriesCode List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   * @param subjectKeyword List of keywords used to subset subjects.
   * @param familyKeyword List of keywords used to subset families.
   * @param seriesKeyword List of keywords used to subset series.
   * @param interval Time series interval, e.g., `Some(1)` for monthly, `Some(3)` for quarterly, and `Some(12)` for annual.
   * @param offset Start period, e.g., `interval=Some(12)` and `offset=Some(12)` would denote a December year.
   * @param limit Keep only `limit` series.
   * @param drop Drop first `offset` series.  Can be used in conjunction with `limit` to emulate pagination.
   * @param head If supplied, include the first `head` elements of each series.
   * @param tail If supplied, include the last `tail` elements of each series.
   */
  def getDataset(
    format: Option[Format], subjectCode: List[String], familyCode: List[String], familyNbr: List[Int], 
    seriesCode: List[String], subjectKeyword: List[String], familyKeyword: List[String], 
    seriesKeyword: List[String], interval: Option[Int], offset: Option[Int],
    limit: Option[Int], drop: Option[Int], head: Option[Int], tail: Option[Int]
  ) = Action.async {
    val appliedLimit: Option[Int] = format match {
      case Some(Format.CSV) => {
        (seriesLimitCSV, limit) match {
          case (Some(m), Some(n)) => Some(math.min(m, n))
          case (Some(m), None) => Some(m)
          case (None, Some(n)) => Some(n)
          case _ => None
        }
      }
      case _ => {
        (seriesLimitJSON, limit) match {
          case (Some(m), Some(n)) => Some(math.min(m, n))
          case (Some(m), None) => Some(m)
          case (None, Some(n)) => Some(n)
          case _ => None
        }
      }
    }

    val series: Future[Seq[SeriesInfo]] = 
      snzts.selectSeriesInfo2(
        subjectCode.distinct, familyCode.distinct, familyNbr.distinct, seriesCode.distinct,
        subjectKeyword.distinct, familyKeyword.distinct, seriesKeyword.distinct,
        interval, offset, appliedLimit, drop
      )

    val data: Future[Seq[Data]] = 
      snzts.selectData2(
        subjectCode.distinct, familyCode.distinct, familyNbr.distinct, seriesCode.distinct,
        subjectKeyword.distinct, familyKeyword.distinct, seriesKeyword.distinct,
        interval, offset, appliedLimit, drop, head, tail
      )

    format match {
      case Some(Format.CSV) => {
        val res = snzts.seriesCSV(series, data)
        res.map(x => Ok.chunked(Source(x)).as("text/csv"))
      }
      case _ => {
        val res = snzts.seriesJSON(series, data)
        res.map(x => Ok.chunked(Source(List("[") ++ x ++ List("]"))).as("application/json"))
      }
    }                    
  } 
}