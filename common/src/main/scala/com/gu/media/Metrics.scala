package com.gu.media

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime, ZoneOffset, ZonedDateTime}

import play.api.libs.json.{JsArray, JsObject, JsValue}

case class OldRouteMetrics(videos: Int) // TODO: incorporate Pluto data for publish time
case class NewRouteMetrics(videos: Int,  minPublishTime: Long, maxPublishTime: Long, avgPublishTime: Long)
case class DayMetrics(day: String, oldRoute: OldRouteMetrics, newRoute: NewRouteMetrics)

class Metrics(capi: CapiPreviewAccess) {
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
    val (min, max, avg) = if(metrics.newPublishTimes.isEmpty) {
      (0L, 0L, 0L)
    } else {
      val publishTimes = metrics.newPublishTimes.sorted
      (publishTimes.head, publishTimes.last, publishTimes.sum / publishTimes.length)
    }

    DayMetrics(day, OldRouteMetrics(metrics.oldVideos), NewRouteMetrics(metrics.newVideos, min, max, avg))
  }
}
