package model.commands

import java.util.Date

import play.api.Logger
import com.gu.atom.data.PreviewDataStore
import CommandExceptions._
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.contentatom.thrift.atom.media.{Asset, Platform}
import model.MediaAtom
import util.{ThriftUtil, YouTubeConfig, YouTubeVideoInfoApi}
import util.atom.MediaAtomImplicits

import scala.util.{Failure, Success}

case class AddAssetCommand(atomId: String,
                           videoUri: String,
                           version: Option[Long],
                           mimeType: Option[String])
                          (implicit previewDataStore: PreviewDataStore,
                           previewPublisher: PreviewAtomPublisher,
                           val youtubeConfig: YouTubeConfig)
    extends Command
    with MediaAtomImplicits {

  type T = MediaAtom

  def process(): MediaAtom = {

    Logger.info(s"Adding asset videoUri $videoUri to $atomId")
    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
        val mediaAtom = atom.tdata
        val currentAssets: Seq[Asset] = mediaAtom.assets

        val resolvedVersion = version.getOrElse(currentAssets.foldLeft(1L){(acc, asset) => if (asset.version >= acc) asset.version + 1 else acc})

        if (currentAssets.exists(asset => asset.version == resolvedVersion && asset.mimeType == mimeType)) {
          AssetVersionConflict
        }

        val newAsset = ThriftUtil.parseAsset(videoUri, mimeType, resolvedVersion)
          .fold(err => AssetParseFailed, identity)

        val assetDuration = newAsset.platform match {
          case Platform.Youtube => YouTubeVideoInfoApi(youtubeConfig).getDuration(newAsset.id)
          case _ => None
        }

        val updatedAtom = atom
          .withData(mediaAtom.copy(
            assets = newAsset +: currentAssets,
            duration = assetDuration
          ))

        Logger.info(s"Constructed new atom $atomId, updating")

        UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom)).process()

      case None => AtomNotFound
    }
  }
}
