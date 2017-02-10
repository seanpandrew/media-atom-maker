package controllers

import javax.inject._

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.ws._
import play.api.mvc.Action
import util.AWSConfig

class Vidispine @Inject() (val authActions: HMACAuthActions, val ws: WSClient, val awsConfig: AWSConfig) extends AtomController {
  import authActions.APIHMACAuthAction

  import scala.concurrent.ExecutionContext.Implicits._

  def version() = APIHMACAuthAction.async {
    ws.url(s"${awsConfig.vidispineUrl}/API/version")
      .withHeaders("Content-Type" -> "application/json")
      .withAuth(awsConfig.vidispineUsername, awsConfig.vidispinePassword, WSAuthScheme.BASIC)
      .get
      .map(response => {
        Ok(response.body)
      })
  }
}
