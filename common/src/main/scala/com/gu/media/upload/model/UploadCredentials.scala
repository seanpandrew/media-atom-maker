package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadCredentials(
  temporaryAccessId: String,
  temporarySecretKey: String,
  sessionToken: String,
  region: String,
  bucket: String,
  key: String
)

object UploadCredentials {
  implicit val format: Format[UploadCredentials] = Jsonx.formatCaseClass[UploadCredentials]
}

sealed abstract class UploadCredentialRequest { }

final case class VideoPartUploadCredentialRequest (
  uploadId: String,
  key: String
) extends UploadCredentialRequest

final case class PacFileUploadCredentialRequest (
  atomId: String
) extends UploadCredentialRequest

object UploadCredentialRequest {
  implicit val videoUploadCredentialRequestFormat: Format[VideoPartUploadCredentialRequest] = Jsonx.formatCaseClass[VideoPartUploadCredentialRequest]
  implicit val pacFileUploadCredentialRequest: Format[PacFileUploadCredentialRequest] = Jsonx.formatCaseClass[PacFileUploadCredentialRequest]
  implicit val format: Format[UploadCredentialRequest] = Jsonx.formatSealed[UploadCredentialRequest]
}
