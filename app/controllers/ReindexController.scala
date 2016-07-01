package controllers

import cats.data.Xor
import com.gu.contentatom.thrift.Atom
import com.gu.pandomainauth.action.AuthActions
import data._

import javax.inject.Inject
import play.api.libs.json.{ JsObject, JsNumber }
import play.api.mvc.Action

import play.Logger
import scala.util.{ Failure, Success }

class ReindexController @Inject() (
  publisher: AtomReindexer,
  dataStore: DataStore,
  val authActions: AuthActions
) extends AtomController {

  def reindexAtoms(atoms: TraversableOnce[Atom]): Xor[Throwable, Long] = {
    publisher.reindexAtoms(atoms) match {
      case Success(count) => Xor.Right(count)
      case Failure(err)   => Xor.Left(err)
    }
  }

  def reindexLive(from: Option[Long], to: Option[Long]) = Action { implicit req =>
    val res = for {
      atoms <- dataStore.listAtoms
      count <- reindexAtoms(atoms)
    } yield count
    res.fold(
      err   => InternalServerError(jsonError(err)),
      count => Ok(JsObject("count" -> JsNumber(count) :: Nil))
    )
  }
}
