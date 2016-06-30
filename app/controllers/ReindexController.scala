package controllers

import data._

import javax.inject.{ Inject, Named }
import play.api.mvc.{ Controller, Action }

import play.Logger

class ReindexController @Inject() (
  @Named("reindex") publisher: AtomPublisher,
  dataStore: DataStore
) extends Controller {

  def reindexLive(from: Option[Long], to: Option[Long]) =
    Action {implicit req =>
      for {
        atoms <- dataStore.listAtoms
        atom <- atoms
      } {
        Logger.info(s"reindexLive triggered: ${atoms}")
      }
      Ok("")
    }

}
