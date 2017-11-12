package util

import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.gu.media.aws.{AwsAccess, UploadAccess}
import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtom
import com.gu.media.upload.model.UploadCredentials
import com.gu.media.util.MediaAtomHelpers
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

class CredentialsGenerator(aws: UploadAccess with AwsAccess, stepFunctions: StepFunctions) extends Logging {
  def forVideoPartKey(key: String): UploadCredentials = {
    val keyPolicy = generateKeyPolicy(key, isMultiPart = true)

    generateCredentials(key, keyPolicy)
  }

  def forPacFile(mediaAtom: MediaAtom): UploadCredentials = {
    val sfJobs = stepFunctions.getJobs(mediaAtom.id)
    val uploadPrefix = if (sfJobs.nonEmpty) {
      sfJobs.head.getName
    } else {
      val currentVersion = MediaAtomHelpers.getCurrentAssetVersion(mediaAtom).getOrElse(1)
      s"${mediaAtom.id}-$currentVersion"
    }

    val key = s"${aws.userUploadFolder}/$uploadPrefix/pacfile.xml"
    val keyPolicy = generateKeyPolicy(key, isMultiPart = false)
    generateCredentials(key, keyPolicy)
  }

  private def generateCredentials(key: String, keyPolicy: String): UploadCredentials = {
    val request = new AssumeRoleRequest()
      .withRoleArn(aws.userUploadRole)
      .withDurationSeconds(900) // 15 minutes (the minimum allowed in STS requests)
      .withPolicy(keyPolicy)
      .withRoleSessionName(s"media-atom-pipeline")

    log.info(s"Issuing STS request for $key")
    val result = aws.uploadSTSClient.assumeRole(request)
    log.info(s"Received STS credentials for $key")

    val credentials = result.getCredentials

    UploadCredentials(
      credentials.getAccessKeyId,
      credentials.getSecretAccessKey,
      credentials.getSessionToken,
      aws.region.toString,
      aws.userUploadBucket,
      key
    )
  }

  private def generateKeyPolicy(key: String, isMultiPart: Boolean): String = {
    val keyArn = s"arn:aws:s3:::${aws.userUploadBucket}/$key"

    val corePermissions = List(
      "s3:PutObject",
      "s3:PutObjectAcl"
    )

    val multipartPermissions = List(
      "s3:ListMultipartUploadParts",
      "s3:AbortMultipartUpload",
      "s3:ListBucketMultipartUploads"
    )

    val permissions = if (isMultiPart) corePermissions ++ multipartPermissions else corePermissions

    val json = JsObject(List(
      "Statement" -> JsArray(List(
        JsObject(List(
          "Action" -> JsArray(permissions.map(JsString)),
          "Resource" -> JsString(keyArn),
          "Effect" -> JsString("Allow")
        ))
      ))
    ))

    Json.stringify(json)
  }
}
