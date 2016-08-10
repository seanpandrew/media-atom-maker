package com.gu.atom.data

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.MediaAtom
import org.scalatest.{ fixture, Matchers, BeforeAndAfterAll, OptionValues }

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._
import com.amazonaws.services.dynamodbv2.model._
import com.gu.scanamo.DynamoFormat._
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import ScanamoUtil._

import com.gu.atom.util.AtomImplicitsGeneral

import cats.data.Xor

import com.gu.atom.TestData._

class DynamoDataStoreSpec
    extends fixture.FunSpec
    with Matchers
    with OptionValues
    with BeforeAndAfterAll
    with AtomImplicitsGeneral
    with org.scalatest.mock.MockitoSugar {
  val tableName = "atom-test-table"

  val localDB = new LocalDynamoDB()

  type FixtureParam = DynamoDataStore[MediaAtom]

  def withFixture(test: OneArgTest) = {
    val client = localDB.dbClient
    val db = new DynamoDataStore[MediaAtom](client, tableName) with MediaAtomDynamoFormats
    try {
      super.withFixture(test.toNoArgTest(db))
    } finally {
      //println(s"[PMR] 1541 shutting down client")
      //client.shutdown
    }
  }

  describe("DynamoDataStore") {
    it("should create a new atom") { dataStore =>
      //dataStore.createAtom(testAtom)// should equal(Xor.Right())
    }

    it("should return the atom") { dataStore =>
      //dataStore.getAtom(testAtom.id) //.value should equal(testAtom)
    }

    it("should update the atom") { dataStore =>
      // val updated = testAtom
      //   .copy(defaultHtml = "<div>updated</div>")
      //   .bumpRevision

      // dataStore.updateAtom(updated)// should equal(Xor.Right())
      //   dataStore.getAtom(testAtom.id)//.value should equal(updated)
    }
  }

  override def beforeAll() = localDB.createTable(localDB.dbClient)(tableName)('id -> S)
}
