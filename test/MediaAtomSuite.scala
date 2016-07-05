package test

import data.AtomPublisher
import org.scalatest.mock.MockitoSugar
import controllers.ReindexController
import org.scalatest.WordSpec
import play.api.inject.Binding
import play.api.test.FakeRequest
import play.api.inject.guice.GuiceApplicationBuilder
import com.gu.pandomainauth.action.AuthActions
import data._
import controllers.Api
import play.api.inject.bind
import play.api.inject.guice.{ GuiceableModule, GuiceableModuleConversions }
import scala.reflect.ClassTag

import TestData._

class MediaAtomSuite[F : ClassTag] extends WordSpec
    with GuiceableModuleConversions {

  class AtomTest(conf: AtomTestConfig) {
    def withBoundInstance[A : ClassTag](ins: A) =
      new AtomTest(conf.copy(overrides = conf.overrides :+ (bind[A] toInstance ins)))

    def withDataStore(ds: DataStore) = withBoundInstance(ds)
    def withPublisher(pub: AtomPublisher) = withBoundInstance(pub)

    def apply(block: AtomTestConfig => Unit) =
      try { block(conf) } finally {
        conf.authActions.shutdown
        conf.authActions.wsClient.close // probably panda auth should be shutting this down, but until then...
      }
  }

  def atomTest = new AtomTest(new AtomTestConfig)

  case class AtomTestConfig(overrides: Seq[Binding[_]] = Nil) {

    // needs to be lazy because it depends on other vals
    lazy val guice = new GuiceApplicationBuilder()
      .overrides(overrides)

    lazy val app = guice.build()

    def iget[A : ClassTag] = app.injector.instanceOf[A]

    lazy val api              = iget[Api]
    lazy val reindexer        = iget[AtomReindexer]
    lazy val dataStore        = iget[DataStore]
    lazy val publisher        = iget[AtomPublisher]
    lazy val reindexCtrl      = iget[ReindexController]
    lazy val authActions      = iget[AuthActions]
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
