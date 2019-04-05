package controllers

import javax.inject.Inject
import play.api.mvc.BaseController
import play.api.mvc.ControllerComponents

/**
 * Say "Hello!".
 */
class HelloController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  /**
   * Say "Hello, name!" 
   *
   * @param name Optional string
   */
  def get(name: Option[String]) = Action {
    name match {
      case Some(s) => Ok(s"Hello, ${s}!")
      case _ => Ok("Hello, World!")
    }
  }
}