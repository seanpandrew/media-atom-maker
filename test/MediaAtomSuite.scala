package test

import com.google.inject.AbstractModule
import controllers.ReindexController
import javax.inject.Provider
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import org.scalatestplus.play.{ PlaySpec, OneAppPerSuite }

import com.gu.pandomainauth.action.AuthActions

import data._

import controllers.Api

import play.api.inject.bind
import play.api.inject.Injector

import play.api.inject.guice.GuiceableModule
import scala.reflect.classTag

trait MediaAtomSuite extends PlaySpec with OneAppPerSuite {

  val defaultGuicer = new GuiceApplicationBuilder()
    .overrides(bind(classOf[AuthActions]).to(classOf[TestPandaAuth]))
    .overrides(bind(classOf[DataStore]).to(classOf[MemoryStore]))

  // override to provide Suite-wide default bindings
  def guicer = defaultGuicer

  override lazy val app = guicer.build
  implicit val mat = app.materializer

  def injectedTest(customBindings: GuiceableModule*)(block: Injector => Unit) = {
    block(
      guicer.overrides(customBindings: _*).injector
    )
  }

  def iget[A : ClassTag](implicit inj: Injector): A = inj.instanceOf[A]

  /**
    * some shortcut messages for getting specific items from the injector
    */
  def reindexController(implicit inj: Injector) = iget[ReindexController]
  def reindexPublisher(implicit inj: Injector)  = iget[AtomReindexer]
  def apiController(implicit inj: Injector)     = iget[Api]
  def dataStore(implicit inj: Injector)         = iget[DataStore]

  val oneHour = 3600000L
  def getApi(dataStore: DataStore, publisher: AtomPublisher) = {
    guicer
      .overrides(bind(classOf[DataStore]).toInstance(dataStore))
      .overrides(bind(classOf[AtomPublisher]).toInstance(publisher))
      .injector
      .instanceOf(classOf[Api])
  }
}
