package com.gu.media

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime, ZoneOffset, ZonedDateTime}

import akka.actor.{Actor, ActorSystem, Cancellable, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.gu.media.logging.Logging
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, JsArray, JsObject, JsValue}

import scala.concurrent.Future
import scala.concurrent.duration._

case class OldRouteMetrics(videos: Int) // TODO: incorporate Pluto data for publish time
object OldRouteMetrics {
  implicit val format: Format[OldRouteMetrics] = Jsonx.formatCaseClass[OldRouteMetrics]
}

case class NewRouteMetrics(videos: Int,  minPublishTime: Long, maxPublishTime: Long, medianPublishTime: Long)
object NewRouteMetrics {
  implicit val format: Format[NewRouteMetrics] = Jsonx.formatCaseClass[NewRouteMetrics]
}

case class DayMetrics(day: String, oldRoute: OldRouteMetrics, newRoute: NewRouteMetrics)
object DayMetrics {
  implicit val format: Format[DayMetrics] = Jsonx.formatCaseClass[DayMetrics]
}

case class Metrics(dayMetrics: List[DayMetrics])
object Metrics {
  implicit val format: Format[Metrics] = Jsonx.formatCaseClass[Metrics]
}

trait MetricsAccess {
  def getMetrics(forceRefresh: Boolean)(implicit timeout: Timeout): Future[Metrics]
}

object MetricsAccess {
  def apply(system: ActorSystem, capi: CapiPreviewAccess, refreshRate: FiniteDuration): MetricsAccess = {
    val actor = system.actorOf(Props(new MetricsActor(capi, refreshRate)))

    new MetricsAccess {
      override def getMetrics(forceRefresh: Boolean)(implicit timeout: Timeout): Future[Metrics] = {
        if(forceRefresh) {
          actor.ask('force).mapTo[Metrics]
        } else {
          actor.ask('get).mapTo[Metrics]
        }
      }
    }
  }
}

class MetricsActor(capi: CapiPreviewAccess, refreshRate: FiniteDuration) extends Actor with Logging {
  import context.dispatcher

  private val capiMetrics = new CapiBackedMetrics(capi)
  private var timer: Option[Cancellable] = None
  private var metrics = Metrics(List.empty)

  override def preStart(): Unit = {
    timer = Some(context.system.scheduler.schedule(0.seconds, refreshRate, self, 'refresh))
  }

  override def postStop(): Unit = {
    timer.foreach(_.cancel())
  }

  override def receive: Actor.Receive = {
    case 'refresh =>
      refresh()

    case 'force =>
      refresh()
      sender() ! metrics

    case 'get =>
      sender() ! metrics
  }

  private def refresh() = {
    log.info("Refreshing metrics from CAPI...")
    metrics = Metrics(capiMetrics.getDayMetrics(14))
    log.info("Metrics refresh complete")
  }
}

class CapiBackedMetrics(capi: CapiPreviewAccess) {
  private case class AggregateMetrics(oldVideos: Int, newVideos: Int, newPublishTimes: List[Long])
  private val emptyAggregate = AggregateMetrics(0, 0, List.empty)

  def getDayMetrics(numberOfDays: Int): List[DayMetrics] = {
    val now = ZonedDateTime.now(ZoneOffset.UTC)
    val days = (0 until numberOfDays).map { n => now.minusDays(n).format(DateTimeFormatter.ISO_LOCAL_DATE) }.toList

    days.map { day =>
      val ids = getContentIdsForDay(day)
      val aggregate = ids.foldLeft(emptyAggregate) { (before, id) => handleVideo(id, before) }

      present(day, aggregate)
    }
  }

  private def getContentIdsForDay(day: String): Set[String] = {
    // ASSUMPTION: we don't publish more than 200 videos per day
    val url = s"search?type=video&from-date=$day&to-date=$day&page-size=200"
    val response = (capi.capiQuery(url) \ "response").get
    val results = (response \ "results").as[JsArray].value

    results.flatMap { result =>
      (result \ "id").asOpt[String]
    }.toSet
  }

  private def handleVideo(id: String, metrics: AggregateMetrics): AggregateMetrics = {
    val url = s"$id?show-fields=all&show-atoms=all"
    val response = (capi.capiQuery(url) \ "response" \ "content").get

    val fields = (response \ "fields").as[JsObject]
    val atom = (response \ "atoms" \ "media").asOpt[JsArray].flatMap(_.value.headOption)

    atom match {
      case Some(atom) =>
        publishTime(fields, atom) match {
          case Some(time) =>
            metrics.copy(
              newVideos = metrics.newVideos + 1,
              newPublishTimes = metrics.newPublishTimes :+ time
            )

          case None =>
            metrics.copy(newVideos = metrics.newVideos + 1)
        }

      case None =>
        metrics.copy(oldVideos = metrics.oldVideos + 1)
    }
  }

  private def publishTime(fields: JsObject, atom: JsValue): Option[Long] = {
    for {
      creationTime <- (atom \ "contentChangeDetails" \ "created" \ "date").asOpt[Long]
      publicationTimeStr <- (fields \ "firstPublicationDate").asOpt[String]

      publicationTime = OffsetDateTime.parse(publicationTimeStr).toInstant.toEpochMilli
    } yield publicationTime - creationTime
  }

  private def present(day: String, metrics: AggregateMetrics): DayMetrics = {
    val (min, max, median) = if(metrics.newPublishTimes.isEmpty) {
      (0L, 0L, 0L)
    } else {
      val publishTimes = metrics.newPublishTimes.sorted
      val median = metrics.newPublishTimes(metrics.newPublishTimes.length / 2)

      (publishTimes.head, publishTimes.last, median)
    }

    DayMetrics(day, OldRouteMetrics(metrics.oldVideos), NewRouteMetrics(metrics.newVideos, min, max, median))
  }
}
