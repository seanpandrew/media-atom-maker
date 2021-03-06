package model.commands

import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.model.{Asset, MediaAtom}
import com.gu.media.youtube.YouTubeVideos
import data.DataStores
import com.gu.media.model.Platform.Youtube

case class DeleteCommand(id: String, override val stores: DataStores, youTube: YouTubeVideos)
  extends Command with AtomAPIActions with Logging {

  type T = Unit

  override def process(): Unit = {
    val atom = getPreviewAtom(id)
    val mediaAtom = MediaAtom.fromThrift(atom)

    makeYouTubeVideosPrivate(mediaAtom.assets)

    val event = ContentAtomEvent(atom, EventType.Takedown, new Date().getTime)
    livePublisher.publishAtomEvent(event)
    previewPublisher.publishAtomEvent(event)

    deletePreviewAtom(id)
    deletePublishedAtom(id)
  }

  private def makeYouTubeVideosPrivate(assets: List[Asset]): Unit = assets.collect {
    case Asset(_, _, videoId, Youtube, _) if youTube.isManagedVideo(videoId) =>
      log.info(s"Marking $videoId as private as parent atom $id is being deleted")
      youTube.setStatus(videoId, PrivacyStatus.Private)
  }
}
