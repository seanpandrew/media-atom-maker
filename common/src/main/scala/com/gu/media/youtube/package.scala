package com.gu.media

import java.net.URI

import com.google.api.services.youtube.model.{Channel, VideoCategory}
import com.gu.media.logging.Logging
import com.typesafe.config.Config
import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json._

package object youtube {
  class YouTube(override val config: Config) extends Logging with YouTubeAccess with YouTubeVideos

  case class YouTubeVideoCategory(id: Int, title: String)
  case class YouTubeChannel(title: String, logo: URI, id: String, defaultDescription: String)

  case class YouTubeMetadataUpdate(title: Option[String], categoryId: Option[String], description: Option[String],
                                   tags: List[String], license: Option[String], privacyStatus: Option[String])

  // failure only set is status is "failed"
  case class YouTubeProcessingStatus(id: String, status: String, total: Long, processed: Long,
                                     timeLeftMs: Long, failure: Option[String])

  implicit val format: Format[YouTubeProcessingStatus] = Jsonx.formatCaseClass[YouTubeProcessingStatus]

  object YouTubeVideoCategory {
    implicit val reads: Reads[YouTubeVideoCategory] = Json.reads[YouTubeVideoCategory]
    implicit val writes: Writes[YouTubeVideoCategory] = Json.writes[YouTubeVideoCategory]

    def build(category: VideoCategory): YouTubeVideoCategory = {
      YouTubeVideoCategory(category.getId.toInt, category.getSnippet.getTitle)
    }
  }

  object YouTubeChannel {
    implicit val reads: Reads[YouTubeChannel] = (
      (__ \ "title").read[String] and
        (__ \ "logo").read[String].map(URI.create) and
        (__ \ "id").read[String] and
        (__ \ "defaultDescription").read[String]
      )(YouTubeChannel.apply _)

    implicit val writes: Writes[YouTubeChannel] = (
      (__ \ "title").write[String] and
        (__ \ "logo").write[String].contramap((_: URI).toString) and
        (__ \ "id").write[String] and
        (__ \ "defaultDescription").write[String]
      )(unlift(YouTubeChannel.unapply))

    def build(channel: Channel): YouTubeChannel = {

      YouTubeChannel(
        title = channel.getSnippet.getTitle,
        logo = URI.create(channel.getSnippet.getThumbnails.getDefault.getUrl),
        id = channel.getId,
        defaultDescription = channel.getBrandingSettings.getChannel.getDescription
      )
    }
  }

  object YouTubeMetadataUpdate {
    def prettyToString(metadata: YouTubeMetadataUpdate): String = {
      Map(
        "title" -> metadata.title,
        "description" -> metadata.description,
        "tags" -> metadata.tags,
        "categoryId" -> metadata.categoryId,
        "license" -> metadata.license,
        "privacyStatus" -> metadata.privacyStatus.map(_.toString)
      ).collect {
        case (key, Some(value)) =>
          s"\t$key=$value"
      }.mkString("\n")
    }
  }
}
