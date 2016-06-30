package test

import data._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any

import play.api.inject.bind

import TestData._

class ReindexSpec extends MediaAtomSuite
    with MockitoSugar {

  val testAtoms = Map("1" -> testAtom, "2" -> testAtom)
  def initialDataStore = new MemoryStore(testAtoms)

  val reindexKinesisMock = mock[AtomPublisher]
  val reindexPublisher =
    guicer
      .overrides(bind(classOf[AtomPublisher])
                   .qualifiedWith("reindex")
                   .to(reindexKinesisMock))
      .injector
      .instanceOf(classOf[controllers.ReindexController])

  "reindexer" should {
    "send a publish message for all atoms in the data store" in {

      testAtoms.values foreach { atom =>
        verify(reindexKinesisMock).publishAtomEvent(any())
      }
    }
  }
}
