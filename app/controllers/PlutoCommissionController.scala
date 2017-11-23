package controllers

import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.Controller

class PlutoCommissionController(
  val authActions: HMACAuthActions,
  override val stores: DataStores
) extends Controller
  with UnpackedDataStores {

  import authActions.APIAuthAction

  def listCommissions() = APIAuthAction {
    val plutoCommissions = stores.plutoCommissionStore.list()
    Ok(Json.toJson(plutoCommissions))
  }
}
