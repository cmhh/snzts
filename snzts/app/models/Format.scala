package models

import java.lang.IllegalArgumentException

/**
 * Class to represent permissible output formats.
 */
case class Format(format: String) {
  if (!List("JSON", "CSV", "CHART").contains(format.toUpperCase)) 
    throw new IllegalArgumentException("Unknown format requested.")

  private val fmt = format.toUpperCase
  
  override def toString = fmt
}

object Format {
  def apply(format: String): Format = {
    if (!List("JSON", "CSV", "CHART").contains(format.toUpperCase)) new Format("JSON")
    else new Format(format.toUpperCase)
  }

  val JSON  = Format("JSON")
  val CSV   = Format("CSV")
  val CHART = Format("CHART")
}