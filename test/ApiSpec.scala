package test

import cats.data.Xor
import data.{ AtomPublisher, DataStore, VersionConflictError }
import org.scalatest.OptionValues
import org.mockito.Mockito._
import org.mockito.Matchers._

import util.atom.MediaAtomImplicits

import play.api.libs.json._
import controllers.Api
import play.api.test._
import play.api.http.HttpVerbs
import play.api.test.Helpers._
import data.MemoryStore

import org.scalatest.{ AppendedClues, MustMatchers }
import scala.util.{ Success, Failure }

import play.api.inject.bind

//import java.util.Date
//import play.api.inject.guice.GuiceableModule

import test.TestData._

class ApiSpec
    extends MediaAtomSuite[Api]
    with OptionValues
    with MustMatchers
    with AppendedClues
    with HttpVerbs
    with MediaAtomImplicits {

  val youtubeId  =  "7H9Z4sn8csA"
  val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

  "api" should {
    "return a media atom" in atomTest { implicit f =>
      val result = f.api.getMediaAtom("1").apply(requestWithCookies)
      status(result) mustEqual OK
      val json = contentAsJson(result)
                              (json \ "id").as[String] mustEqual "1"
        (json \ "data" \ "assets").as[List[JsValue]] must have size 2
    }
    "return NotFound for missing atom" in atomTest { implicit f =>
      val result = f.api.getMediaAtom("xyzzy").apply(requestWithCookies)
      status(result) mustEqual NOT_FOUND
    }
    "return not found when adding asset to a non-existant atom" in atomTest { implicit f =>
      val req = requestWithCookies.withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "3")
      val result = call(f.api.addAsset("xyzzy"), req)
      status(result) mustEqual NOT_FOUND
    }
    "complain when catching simultaenous update from datastore" in atomTest.withDataStore(mock[DataStore]) { implicit f =>
      val ds = f.dataStore
      when(ds.getMediaAtom(any())).thenReturn(Some(testAtom))
      when(ds.updateMediaAtom(any())).thenReturn(Xor.Left(VersionConflictError(1)))
      val req = requestWithCookies
        .withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
      val result = call(f.api.addAsset("1"), req)
      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(ds).updateMediaAtom(any())
    }
    "add an asset to an atom" in atomTest { implicit f =>
      val req = requestWithCookies.withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
      val result = call(api.addAsset("1"), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED }
      dataStore.getMediaAtom("1").value.tdata.assets must have size 3
    }
    "create an atom" in atomTest { implicit f =>
      val req = requestWithCookies.withFormUrlEncodedBody("id" -> "2")
      val result = call(api.createMediaAtom(), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED  }
      val createdAtom = dataStore.getMediaAtom("2").value
      createdAtom.id mustEqual "2"
    }
    "call out to publisher to publish an atom" in atomTest { implicit f =>
      val result = call(api.publishAtom("1"), requestWithCookies)
      status(result) mustEqual NO_CONTENT
      verify(publisher).publishAtomEvent(any())
    }
    "call report failure if publisher fails" in atomTest.withPublisher(failingMockPublisher) { implicit f =>
      val result = call(api.publishAtom("1"), requestWithCookies)
      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(publisher).publishAtomEvent(any())
    }
    "should list atoms" in atomTest { implicit f =>
      dataStore.createMediaAtom(testAtom.copy(id = "2"))
      val result = call(api.listAtoms(), requestWithCookies)
      status(result) mustEqual OK
      contentAsJson(result).as[List[JsValue]] must have size 2
    }
    "should change version of atom" in atomTest { implicit f =>
      // before...
      dataStore.getMediaAtom("1").value.tdata.activeVersion mustEqual 2L
      val result = call(api.revertAtom("1", 1L), requestWithCookies)
      status(result) mustEqual OK
      // after ...
      dataStore.getMediaAtom("1").value.tdata.activeVersion mustEqual 1L
    }
    "should complain if revert to version without asset" in atomTest { implicit f =>
      // before...
      val result = call(api.revertAtom("1", 10L), requestWithCookies)
      status(result) mustEqual INTERNAL_SERVER_ERROR
    }
  }
}
