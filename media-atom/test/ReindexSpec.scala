package test

import controllers.ReindexController
import com.gu.contentatom.thrift.Atom
import data._
import org.mockito.ArgumentCaptor
import org.scalatest.mock.MockitoSugar
import org.scalatest.MustMatchers
import org.mockito.Mockito._
import org.mockito.Matchers.any
import play.api.test._
import play.api.inject.bind
import play.api.test.Helpers._
import scala.util.{ Failure, Success }
import play.api.libs.json._

import play.api.inject.guice.GuiceableModule

import TestData._

class ReindexSpec
    extends MediaAtomSuite[ReindexController]
    with MustMatchers
    with MockitoSugar {

  val testAtoms = Map("1" -> testAtom, "2" -> testAtom)

  override def initialDataStore = new MemoryStore(testAtoms)

  "reindexer" should {
    "return error if publisher fails" in atomTest { implicit f =>
      when(reindexPublisher.reindexAtoms(any())).thenReturn(Failure(new Exception("forced failure")))
      val res = call(reindexCtrl.reindexLive(None, None), FakeRequest())
      status(res) mustEqual INTERNAL_SERVER_ERROR
      contentAsJson(res) mustEqual JsObject("error" -> JsString("forced failure") :: Nil)
    }

    "call publisher with atoms in dataStore" in atomTest { implicit fix =>
      when(reindexPublisher.reindexAtoms(any()))
        .thenReturn(Success(testAtoms.values.size.toLong))
      val res = call(reindexCtrl.reindexLive(None, None), FakeRequest())
      status(res) mustEqual OK
      val cap = ArgumentCaptor.forClass(classOf[TraversableOnce[Atom]])
      verify(reindexPublisher).reindexAtoms(cap.capture())
      cap.getValue().toList must contain theSameElementsAs(testAtoms.values)
    }
  }
}
