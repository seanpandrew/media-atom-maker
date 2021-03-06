package model.commands

import java.util.Date

import ai.x.diff.DiffShow
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandomainauth.model.{User => PandaUser}
import com.gu.media.logging.Logging
import data.DataStores
import model.commands.CommandExceptions._
import com.gu.media.model.{ChangeRecord, MediaAtom}
import com.gu.media.util.MediaAtomImplicits
import org.joda.time.DateTime

import scala.util.{Failure, Success}

case class UpdateAtomCommand(id: String, atom: MediaAtom, override val stores: DataStores, user: PandaUser)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to update atom ${atom.id}")

    if (id != atom.id) {
      AtomIdConflict
    }

    val existingAtom = getPreviewAtom(atom.id)

    val diffString = createDiffString(MediaAtom.fromThrift(existingAtom), atom)
    log.info(s"Update atom changes ${atom.id}: $diffString")

    val changeRecord = ChangeRecord.now(user)

    val scheduledLaunchDate: Option[DateTime] = atom.contentChangeDetails.scheduledLaunch.map(scheduledLaunch => new DateTime(scheduledLaunch.date))

    val details = atom.contentChangeDetails.copy(
      revision = existingAtom.contentChangeDetails.revision + 1,
      lastModified = Some(changeRecord),
      scheduledLaunch = scheduledLaunchDate.map(ChangeRecord.build(_, user))
    )
    val thrift = atom.copy(contentChangeDetails = details).asThrift

    previewDataStore.updateAtom(thrift).fold(
      err => {
        log.error(s"Unable to update atom ${atom.id}", err)
        AtomUpdateFailed(err.msg)
      },
      _ => {
        val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_) => {
            auditDataStore.auditUpdate(id, getUsername(user), diffString)

            log.info(s"Successfully updated atom ${atom.id}")
            MediaAtom.fromThrift(thrift)
          }
          case Failure(err) =>
            log.error(s"Unable to publish updated atom ${atom.id}", err)
            AtomPublishFailed(s"could not publish: ${err.toString}")
        }
      }
    )
  }

  private val interestingFields = List("title", "category", "description", "duration", "source", "youtubeCategoryId", "license", "commentsEnabled", "channelId", "legallySensitive")

  // We don't use HTTP patch so diffing has to be done manually
  def createDiffString(before: MediaAtom, after: MediaAtom): String = {
    val fieldDiffs = DiffShow.diff[MediaAtom](before, after).string
      .replaceAll("\\[*[0-9]+m", "") // Clean out the silly console coloring stuff
      .split('\n')
      .map(_.trim())
      .filter(line => !line.contains("ERROR")) // More silly stuff from diffing library
      .filter(line => interestingFields.exists(line.contains))
      .mkString(", ")

    if (fieldDiffs == "") { // There's a change, but in some field we're not interested in (or rather, unable to format nicely)
      "Updated atom fields"
    } else {
      s"Updated atom fields ($fieldDiffs)"
    }
  }
}
