package com.gu.atom.data

import scala.collection.convert.decorateAsJava._

import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model._

/*
 * copied from:
 *    https://github.com/guardian/scanamo/blob/master/src/test/scala/com/gu/scanamo/LocalDynamoDB.scala
 */

class LocalDynamoDB {
  def dbClient = {
    val client = new AmazonDynamoDBAsyncClient(new com.amazonaws.auth.BasicAWSCredentials("key", "secret"))
    client.setEndpoint("http://localhost:8000")
    client
  }

  def createTable(client: AmazonDynamoDB)(tableName: String)(attributes: (Symbol, ScalarAttributeType)*) = {
    client.createTable(
      attributeDefinitions(attributes),
      tableName,
      keySchema(attributes),
      arbitraryThroughputThatIsIgnoredByDynamoDBLocal
    )
  }

  private def keySchema(attributes: Seq[(Symbol, ScalarAttributeType)]) = {
    val hashKeyWithType :: rangeKeyWithType = attributes.toList
    val keySchemas = hashKeyWithType._1 -> KeyType.HASH :: rangeKeyWithType.map(_._1 -> KeyType.RANGE)
    keySchemas.map{ case (symbol, keyType) => new KeySchemaElement(symbol.name, keyType)}.asJava
  }

  private def attributeDefinitions(attributes: Seq[(Symbol, ScalarAttributeType)]) = {
    attributes.map{ case (symbol, attributeType) => new AttributeDefinition(symbol.name, attributeType)}.asJava
  }

  private val arbitraryThroughputThatIsIgnoredByDynamoDBLocal = new ProvisionedThroughput(1L, 1L)
}
