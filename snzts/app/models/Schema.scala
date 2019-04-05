package models

/**
 * Case class to represent a subject record.
 */
case class Subject(
  subjectCode: String, titleText: String)

/**
 * Case class to represent a family record.
 */
case class Family(
  subjectCode: String, familyCode: String, familyNbr: Int, titleText: String, 
  title1: Option[String], title2: Option[String], title3: Option[String], title4: Option[String], title5: Option[String])

/**
 * Case class to represent a series record.
 */
case class Series(
  subjectCode: String, familyCode: String, familyNbr: Int, seriesCode: String, 
  interval: Int, offset: Int, magnitude: Int, unit: String, 
  code1: Option[String], code2: Option[String], code3: Option[String], code4: Option[String], code5: Option[String], 
  desc1: Option[String], desc2: Option[String], desc3: Option[String], desc4: Option[String], desc5: Option[String]
)

/**
 * Case class to represent a series record with subject and family titles added.
 */
case class SeriesInfo(
  subjectCode: String, familyCode: String, familyNbr: Int, seriesCode: String, 
  subjectTitle: String, familyTitle: String, 
  interval: Int, offset: Int, magnitude: Int, unit: String, 
  title1: Option[String], title2: Option[String], title3: Option[String], title4: Option[String], title5: Option[String],
  code1:  Option[String], code2:  Option[String], code3:  Option[String], code4:  Option[String], code5:  Option[String], 
  desc1:  Option[String], desc2:  Option[String], desc3:  Option[String], desc4:  Option[String], desc5:  Option[String]
)

/**
 * Case class to represent a data record.
 */
case class Data(
  seriesCode: String,
  period: String,
  value: Option[Double],
  status: String,
  m: Int,
  n: Int
)