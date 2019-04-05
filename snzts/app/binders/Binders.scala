package binders

import models.Format

import play.api.mvc.{PathBindable, QueryStringBindable}

/**
 * Convert query parameters to required classes.
 */
object Binders{
  /**
   * Implicit conversion between String and Format
   */
  implicit def formatQueryStringBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[Format] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Format]] = {
      stringBinder.bind(key, params) map {
        case Right(format) => Right(Format(format))
        case _ => Left("Unable to bind a format")
      }
    }

    override def unbind(key: String, format: Format): String = {
      stringBinder.unbind(key, format.format)
    }
  }

  /**
   * Implicit conversion between String and Format
   */
  implicit def formatPathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Format] {
    override def bind(key: String, value: String): Either[String, Format] =
      stringBinder.bind(key, value) match {
        case Right(format) => Right(Format(format))
        case _ => Left("Unable to bind a format")
      }

    override def unbind(key: String, format: Format): String = format.format
  }
}