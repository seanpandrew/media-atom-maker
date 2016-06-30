package data

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.gu.contentatom.thrift.{ Atom, ContentAtomEvent }

import com.amazonaws.services.kinesis.AmazonKinesisClient
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import scala.util.{ Success, Failure, Try }

class KinesisAtomPublisher (val streamName: String, val kinesis: AmazonKinesisClient)
    extends AtomPublisher
    with ThriftSerializer[ContentAtomEvent]
    with com.typesafe.scalalogging.LazyLogging {

  logger.info(s"KinsisAtomPublisher started with streamName $streamName")

  @Inject() def this(awsConfig: util.AWSConfig) = this(awsConfig.kinesisStreamName, awsConfig.kinesisClient)

  def makeParititionKey(event: ContentAtomEvent): String = event.atom.atomType.name

  def publishAtomEvent(event: ContentAtomEvent): Try[Unit] = Try {
      val data = serializeEvent(event)
      kinesis.putRecord(streamName, data, makeParititionKey(event))
    }
}

class KinesisAtomReindexer (streamName: String, kinesis: AmazonKinesisClient)
    extends KinesisAtomPublisher(streamName, kinesis)
    with AtomReindexer {
  @Inject() def this(awsConfig: util.AWSConfig) =
    this(awsConfig.kinesisReindexStreamName, awsConfig.kinesisClient)

  def reindexAtoms(atoms: TraversableOnce[Atom]) = Failure(new Exception())

}
