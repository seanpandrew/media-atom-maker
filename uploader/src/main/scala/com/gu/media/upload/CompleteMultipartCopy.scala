package com.gu.media.upload

import com.amazonaws.services.s3.model.{CompleteMultipartUploadRequest, PartETag}
import com.gu.media.aws.S3Access
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{CopyETag, CopyProgress, PlutoSyncMetadata, Upload}

import scala.util.control.NonFatal
import scala.collection.JavaConverters._

class CompleteMultipartCopy extends LambdaWithParams[Upload, Upload] with S3Access with Logging {
  override def handle(upload: Upload) = {
    upload.progress.copyProgress match {
      case Some(CopyProgress(copyId, _ , copyETags)) =>
        val bucket = upload.metadata.bucket
        val destination = s"${upload.metadata.pluto.completeKey()}"
        val eTags = copyETags.map { case CopyETag(n, t) => new PartETag(n, t) }

        log.info(s"Completing multipart copy. upload=${upload.id} multipart=$copyId")

        val request = new CompleteMultipartUploadRequest(bucket, destination, copyId, eTags.asJava)
        s3Client.completeMultipartUpload(request)

      case None =>
        log.error("Missing copyProgress when invoking CompleteMultipartCopy")
    }

    // The complete key will be deleted once it has been ingested by Pluto
    upload.parts.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        s3Client.deleteObject(upload.metadata.bucket, part.key)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }

    upload
  }
}
