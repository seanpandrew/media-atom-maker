package test

import com.google.inject.AbstractModule
import controllers.ReindexController
import javax.inject.Provider
import org.scalatest.{ TestData => ScalaTestTestData, WordSpec }
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

class MediaAtomSuite[F : ClassTag] extends WordSpec
    with OneAppPerTest
    with GuiceableModuleConversions {

  type FixtureParam = F

  /**
    * This trait provides one app, and one Guice module per test. The
    * Guice module will be built from the components returned by the
    * default* methods, which can be overridden to customise in your
    * suite.
    */

  /**
    * make the things available implicitly to the tests
    */
  //implicit def injector: Injector = guicer.injector()

  // overridden from OneAppPerTest trait
  override def newAppForTest(testData: ScalaTestTestData): Application = newGuiceForTest.build()

  /**
    * override this to customise; e.g. to add to the existing bindings:
    * 
    *    override def newGuiceForTest = super.newGuiceForTest.bindings(...)
    */
  def newGuiceForTest = new GuiceApplicationBuilder()
    .overrides(bind[AuthActions] to classOf[TestPandaAuth])

  /**
    * XXX - Cannot find a way to use `withFixture` and `OneArgTest`
    * here, because of an ordering issue: in order to create the
    * application, which is a requirement for injecting the fixture,
    * we need to call `super.withFixture()`, but that will trigger
    * running the test, meaning that by then it is too late.
    */

  override def withFixture(test: NoArgTest) = {
    try {
      super.withFixture(test)
    } finally {
      iget[AuthActions].shutdown
    }
  }

  implicit def mat = app.materializer

  def iget[A : ClassTag]: A = app.injector.instanceOf[A]

  /**
    * some shortcut messages for getting specific items from the
    * injector; these will probably trigger creation of a new instance
    * each time they are called (unless they are annotated
    * as @Singleton or similar) so use sparingly
    */
  def reindexer        = iget[ReindexController]
  def reindexPublisher = iget[AtomReindexer]
  def api              = iget[Api]
  def dataStore        = iget[DataStore]

  val oneHour = 3600000L

}
