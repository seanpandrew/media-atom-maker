package test

import data.AtomPublisher
import org.scalatest.mock.MockitoSugar
import com.google.inject.AbstractModule
import controllers.ReindexController
import javax.inject.Provider
import org.scalatest.{ TestData => ScalaTestTestData, WordSpec }
import play.api.test.FakeRequest
import play.api.{ Application, Configuration }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import scala.util.{ Success, Failure }

import org.scalatestplus.play.{ PlaySpec, OneAppPerTest }

import com.gu.pandomainauth.action.AuthActions

import data._

import controllers.Api

import play.api.inject.bind
import play.api.inject.Injector

import play.api.inject.guice.{ GuiceableModule, GuiceableModuleConversions }
import scala.reflect.ClassTag

import org.mockito.Mockito._
import org.mockito.Matchers._

import TestData._

class MediaAtomSuite[F : ClassTag] extends WordSpec
    with MockitoSugar
    with GuiceableModuleConversions {

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

  class AtomTest(conf: AtomTestConfig) {
    def withDataStore(ds: DataStore) = new AtomTest(conf.copy(dataStore = ds))
    def withPublisher(pub: AtomPublisher) = new AtomTest(conf.copy(publisher = pub))
    def apply(block: AtomTestConfig => Unit) =
      try { block(conf) } finally {
        conf.authActions.shutdown
        conf.authActions.wsClient.close // probably panda auth should be shutting this down, but until then...
      }
  }

  def atomTest = new AtomTest(new AtomTestConfig)

  private def ibind[A : ClassTag](a: A) = bind[A] toInstance a

  case class AtomTestConfig(
    dataStore: DataStore = initialDataStore,
    publisher: AtomPublisher = defaultPublisher,
    reindexer: AtomReindexer = defaultReindexer
  ) {
    // needs to be lazy because it depends on other vals
    lazy val guice = new GuiceApplicationBuilder()
      .overrides(ibind(dataStore),
                 ibind(publisher),
                 ibind(defaultReindexer),
                 bind[AuthActions] to classOf[TestPandaAuth])
    lazy val app = guice.build()

    def iget[A : ClassTag] = app.injector.instanceOf[A]

    lazy val api = iget[Api]
    lazy val reindexCtrl = iget[ReindexController]
    lazy val authActions = iget[AuthActions]
    lazy val reindexPublisher = iget[AtomReindexer]
  }

  /* some shortcuts to commonly accessed members of AtomTestConfig */
  def api(implicit conf: AtomTestConfig) = conf.api
  def reindexer(implicit conf: AtomTestConfig) = conf.reindexer
  def dataStore(implicit conf: AtomTestConfig) = conf.dataStore
  def publisher(implicit conf: AtomTestConfig) = conf.publisher
  def reindexPublisher(implicit conf: AtomTestConfig) = conf.reindexPublisher
  def reindexCtrl(implicit conf: AtomTestConfig) = conf.reindexCtrl

  def requestWithCookies(implicit conf: AtomTestConfig) =
    FakeRequest().withCookies(conf.authActions.generateCookies(testUser): _*)

  implicit def app(implicit conf: AtomTestConfig) = conf.app
  implicit def mat(implicit conf: AtomTestConfig) = app.materializer

}
