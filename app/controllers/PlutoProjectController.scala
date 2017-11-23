package controllers

import com.gu.media.pluto.PlutoUpsertRequest
import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.Controller

class PlutoProjectController(val authActions: HMACAuthActions, override val stores: DataStores) extends Controller
  with UnpackedDataStores
  with JsonRequestParsing {

  import authActions.APIHMACAuthAction

  def listProjects() = APIHMACAuthAction {
    val plutoProjects = stores.plutoProjectStore.list()
    Ok(Json.toJson(plutoProjects))
  }

  def upsertProject() = APIHMACAuthAction { implicit req =>
    parse[PlutoUpsertRequest](req) { data: PlutoUpsertRequest => {
      val project = stores.plutoProjectStore.upsert(data)
      Ok(Json.toJson(project))
    }}
  }
}
