package controllers

import akka.util.Timeout
import com.gu.editorial.permissions.client.PermissionsProvider
import com.gu.media.MetricsAccess
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._

class DashboardController(override val authActions: HMACAuthActions,
                          override val permissions: PermissionsProvider,
                          override val stores: DataStores,
                          metrics: MetricsAccess) extends AtomController {

  import authActions.APIHMACAuthAction

  implicit val timeout = Timeout(30.seconds)

  def getMetrics = APIHMACAuthAction.async { req =>
    val forceRefresh = req.headers.get("X-Force-Refresh").nonEmpty

    metrics.getMetrics(forceRefresh).map { data =>
      Ok(Json.toJson(data))
    }
  }
}
