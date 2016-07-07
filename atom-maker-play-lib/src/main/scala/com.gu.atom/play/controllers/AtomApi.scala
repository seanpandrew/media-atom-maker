package com.gu.atom.play.controllers

import cats.data.Xor
import play.api.mvc._
import com.gu.atom.data._

import play.api.libs.json._
import play.api.http.Writeable._

// import com.gu.contentatom.thrift.{ ContentAtomEvent, EventType }
// import com.gu.contentatom.thrift.atom.media.Asset
import com.gu.contentatom.thrift.{ Atom, AtomData }
// import com.gu.pandomainauth.action.AuthActions

// import play.api.Configuration

// import util.atom.MediaAtomImplicits

// import javax.inject._
// import java.util.{ Date, UUID }
import play.api.libs.json._

// import model.ThriftUtil
// import ThriftUtil._
// import data._
// import play.api.libs.concurrent.Execution.Implicits.defaultContext
// import scala.util.{ Success, Failure }

// import data.JsonConversions._

trait AtomApi extends Controller {

  val dataStore: DataStore
  val publisher: AtomPublisher

  protected def jsonError(msg: String): JsObject = JsObject(Seq("error" -> JsString(msg)))
  protected def jsonError(ex: Throwable): JsObject = jsonError(ex.getMessage)

  /**
   * Create a new Atom by parsing the Request
   */
  def atomCreate(implicit req: Request[_]): Xor[String, Atom]

  /* you should provide this, it will need to have knowledge of your
   * specific atom type, it can encode the AtomData field */
  implicit val atomToJson: Writes[Atom]

  /* what is the URL of an atom that we should send people to in links
   * and 301's */
  private def atomUrl(id: String) = s"/atom/$id"

  def getAtom(id: String) = Action { implicit req =>
    dataStore.getMediaAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def createMediaAtom = Action { implicit req =>
    val res = for {
      atom <- atomCreate
      _ <- dataStore.createMediaAtom(atom)
    } yield {
      Created(Json.toJson(atom))
        .withHeaders("Location" -> atomUrl(atom.id))
    }
    res valueOr {
      case IDConflictError =>
        Conflict(s"already exists")
      case _ => InternalServerError("Unknown error")
    }
  }
}

// class Api @Inject() (
//   val dataStore: DataStore,
//   val publisher: AtomPublisher,
//   val conf: Configuration,
//   val authActions: AuthActions
// )
//     extends AtomController
//     with MediaAtomImplicits {

//   // takes a configured URL object and shows how it would look as a content atom

//   def createMediaAtom = thriftResultAction(atomBodyParser) { implicit req =>
//     val atom = req.body
//     dataStore.createMediaAtom(atom).fold(
//       {
//         case data.IDConflictError =>
//           Conflict(s"${atom.id} already exists")
//         case _ => InternalServerError("Unknown error")
//       },
//       _ => Created(Json.toJson(atom))
//         .withHeaders("Location" -> atomUrl(atom.id))
//     )
//   }

//   def addAsset(atomId: String) = thriftResultAction(assetBodyParser) { implicit req =>
//     val newAsset = req.body
//     dataStore.getMediaAtom(atomId) match {
//       case Some(atom) =>
//         val ma = atom.tdata
//         val assets = ma.assets
//         val newAtom = atom
//           .withData(ma.copy(
//             activeVersion = newAsset.version,
//             assets = newAsset +: assets
//           ))
//           .withRevision(_ + 1)

//         dataStore.updateMediaAtom(newAtom).fold(
//           err => InternalServerError(err.msg),
//           _ => Created(s"updated atom $atomId").withHeaders("Location" -> atomUrl(atom.id))
//         )
//       case None => NotFound(s"atom not found $atomId")
//     }
//   }

//   def now() = (new Date()).getTime()

//   def publishAtom(atomId: String) = APIAuthAction { implicit req =>
//     dataStore.getMediaAtom(atomId) match {
//       case Some(atom) =>
//         val event = ContentAtomEvent(atom, EventType.Update, now())
//         publisher.publishAtomEvent(event) match {
//           case Success(_) => NoContent
//           case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
//         }
//       case None => NotFound(jsonError(s"No such atom $atomId"))
//     }
//   }

//   def revertAtom(atomId: String, version: Long) = APIAuthAction { implicit req =>
//     dataStore.getMediaAtom(atomId) match {
//       case Some(atom) =>
//         if (!atom.tdata.assets.exists(_.version == version)) {
//           InternalServerError(jsonError(s"no asset is listed for version $version"))
//         } else {
//           dataStore.updateMediaAtom(
//             atom
//               .withRevision(_ + 1)
//               .updateData { media => media.copy(activeVersion = version) }
//           )
//           Ok(s"updated to $version")
//         }
//       case None => NotFound(s"atom not found $atomId")
//     }
//   }

//   // TODO -> this needs to handle paging
//   def listAtoms = APIAuthAction { implicit req =>
//     dataStore.listAtoms.fold(
//       err => InternalServerError(jsonError(err.msg)),
//       atoms => Ok(Json.toJson(atoms.toList))
//     )
//   }
// }

