package test

import data._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any
import play.api.inject.guice.GuiceableModuleConversions
import play.api.test._
import play.api.inject.bind
import play.api.test.Helpers._
import play.api.inject.Injector
import scala.util.Failure
import controllers.ReindexController

import TestData._

class ReindexSpec
    extends MediaAtomSuite
    with MockitoSugar {

  object mock {
    val pubFail = {
      val reindexKinesisMock = mock[AtomReindexer]
      when(reindexKinesisMock.reindexAtoms(any())).thenReturn(Failure(new Exception()))
      reindexKinesisMock
    }
  }




  "reindexer" should {
    "return error if publisher fails" in injectedTest(
      bind[AtomReindexer].to(mock.pubFail)
    ) { implicit inj =>
      val res = call(reindexController.reindexLive(None, None), FakeRequest())
      status(res) mustEqual INTERNAL_SERVER_ERROR
    }
  }

}
