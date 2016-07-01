package test

import com.google.inject.AbstractModule
import controllers.ReindexController
import javax.inject.Provider
import org.scalatest.{ TestData => ScalaTestTestData }
import org.scalatest.fixture
import play.api.{ Application, Configuration }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import org.scalatestplus.play.{ PlaySpec, OneAppPerTest }

import com.gu.pandomainauth.action.AuthActions

import data._

import controllers.Api

import play.api.inject.bind
import play.api.inject.Injector

import play.api.inject.guice.{ GuiceableModule, GuiceableModuleConversions }
import scala.reflect.ClassTag

class MediaAtomSuite[F : ClassTag] extends fixture.WordSpec
    with OneAppPerTest
    with GuiceableModuleConversions {

  type FixtureParam = F

  /**
    * This trait provides one app, and one Guice module per test. The
    * Guice module will be built from the components returned by the
    * default* methods, which can be overridden to customise in your
    * suite.
    */

  private var guicer: GuiceApplicationBuilder = _

  /**
    * make the things available implicitly to the tests
    */
  implicit def injector: Injector = guicer.injector()

  // overridden from OneAppPerTest trait
  override def newAppForTest(testData: ScalaTestTestData): Application = guicer.build()

  /**
    * override this to customise; e.g. to add to the existing bindings:
    * 
    *    override def newGuiceForTest = super.newGuiceForTest.bindings(...)
    */
  def newGuiceForTest = {
    println("PMR 1605 newGuiceForTest")
    new GuiceApplicationBuilder()
      .overrides(bind[AuthActions] to classOf[TestPandaAuth])
  }

 override def withFixture(test: OneArgTest) = {
    synchronized {
      guicer = newGuiceForTest
    }
   super.withFixture(test.toNoArgTest(iget[FixtureParam]))
  }

  implicit def mat = app.materializer

  def iget[A : ClassTag]: A = injector.instanceOf[A]

  /**
    * some shortcut messages for getting specific items from the
    * injector; these will probably trigger creation of a new instance
    * each time they are called (unless they are annotated
    * as @Singleton or similar) so use sparingly
    */
  def reindexController = iget[ReindexController]
  def reindexPublisher  = iget[AtomReindexer]
  def apiController     = iget[Api]
  def dataStore         = iget[DataStore]

  val oneHour = 3600000L
  def getApi(dataStore: DataStore, publisher: AtomPublisher) = {
    guicer
      .overrides(bind(classOf[DataStore]).toInstance(dataStore))
      .overrides(bind(classOf[AtomPublisher]).toInstance(publisher))
      .injector
      .instanceOf(classOf[Api])
  }
}
