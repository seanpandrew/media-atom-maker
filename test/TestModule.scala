package test

import TestData._
import data._
import org.scalatest.mock.MockitoSugar
import scala.util.{ Success, Failure }

import org.mockito.Mockito._
import org.mockito.Matchers._

import com.google.inject.AbstractModule

class TestModule extends AbstractModule with MockitoSugar {

  def initialDataStore = new MemoryStore(Map("1" -> testAtom))

  def defaultPublisher: AtomPublisher = {
    val p = mock[AtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Success(()))
    p
  }

  def failingMockPublisher: AtomPublisher = {
    val p = mock[AtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Failure(new Exception("failure")))
    p
  }

  def defaultReindexer: AtomReindexer = mock[AtomReindexer]

  private def ibind[A : ClassTag](a: A) = bind[A] toInstance a

  def configure = {
    // ibind(dataStore),
//                  ibind(publisher),
//                  ibind(defaultReindexer),
//                  bind[AuthActions] to classOf[TestPandaAuth])


  }

}
