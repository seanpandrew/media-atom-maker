package test

import com.gu.contentatom.thrift.Atom
import data._
import org.mockito.ArgumentCaptor
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any
import play.api.test._
import play.api.inject.bind
import play.api.test.Helpers._
import scala.util.{ Failure, Success }
import play.api.libs.json._

import TestData._

class ReindexSpec
    extends MediaAtomSuite
    with MustMatchers
    with MockitoSugar {

  val testAtoms = Map("1" -> testAtom, "2" -> testAtom)
  def initialDataStore = new MemoryStore(testAtoms)

  override def newGuiceForTest =
    super.newGuiceForTest overrides (
      bind[AtomReindexer] toInstance mock[AtomReindexer],
      bind[DataStore] toInstance initialDataStore
    )

  // def withReindexer(publisher: AtomReindexer = defaultReindexer)(f: ReindexController => Unit) =
  //   f(guicer
  //       .overrides(bind(classOf[AtomReindexer])
  //                    .toInstance(publisher))
  //       .injector
  //       .instanceOf(classOf[controllers.ReindexController]))

  // def reindexTest(
  //   reindexer: AtomReindexer = mock[AtomReindexer],
  //   dataStore: DataStore = initialDataStore
  // ) = injectedTest(
  //   bind[AtomReindexer] toInstance reindexer,
  //   bind[DataStore] toInstance dataStore
  // ) _

  "reindexer" should {
    "return error if publisher fails" in {
      when(reindexPublisher.reindexAtoms(any())).thenReturn(Failure(new Exception("forced failure")))
      val res = call(reindexController.reindexLive(None, None), FakeRequest())
      status(res) mustEqual INTERNAL_SERVER_ERROR
      contentAsJson(res) mustEqual JsObject("error" -> JsString("forced failure") :: Nil)
    }

    "call publisher with atoms in dataStore" in {
      when(reindexPublisher.reindexAtoms(any()))
        .thenReturn(Success(testAtoms.values.size.toLong))
      val res = call(reindexController.reindexLive(None, None), FakeRequest())
      status(res) mustEqual OK
      val cap = ArgumentCaptor.forClass(classOf[TraversableOnce[Atom]])
      verify(reindexPublisher).reindexAtoms(cap.capture())
      cap.getValue().toList must contain theSameElementsAs(testAtoms.values)
    }
  }

    //     testAtoms.values foreach { atom =>
    //       val req = FakeRequest()
    //       val res = call(reindexer.reindexLive(None, None), req)
    //       status(res) mustEqual OK
    //       verify(reindexKinesisMock).reindexAtoms(any())
    //     }
    //   }
    // }
}
