package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import CommandExceptions._
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.contentatom.thrift.atom.media.Asset
import util.atom.MediaAtomImplicits
import util.ThriftUtil

import scala.util.{Failure, Success}

case class AddAssetCommand(atomId: String,
                           videoUri: String,
                           version: Option[Long],
                           mimeType: Option[String])
                          (implicit previewDataStore: PreviewDataStore,
                           previewPublisher: PreviewAtomPublisher)
    extends Command
    with MediaAtomImplicits {

  type T = Unit

  def process(): Unit = {
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

        val newAtom = atom
          .withData(mediaAtom.copy(
            activeVersion = Some(newAsset.version),
            assets = newAsset +: currentAssets
          ))
          .withRevision(_ + 1)

        previewDataStore.updateAtom(newAtom).fold(
          err => InternalServerError(err.msg),
          _ => {
            val event = ContentAtomEvent(newAtom, EventType.Update, new Date().getTime)

            previewPublisher.publishAtomEvent(event) match {
              case Success(_) => ()
              case Failure(err) => InternalServerError(s"could not publish: ${err.toString}")
            }
          }
        )
      case None => AtomNotFound
    }
  }
}
