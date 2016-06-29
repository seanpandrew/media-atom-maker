package controllers

import javax.inject.Inject
import play.api.mvc.{ Controller, Action }

import play.Logger

class Reindex @Inject() () extends Controller {

  def reindexLive(from: Option[Long], to: Option[Long]) =
    Action {implicit req =>
      Logger.info("reindexLive triggered")
      Ok("")
    }

}
