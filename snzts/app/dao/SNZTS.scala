package dao

import models._

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.{ JdbcProfile, ResultSetType, ResultSetConcurrency }
import slick.basic.DatabasePublisher
import slick.collection.heterogeneous._
import slick.collection.heterogeneous.syntax._

/**
 * Database access object for snzts database.
 */
class SNZTS @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) 
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._


  /**
   * Interface for table `subject`.
   */
  private class SubjectTable(tag: Tag) extends Table[Subject](tag, "subject") {
    def subjectCode = column[String]("subject_code")
    def title = column[String]("title_text")
    def * = (subjectCode, title) <> (Subject.tupled, Subject.unapply)
  }

  /**
   * Interface for table `subject`.
   */
  private lazy val SubjectTable = TableQuery[SubjectTable]
  
  /**
   * Interface for table `family`.
   */
  private class FamilyTable(tag: Tag) extends Table[Family](tag, "family") {
    def subjectCode = column[String]("subject_code")
    def familyCode = column[String]("family_code")
    def familyNbr = column[Int]("family_nbr")
    def title = column[String]("title_text")
    def title1 = column[Option[String]]("title_1")
    def title2 = column[Option[String]]("title_2")
    def title3 = column[Option[String]]("title_3")
    def title4 = column[Option[String]]("title_4")
    def title5 = column[Option[String]]("title_5")
    def * = (subjectCode, familyCode, familyNbr, title, title1, title2, title3, title4, title5) <> (Family.tupled, Family.unapply)
  }
  
  /**
   * Interface for table `family`.
   */
  private lazy val FamilyTable = TableQuery[FamilyTable]
  
  /**
   * Interface for table `series`.
   */
  private class SeriesTable(tag: Tag) extends Table[Series](tag, "series") {
    def subjectCode = column[String]("subject_code")
    def familyCode = column[String]("family_code")
    def familyNbr = column[Int]("family_nbr")
    def seriesCode = column[String]("series_code")
    def interval = column[Int]("series_interval_nbr")
    def offset = column[Int]("mnth_offset_nbr")
    def magnitude = column[Int]("magnitude_nbr")
    def unit = column[String]("unit_text")
    def code1 = column[Option[String]]("code_1")
    def code2 = column[Option[String]]("code_2")
    def code3 = column[Option[String]]("code_3")
    def code4 = column[Option[String]]("code_4")
    def code5 = column[Option[String]]("code_5")
    def desc1 = column[Option[String]]("description_1")
    def desc2 = column[Option[String]]("description_2")
    def desc3 = column[Option[String]]("description_3")
    def desc4 = column[Option[String]]("description_4")
    def desc5 = column[Option[String]]("description_5")
    def * = (
      subjectCode, familyCode, familyNbr, seriesCode, interval, offset, magnitude, unit, 
      code1, code2, code3, code4, code5, desc1, desc2, desc3, desc4, desc5
    ) <> (Series.tupled, Series.unapply)
  }
  
  /**
   * Interface for table `series`.
   */
  private lazy val SeriesTable = TableQuery[SeriesTable]
  
  /**
   * Interface for table `data`.
   */
  private class DataTable(tag: Tag) extends Table[Data](tag, "data") {
    def seriesCode = column[String]("series_code")
    def period = column[String]("period")
    def value = column[Option[Double]]("value")
    def status = column[String]("status")
    def m = column[Int]("m")
    def n = column[Int]("n")
    def * = (seriesCode, period, value, status, m, n) <> (Data.tupled, Data.unapply)
  }
  
  /**
   * Interface for table `data`.
   */
  private lazy val DataTable = TableQuery[DataTable]
  
  /**
   * Interface for table `series_info`.
   */
  private class SeriesInfoTable(tag: Tag) extends Table[SeriesInfo](tag, "series_info") {
    def subjectCode = column[String]("subject_code")
    def familyCode = column[String]("family_code")
    def familyNbr = column[Int]("family_nbr")
    def seriesCode = column[String]("series_code")
    def subjectTitle = column[String]("subject_title")
    def familyTitle = column[String]("family_title")
    def interval = column[Int]("series_interval_nbr")
    def offset = column[Int]("mnth_offset_nbr")
    def magnitude = column[Int]("magnitude_nbr")
    def unit = column[String]("unit_text")
    def title1 = column[Option[String]]("title_1")
    def title2 = column[Option[String]]("title_2")
    def title3 = column[Option[String]]("title_3")
    def title4 = column[Option[String]]("title_4")
    def title5 = column[Option[String]]("title_5")
    def code1 =  column[Option[String]]("code_1")
    def code2 =  column[Option[String]]("code_2")
    def code3 =  column[Option[String]]("code_3")
    def code4 =  column[Option[String]]("code_4")
    def code5 =  column[Option[String]]("code_5")
    def desc1 =  column[Option[String]]("description_1")
    def desc2 =  column[Option[String]]("description_2")
    def desc3 =  column[Option[String]]("description_3")
    def desc4 =  column[Option[String]]("description_4")
    def desc5 =  column[Option[String]]("description_5")

    def * = (
      subjectCode :: familyCode :: familyNbr :: seriesCode ::  
      subjectTitle :: familyTitle :: interval :: offset :: magnitude :: unit ::
      title1 :: title2 :: title3 :: title4 :: title5 ::
      code1 :: code2 :: code3 :: code4 :: code5 :: 
      desc1 :: desc2 :: desc3 :: desc4 :: desc5 :: HNil
    ) <> (createSeriesInfo, extractSeriesInfo)

    type SeriesInfoHList = 
      String :: String :: Int :: String :: String :: String :: Int :: Int :: Int :: String :: 
      Option[String] :: Option[String] :: Option[String] :: Option[String] :: Option[String] :: 
      Option[String] :: Option[String] :: Option[String] :: Option[String] :: Option[String] :: 
      Option[String] :: Option[String] :: Option[String] :: Option[String] :: Option[String] :: HNil

    def createSeriesInfo(data: SeriesInfoHList): SeriesInfo = data match {
      case 
        subjectCode :: familyCode :: familyNbr :: seriesCode :: 
        subjectTitle :: familyTitle :: interval :: offset :: magnitude :: unit ::
        title1 :: title2 :: title3 :: title4 :: title5 ::
        code1 :: code2 :: code3 :: code4 :: code5 :: 
        desc1 :: desc2 :: desc3 :: desc4 :: desc5 :: HNil =>
          SeriesInfo(
            subjectCode, familyCode, familyNbr, seriesCode,  
            subjectTitle, familyTitle, interval, offset, magnitude, unit,
            title1, title2, title3, title4, title5,
            code1, code2, code3, code4, code5,
            desc1, desc2, desc3, desc4, desc5
          )
    }

    def extractSeriesInfo(data: SeriesInfo): Option[SeriesInfoHList] = data match {
      case
        SeriesInfo(
          subjectCode, familyCode, familyNbr, seriesCode, 
          subjectTitle, familyTitle, interval, offset, magnitude, unit,
          title1, title2, title3, title4, title5,
          code1, code2, code3, code4, code5,
          desc1, desc2, desc3, desc4, desc5
        ) => Some(
          subjectCode :: familyCode :: familyNbr :: seriesCode ::  
          subjectTitle :: familyTitle :: interval :: offset :: magnitude :: unit ::
          title1 :: title2 :: title3 :: title4 :: title5 ::
          code1 :: code2 :: code3 :: code4 :: code5 :: 
          desc1 :: desc2 :: desc3 :: desc4 :: desc5 :: HNil
        )
    }
  }
  
  /**
   * Interface for table `series_info`.
   */
  private lazy val SeriesInfoTable = TableQuery[SeriesInfoTable]

  /**
   * Select information from `subject` table.
   *
   * @param subjectCode Subject code, e.g. `Some("HLF")` or `Some("BLD")`.
   * @param subjectKeywords List of keywords used to filter by subject, e.g. `List("Labour", "Force")`.  These are joined by `AND`.
   */
  def selectSubjects(subjectCode: Option[String], subjectKeywords: List[String]): Future[Seq[Subject]] = {
    val q1 = subjectCode match {
      case Some(s) => SubjectTable.filter(_.subjectCode === s)
      case _ => SubjectTable
    }

    val q2 = if (subjectKeywords.size > 0)
      q1.filter {
        m => subjectKeywords.map(kw => m.title.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _)
      } else q1 

    db.run(q2.result)
  }

  /**
   * Select information from `family` table.
   *
   * @param subjectCode Subject code, e.g. `Some("HLF")` or `Some("BLD")`.
   * @param familyCode Family code, e.g. `Some("SA")`.
   * @param familyNbr Family number.
   * @param subjectKeywords List of keywords used to filter by subject, e.g. `List("Labour", "Force")`.  These are joined by `AND`.
   * @param familyKeywords List of keywords used to filter by family.  These are joined by `AND`.
   */
  def selectFamilies(subjectCode: Option[String], 
                     familyCode: Option[String], familyNbr: Option[Int],
                     subjectKeywords: List[String], familyKeywords: List[String]): Future[Seq[Family]] = {
    val q1 = if (subjectKeywords.size > 0) {
      for {
        s <- SubjectTable.filter { m => subjectKeywords.map(kw => m.title.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _) }
        f <- FamilyTable if s.subjectCode === f.subjectCode
      } yield f
    } else FamilyTable

    val q2 = subjectCode match {
      case Some(s) => q1.filter(_.subjectCode === s)
      case _ => q1
    }

    val q3 = familyCode match {
      case Some(s) => q2.filter(_.familyCode === s)
      case _ => q2
    }

    val q4 = familyNbr match {
      case Some(i) => q3.filter(_.familyNbr === i)
      case _ => q3
    }

    val q5 = if (familyKeywords.size > 0)
      q4.filter {
        x => familyKeywords.map(kw => x.title.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _)
      } else q4

    val q6 = q5.sortBy(x => (x.subjectCode, x.familyCode, x.familyNbr))

    db.run(q6.result)
  }

  /**
   * Select information from `series_info` table.
   *
   * @param seriesCodes List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   */
  def selectSeriesInfo1(seriesCodes: List[String]): Seq[SeriesInfo] = {
    val q1 = SeriesInfoTable.filter(_.seriesCode.inSet(seriesCodes)).sortBy(x => (x.seriesCode))

    // Shouldn't Await, but can't get Action.async working when consuming 2 futures... for now :(
    Await.result(db.run(q1.result), Duration.Inf)
  }

  /**
   * Select information from `series_info` table.
   *
   * @param subjectCodes  List of subject codes, e.g. `List("HLF","BLD")`.
   * @param familyCodes List of family codes, e.g. `List("SA")`.
   * @param familyNbrs List of family numbers.
   * @param seriesCodes List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   * @param subjectKeywords List of keywords used to filter by subject, e.g. `List("Labour", "Force")`.  These are joined by `AND`.
   * @param familyKeywords List of keywords used to filter by family.  These are joined by `AND`.
   * @param seriesKeywords List of keywords used to filter series--fields searched are titles and class labels.
   * @param interval Time series interval, e.g., `Some(1)` for monthly, `Some(3)` for quarterly, and `Some(12)` for annual.
   * @param offset Start period, e.g., `interval=Some(12)` and `offset=Some(12)` would denote a December year.
   * @param limit Maximum number of unique series objects to return.
   * @param drop Used in conjunction with `limit` to retrieve a result set in batches.  For example, to retrieve 1001 through 2000, set `limit=Some(1000)` and `drop=Some(1000)`.
   */
  def selectSeriesInfo2(subjectCodes: List[String], familyCodes: List[String], familyNbrs: List[Int], seriesCodes: List[String], 
                        subjectKeywords: List[String], familyKeywords: List[String], seriesKeywords: List[String],
                        interval: Option[Int], offset: Option[Int],
                        limit: Option[Int], drop: Option[Int]): Seq[SeriesInfo] = {
    val q1 = if (subjectCodes.size > 0) {
      SeriesInfoTable.filter(_.subjectCode.inSet(subjectCodes))
    } else SeriesInfoTable

    val q2 = if (familyCodes.size > 0) {
      q1.filter(_.familyCode.inSet(familyCodes))
    } else q1

    val q3 = if (familyNbrs.size > 0) {
      q2.filter(_.familyNbr.inSet(familyNbrs))
    } else q2

    val q4 = if (seriesCodes.size > 0) {
      q3.filter(_.seriesCode.inSet(seriesCodes))
    } else q3
    
    val q5 = if (subjectKeywords.size > 0)
      q4.filter { m => subjectKeywords.map(kw => m.subjectTitle.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _) }
    else
      q4
    
    val q6 = if (familyKeywords.size > 0)
      q5.filter { m => familyKeywords.map(kw => m.familyTitle.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _) }
    else 
      q5    

    val q7 = if (seriesKeywords.size > 0) {
      q6.filter { m => seriesKeywords.map(kw => {
        (m.desc1.toLowerCase like s"%${kw.toLowerCase}%") || (m.desc2.toLowerCase like s"%${kw.toLowerCase}%") || 
        (m.desc3.toLowerCase like s"%${kw.toLowerCase}%") || (m.desc4.toLowerCase like s"%${kw.toLowerCase}%") || 
        (m.desc5.toLowerCase like s"%${kw.toLowerCase}%")}).reduceLeft( _ && _)}
    } else q6

    val q8 = interval match {
      case Some(i) => q7.filter(_.interval === i)
      case _ => q7
    }

    val q9 = offset match {
      case Some(i) => q8.filter(_.interval === i)
      case _ => q8
    }

    val q10 = (limit, drop) match {
      case (Some(m), Some(n)) => q9.sortBy(x => (x.seriesCode)).drop(n).take(m)
      case (Some(m), None)    => q9.sortBy(x => (x.seriesCode)).take(m)
      case (None, Some(n))    => q9.sortBy(x => (x.seriesCode)).drop(n)
      case _                  => q9
    }

    val q11 = q10.sortBy(x => (x.seriesCode))

    // Shouldn't Await, but can't get Action.async working when consuming 2 futures... for now :(
    Await.result(db.run(q11.result), Duration.Inf)      
  } 

  /**
   * Select information from `data` table.
   *
   * @param seriesCodes List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   * @param start A start date, e.g. `Some("1986.03")`.
   * @param end An end data, e.g. `Some("2018.03")`.
   * @param head The number of observations to keep from the start of each series.
   * @param tail The number of observations to keep from the end of each series.
   */
  def selectData1(seriesCodes: List[String], 
                  start: Option[String], end: Option[String], 
                  head: Option[Int], tail: Option[Int]): Seq[Data] = {
    val q1 =  DataTable.filter(_.seriesCode.inSet(seriesCodes))

    val q2 = start match {
      case Some(s) => q1.filter(_.period >= s)
      case _ => q1
    }   

    val q3 = end match {
      case Some(s) => q2.filter(_.period <= s)
      case _ => q2
    }

    val q4 = (head, tail) match {
      case (Some(m), Some(n)) => q3.filter(q => q.m <= head || q.n <= tail)
      case (Some(m), None)    => q3.filter(_.m <= head)
      case (None, Some(n))    => q3.filter(_.n <= tail)
      case _                  => q3
    }

    val q5 = q4.sortBy(x => (x.seriesCode, x.period))

    // Shouldn't Await, but can't get Action.async working when consuming 2 futures... for now :(
    Await.result(db.run(q5.result), Duration.Inf)              
  }

  /**
   * Select information from `data` table.
   *
   * @param subjectCodes  List of subject codes, e.g. `List("HLF","BLD")`.
   * @param familyCodes List of family codes, e.g. `List("SA")`.
   * @param familyNbrs List of family numbers.
   * @param seriesCodes List of series codes, e.g. `List("HLFQ.SAA1AZ", "HLFQ.SAA2AZ")`.
   * @param subjectKeywords List of keywords used to filter by subject, e.g. `List("Labour", "Force")`.  These are joined by `AND`.
   * @param familyKeywords List of keywords used to filter by family.  These are joined by `AND`.
   * @param seriesKeywords List of keywords used to filter series--fields searched are titles and class labels.
   * @param interval Time series interval, e.g., `Some(1)` for monthly, `Some(3)` for quarterly, and `Some(12)` for annual.
   * @param offset Start period, e.g., `interval=Some(12)` and `offset=Some(12)` would denote a December year.
   * @param limit Maximum number of unique series objects to return.
   * @param drop Used in conjunction with `limit` to retrieve a result set in batches.  For example, to retrieve 1001 through 2000, set `limit=Some(1000)` and `drop=Some(1000)`.
   * @param head The number of observations to keep from the start of each series.
   * @param tail The number of observations to keep from the end of each series.
   */
  def selectData2(subjectCodes: List[String], familyCodes: List[String], familyNbrs: List[Int], seriesCodes: List[String], 
                  subjectKeywords: List[String], familyKeywords: List[String], seriesKeywords: List[String],
                  interval: Option[Int], offset: Option[Int],
                  limit: Option[Int], drop: Option[Int],
                  head: Option[Int], tail: Option[Int]): Seq[Data] = {
    val q1 = if (subjectCodes.size > 0) {
      SeriesInfoTable.filter(_.subjectCode.inSet(subjectCodes))
    } else SeriesInfoTable

    val q2 = if (familyCodes.size > 0) {
      q1.filter(_.familyCode.inSet(familyCodes))
    } else q1

    val q3 = if (familyNbrs.size > 0) {
      q2.filter(_.familyNbr.inSet(familyNbrs))
    } else q2

    val q4 = if (seriesCodes.size > 0) {
      q3.filter(_.seriesCode.inSet(seriesCodes))
    } else q3
    
    val q5 = if (subjectKeywords.size > 0)
      q4.filter { m => subjectKeywords.map(kw => m.subjectTitle.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _) }
    else
      q4
    
    val q6 = if (familyKeywords.size > 0)
      q5.filter { m => familyKeywords.map(kw => m.familyTitle.toLowerCase.like(s"%${kw.toLowerCase}%")).reduceLeft(_ && _) }
    else 
      q5        

    val q7 = if (seriesKeywords.size > 0) {
      q6.filter { m => seriesKeywords.map(kw => {
        (m.desc1.toLowerCase like s"%${kw.toLowerCase}%") || (m.desc2.toLowerCase like s"%${kw.toLowerCase}%") || 
        (m.desc3.toLowerCase like s"%${kw.toLowerCase}%") || (m.desc4.toLowerCase like s"%${kw.toLowerCase}%") || 
        (m.desc5.toLowerCase like s"%${kw.toLowerCase}%")}).reduceLeft( _ && _)}
    } else q6

    val q8 = interval match {
      case Some(i) => q7.filter(_.interval === i)
      case _ => q7
    }

    val q9 = offset match {
      case Some(i) => q8.filter(_.interval === i)
      case _ => q8
    }

    val q10 = (limit, drop) match {
      case (Some(m), Some(n)) => q9.sortBy(x => (x.seriesCode)).drop(n).take(m)
      case (Some(m), None)    => q9.sortBy(x => (x.seriesCode)).take(m)
      case (None, Some(n))    => q9.sortBy(x => (x.seriesCode)).drop(n)
      case _                  => q9
    }

    val q11 = for {
      series <- q10
      data <- DataTable if series.seriesCode === data.seriesCode 
    } yield data

    val q12 = (head, tail) match {
      case (Some(m), Some(n)) => q11.filter(x => (x.m <= m || x.n <= n))
      case (Some(m), None)    => q11.filter(_.m <= m)
      case (None, Some(n))    => q11.filter(_.n <= n)
      case _                  => q11
    }

    val q13 = q12.sortBy(x => (x.seriesCode, x.period))

    // Shouldn't Await, but can't get Action.async working when consuming 2 futures... for now :(
    Await.result(db.run(q13.result), Duration.Inf)
  } 

  /**
   * Convert a sequence of `Subject` objects to CSV.
   */
  def subjectsCSV(s: Seq[Subject]): String = 
    """"subject_code","title_text"""" + "\n" +
            s.map(s => {s""""${s.subjectCode}","${s.titleText}""""}).mkString("\n")

  /**
   * Convert a sequence of `Subject` objects to JSON.
   */
  def subjectsJSON(s: Seq[Subject]): String = 
    "[" + s.map(s => {s"""{"subject_code":"${s.subjectCode}","title_text":"${s.titleText}"}"""}).mkString(",") + "]"

  /**
   * Convert a sequence of `Family` objects to CSV.
   */
  def familiesCSV(s: Seq[Family]): String = 
    """"subject_code", "family_code","family_nbr","title_text"""" + "\n" +
            s.map(s => {s""""${s.subjectCode}","${s.familyCode}",${s.familyNbr},"${s.titleText}""""}).mkString("\n") 

  /**
   * Convert a sequence of `Family` objects to JSON.
   */
  def familiesJSON(s: Seq[Family]): String =
    "[" + s.map(s => {
      s"""{"subject_code":"${s.subjectCode}","family_code":"${s.familyCode}","family_nbr":${s.familyNbr},"title_text":"${s.titleText}"}"""
    }).mkString(",") + "]"

  /**
   * Convert a sequence of `SeriesInfo` objects to CSV.
   */
  def infoCSV(s: Seq[SeriesInfo]): String = {
    val h = """"subject_code","family_code","family_nbr","series_code",
    |"subject_title","family_title","interval","offset","magnitude","unit",
    |"title_1","title_2","title_3","title_4","title_5",
    |"desc_1","desc_2","desc_3","desc_4","desc_5""""
      .stripMargin.replaceAll("\n", "") 
    h + "\n" +
      s.map(s => {
        s""""${s.subjectCode}","${s.familyCode}",${s.familyNbr},"${s.seriesCode}",
        |"${s.subjectTitle}","${s.familyTitle}",${s.interval},${s.offset},${s.magnitude},"${s.unit}",
        |"${s.title1}","${s.title2}","${s.title3}","${s.title4}","${s.title5}",
        |"${s.desc1}","${s.desc2}","${s.desc3}","${s.desc4}","${s.desc5}""""
          .stripMargin.replaceAll("\n", "") 
      }).mkString("\n")
  }

  /**
   * Convert a sequence of `SeriesInfo` objects to JSON.
   */
  def infoJSON(s: Seq[SeriesInfo]): String = {
    "[" + s.map(s => {
      val variables = List(s.title1, s.title2, s.title3, s.title4, s.title5)
        .flatten.map(s => s""""$s"""").mkString(",")
      val outcomes = List(s.desc1, s.desc2, s.desc3, s.desc4, s.desc5)
        .flatten.map(s => s""""$s"""").mkString(",")
      s"""{"subject_code":"${s.subjectCode}","family_code":"${s.familyCode}","family_nbr":${s.familyNbr},
      |"series_code":"${s.seriesCode}",
      |"subject_title":"${s.subjectTitle}","family_title":"${s.familyTitle}",
      |"interval":${s.interval},"offset":${s.offset},"magnitude":${s.magnitude},"unit":"${s.unit}",
      |"variables":[$variables],"outcomes":[$outcomes]}"""
        .stripMargin.replaceAll("\n", "") 
    }).mkString(",") + "]"
  }

  /**
   * Convert a pair of sequences of `SeriesInfo` and `Data` objects to CSV.
   */
  def seriesCSV(seriesInfo: Seq[SeriesInfo], data: Seq[Data]): List[String] = {
    def gets(s: Option[String]): String = s match {
      case Some(x) => s""""$x""""
      case _ => ""
    }
    def getn(n: Option[Double]): String = n match {
      case Some(d) => d.toString
      case _ => "null"
    }
    def seriesCSV_(s: Seq[SeriesInfo], d: Seq[Data], 
                   seriesCode: String, acc: List[String], series: String, first: Boolean): List[String] = {
      if (d.isEmpty | s.isEmpty) {
        acc :+ series
      } else {
        if (first == true){
          val h = s""" subject_code,family_code,family_nbr,series_code,
          |subject_title,family_title,var_1,var_2,var_3,var_4,var_5,
          |val_1,val_2,val_3,val_4,val_5,
          |interval,offset,magnitude,unit,period,value,status"""
            .stripMargin.replaceAll("\n", "")
          seriesCSV_(s, d, seriesCode, acc, series + h, false)
        } else {
          val dataRow = d.head
          dataRow.seriesCode match {
            case `seriesCode` => {
              val seriesRow = s.head
              val row = "\n" + 
                s""""${seriesRow.subjectCode}","${seriesRow.familyCode}",${seriesRow.familyNbr},
                |"${seriesRow.seriesCode}",
                |"${seriesRow.subjectTitle}","${seriesRow.familyTitle}",
                |${gets(seriesRow.title1)},${gets(seriesRow.title2)},${gets(seriesRow.title3)},
                |${gets(seriesRow.title4)},${gets(seriesRow.title5)},
                |${gets(seriesRow.desc1)},${gets(seriesRow.desc2)},${gets(seriesRow.desc3)},
                |${gets(seriesRow.desc4)},${gets(seriesRow.desc5)},
                |${seriesRow.interval},${seriesRow.magnitude},${seriesRow.offset},"${seriesRow.unit}",
                |${dataRow.period},${getn(dataRow.value)},"${dataRow.status}""""
                  .stripMargin.replaceAll("\n", "")
              seriesCSV_(s, d.tail, dataRow.seriesCode, acc, series + row, false)
            }
            case _ => {
              seriesCSV_(s.tail, d, dataRow.seriesCode, acc :+ series, "", false)
            }
          }
        }
      }
    }
    if (data.isEmpty) {
      List[String]()
    } else {
      seriesCSV_(seriesInfo, data, seriesInfo.head.seriesCode, List[String](), "", true)
    }
  }

  /**
   * Convert a pair of sequences of `SeriesInfo` and `Data` objects to JSON.
   */
  def seriesJSON(seriesInfo: Seq[SeriesInfo], data: Seq[Data]): List[String] = {
    def getn(n: Option[Double]): String = n match {
      case Some(d) => d.toString
      case _ => "null"
    }
    def seriesJSON_(s: Seq[SeriesInfo], d: Seq[Data], seriesCode: String, acc: List[String],
                    header: String, period: String, value: String, status: String,
                    first: Boolean, empty: Boolean): List[String] = {
      if (d.isEmpty | s.isEmpty) {
        val x = s"""{${header},"period":[${period}],"value":[${value}],"status":[${status}]}"""
        if (empty == true) acc :+ x else acc :+ ("," + x)
      }
      else {
        val dataRow = d.head
        dataRow.seriesCode match {
          case `seriesCode` => {
            val h = if (first == true) {              
              val seriesRow = s.head
              val variables = List(seriesRow.title1, seriesRow.title2, seriesRow.title3, seriesRow.title4, seriesRow.title5)
                              .flatten
                              .map(s => s""""$s"""")
                              .mkString(",")
              val outcomes  = List(seriesRow.desc1, seriesRow.desc2, seriesRow.desc3, seriesRow.desc4, seriesRow.desc5)
                              .flatten
                              .map(s => s""""$s"""")
                              .mkString(",")
              s""""subject_code":"${seriesRow.subjectCode}","subject_title":"${seriesRow.subjectTitle}",
              |"family_code":"${seriesRow.familyCode}","family_nbr":${seriesRow.familyNbr},"family_title":"${seriesRow.familyTitle}",
              |"series_code":"${seriesRow.seriesCode}","interval":${seriesRow.interval},
              |"magnitude":${seriesRow.magnitude},"offset":${seriesRow.offset},"units":"${seriesRow.unit}",
              |"variables":[$variables],"outcomes":[$outcomes]"""
                .stripMargin.replaceAll("\n", "")
            } else header
            seriesJSON_(s, d.tail, dataRow.seriesCode, acc, h,
                        period + (if (period != "") "," else "") + s""""${d.head.period}"""", 
                        value + (if (value != "") "," else "") + getn(d.head.value), 
                        status + (if (status != "") "," else "") + s""""${d.head.status}"""", false, empty)
          } 
          case _ => {
            val x = s"""{${header},"period":[${period}],"value":[${value}],"status":[${status}]}"""
            val a = if (empty == true) acc :+ x else acc :+ ("," + x)
            seriesJSON_(s.tail, d, dataRow.seriesCode, a, header, "", "", "", true, false)
          }
        }
      } 
    }
    if (seriesInfo.isEmpty | data.isEmpty) List[String]()
    else seriesJSON_(seriesInfo, data, seriesInfo.head.seriesCode, List[String](), "", "", "", "", true, true)
  }
}

/*
println("")
println(q4.result.statements.headOption.get)
println("")
*/