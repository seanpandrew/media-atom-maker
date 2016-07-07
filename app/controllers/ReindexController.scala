package controllers

import cats.data.Xor
import com.gu.contentatom.thrift.Atom
import com.gu.pandomainauth.action.AuthActions
import data._

import javax.inject.{ Inject, Named }
import play.api.libs.json.{ Json, JsObject, JsNumber, JsString }
import play.api.mvc.Action

import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask

import play.Logger
import scala.util.{ Failure, Success }

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import util.reindex._

class ReindexController @Inject() (
    @Named("reindex-lifecycle-manager") reindexManager: ActorRef,
    val authActions: AuthActions
) extends AtomController {

  implicit val timeout = Timeout(30.seconds)
  implicit val w = Json.writes[ReindexJob]

  def reindexStart(from: Option[Long], to: Option[Long]) =
    Action.async { implicit req =>
      (reindexManager ? Start(data.Query(None, None))) map {
        case job: ReindexJob => Ok(Json.toJson(job))
      }
    }

  def reindexStatus(id: Int) = Action.async { implicit req =>
    (reindexManager ? GetStatus(Some(id))) map {
      case job: ReindexJob => Ok(Json.toJson(job))
    }
  }
}
