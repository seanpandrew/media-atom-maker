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
//    with OneAppPerTest
    with GuiceableModuleConversions {

  type FixtureParam = F

  def defaultOverrides: Seq[GuiceableModule] =
    Seq(bind[AuthActions] to classOf[TestPandaAuth])

  trait FixtureData {
    /* Read a value from the app's injector.
     *
     * NOTE that because of the way injection works, unless the class A
     * is marked as singleton, then each time you call this you will
     * generate a new instance of A */
    def iget[A : ClassTag]: A = app.injector.instanceOf[A]
    def overrides: Seq[GuiceableModule] = defaultOverrides
    lazy val guice = new GuiceApplicationBuilder().overrides(overrides: _*)
    implicit lazy val app = guice.build()
    val param: FixtureParam
  }

  def atomTest(customOverrides: GuiceableModule*)(block: FixtureData => Unit) = {
    val data = new FixtureData {
      override def overrides = super.overrides ++ customOverrides
      val param = app.injector.instanceOf[FixtureParam]
    }
    try {
      block(data)
    } finally {
      // shutdown Panda Auth agents
      data.iget[AuthActions].shutdown
    }
  }

  def atomTest(ds: DataStore)(block: FixtureData => Unit): Unit =
    atomTest(bind[DataStore] toInstance ds)(block)

  /**
    * This trait provides one app, and one Guice module per test. The
    * Guice module will be built from the components returned by the
    * default* methods, which can be overridden to customise in your
    * suite.
    */

  /*
   * Create a new application instance: overridden from OneAppPerTest
   * trait and will be called by code in that trait before each test.
   */
  //override def newAppForTest(testData: ScalaTestTestData): Application = newGuiceForTest.build()

  /**
    * override this to customise; e.g. to add to the existing bindings:
    * 
    *    override def newGuiceForTest = super.newGuiceForTest.bindings(...)
    */
  def newGuiceForTest = new GuiceApplicationBuilder()
    .overrides(bind[AuthActions] to classOf[TestPandaAuth])

  // /**
  //   * Cannot find a way to use `withFixture` and `OneArgTest` here,
  //   * because of an ordering issue: in order to create the
  //   * application, which is a requirement for injecting the fixture,
  //   * we need to call `super.withFixture()`, but that will trigger
  //   * running the test, meaning that by then it is too late.
  //   */

  // override def withFixture(test: OneArgTest) = try {
  //   super.withFixture(
  //     new NoArgTest {
  //       def apply() = test(iget[FixtureParam])
  //       val configMap = test.configMap
  //       val name: String = test.name
  //       val scopes = test.scopes
  //       val tags = test.tags
  //       val text = test.text
  //     }
  //   )
  // } finally {
  //   iget[AuthActions].shutdown
  // }

  implicit def app(implicit fix: FixtureData) = fix.app
  implicit def mat(implicit fix: FixtureData) = app.materializer

  val oneHour = 3600000L

}
