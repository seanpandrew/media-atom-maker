package model

import com.gu.contentatom.thrift.atom.media.{MediaAtom => ThriftMediaAtom, Metadata => ThriftMetadata, PlutoData => ThriftPlutoData}
import com.gu.contentatom.thrift.{AtomData, Atom => ThriftAtom, AtomType => ThriftAtomType, Flags => ThriftFlags}
import org.cvogt.play.json.Jsonx
import util.atom.MediaAtomImplicits

abstract class MediaAtomBase {
  val title: String
  val category: Category
  val source: Option[String]
  val description: Option[String]
  val trailText: Option[String]
  val posterImage: Option[Image]
  val trailImage: Option[Image]
  // metadata
  val tags: List[String]
  val byline: List[String]
  val commissioningDesks: List[String]
  val keywords: List[String]
  val youtubeCategoryId: Option[String]
  val license: Option[String]
  val channelId: Option[String]
  val commentsEnabled: Boolean
  val legallySensitive: Option[Boolean]
  val sensitive: Option[Boolean]
  val privacyStatus: Option[PrivacyStatus]
  val expiryDate: Option[Long]
  val blockAds: Boolean
}

// This is used to parse the a media atom from a create atom
// request before an id has been added to it
case class MediaAtomBeforeCreation(
  title: String,
  category: Category,
  source: Option[String],
  description: Option[String],
  trailText: Option[String],
  posterImage: Option[Image],
  trailImage: Option[Image],
  tags: List[String],
  byline: List[String],
  commissioningDesks: List[String],
  keywords: List[String],
  youtubeCategoryId: Option[String],
  license: Option[String],
  channelId: Option[String],
  commentsEnabled: Boolean,
  legallySensitive: Option[Boolean],
  sensitive: Option[Boolean],
  privacyStatus: Option[PrivacyStatus],
  expiryDate: Option[Long],
  blockAds: Boolean
) extends MediaAtomBase {

  def asThrift(id: String, contentChangeDetails: ContentChangeDetails) = {
    val data = ThriftMediaAtom(
      assets = Nil,
      activeVersion = None,
      title = title,
      category = category.asThrift,
      duration = None,
      source = source,
      posterUrl = posterImage.flatMap(_.master).map(_.file),
      description = description,
      trailText = trailText,
      posterImage = posterImage.map(_.asThrift),
      trailImage = trailImage.map(_.asThrift),
      byline = Some(byline),
      commissioningDesks = Some(commissioningDesks),
      keywords = Some(keywords),
      metadata = Some(ThriftMetadata(
        tags = Some(tags),
        categoryId = youtubeCategoryId,
        license = license,
        commentsEnabled = Some(commentsEnabled),
        channelId = channelId,
        privacyStatus = privacyStatus.flatMap(_.asThrift),
        expiryDate = expiryDate,
        pluto = None
      ))
    )

    ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = MediaAtomImplicits.defaultMediaHtml(data),
      title = Some(title),
      data = AtomData.Media(data),
      contentChangeDetails = contentChangeDetails.asThrift,
      flags = Some(ThriftFlags(
        legallySensitive = legallySensitive,
        blockAds = Some(blockAds),
        sensitive = sensitive
      ))
    )
  }
}

object MediaAtomBeforeCreation {
  implicit val mediaAtomBeforeCreationFormat = Jsonx.formatCaseClass[MediaAtomBeforeCreation]
}

// Note: This is *NOT* structured like the thrift representation
case class MediaAtom(
  // Atom wrapper fields
  id: String,
  labels: List[String],
  contentChangeDetails: ContentChangeDetails,
  // data field
  assets: List[Asset],
  activeVersion: Option[Long],
  title: String,
  category: Category,
  plutoData: Option[PlutoData],
  duration: Option[Long],
  source: Option[String],
  description: Option[String],
  trailText: Option[String],
  posterImage: Option[Image],
  trailImage: Option[Image],
  // metadata
  tags: List[String],
  byline: List[String],
  commissioningDesks: List[String],
  keywords: List[String],
  youtubeCategoryId: Option[String],
  license: Option[String],
  channelId: Option[String],
  commentsEnabled: Boolean = false,
  legallySensitive: Option[Boolean],
  sensitive: Option[Boolean],
  privacyStatus: Option[PrivacyStatus],
  expiryDate: Option[Long] = None,
  blockAds: Boolean = false)
    extends MediaAtomBase {


  def asThrift = {
    val data = ThriftMediaAtom(
      assets = assets.map(_.asThrift),
      activeVersion = activeVersion,
      title = title,
      category = category.asThrift,
      duration = duration,
      source = source,
      posterUrl = posterImage.flatMap(_.master).map(_.file),
      description = description,
      trailText = trailText,
      posterImage = posterImage.map(_.asThrift),
      trailImage = trailImage.map(_.asThrift),
      byline = Some(byline),
      commissioningDesks = Some(commissioningDesks),
      keywords = Some(keywords),
      metadata = Some(ThriftMetadata(
        tags = Some(tags),
        categoryId = youtubeCategoryId,
        license = license,
        commentsEnabled = Some(commentsEnabled),
        channelId = channelId,
        privacyStatus = privacyStatus.flatMap(_.asThrift),
        expiryDate = expiryDate,
        pluto = plutoData.map(_.asThrift)
      ))
    )

    ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = MediaAtomImplicits.defaultMediaHtml(data),
      title = Some(title),
      data = AtomData.Media(data),
      contentChangeDetails = contentChangeDetails.asThrift,
      flags = Some(ThriftFlags(
        legallySensitive = legallySensitive,
        blockAds = Some(blockAds),
        sensitive = sensitive
      ))
    )
  }
}

object MediaAtom extends MediaAtomImplicits {
  implicit val mediaAtomFormat = Jsonx.formatCaseClass[MediaAtom]

  def fromThrift(atom: ThriftAtom) = {
    val data = atom.tdata

    MediaAtom(
      id = atom.id,
      labels = atom.labels.toList,
      contentChangeDetails = ContentChangeDetails.fromThrift(atom.contentChangeDetails),
      assets = data.assets.map(Asset.fromThrift).toList,
      activeVersion = data.activeVersion,
      title = data.title,
      category = Category.fromThrift(data.category),
      plutoData = data.metadata.flatMap(_.pluto).map(PlutoData.fromThrift),
      duration = data.duration,
      source = data.source,
      posterImage = data.posterImage.map(Image.fromThrift),
      trailImage = data.trailImage.map(Image.fromThrift),
      description = data.description,
      trailText = data.trailText,
      tags = data.metadata.flatMap(_.tags.map(_.toList)).getOrElse(Nil),
      byline = data.byline.map(_.toList).getOrElse(Nil),
      commissioningDesks = data.commissioningDesks.map(_.toList).getOrElse(Nil),
      keywords = data.keywords.map(_.toList).getOrElse(Nil),
      youtubeCategoryId = data.metadata.map(_.categoryId).getOrElse(None),
      expiryDate = data.metadata.map(_.expiryDate).getOrElse(None),
      blockAds = atom.flags.flatMap(_.blockAds).getOrElse(false),
      license = data.metadata.flatMap(_.license),
      commentsEnabled = data.metadata.flatMap(_.commentsEnabled).getOrElse(false),
      channelId = data.metadata.flatMap(_.channelId),
      legallySensitive = atom.flags.flatMap(_.legallySensitive),
      sensitive = atom.flags.flatMap(_.sensitive),
      privacyStatus = data.metadata.flatMap(_.privacyStatus).flatMap(PrivacyStatus.fromThrift)
    )
  }

  def getActiveYouTubeAsset(mediaAtom: MediaAtom): Option[Asset] = {
    val assets = mediaAtom.assets
    val activeAsset = mediaAtom.activeVersion.flatMap(activeVersion => assets.find(_.version == activeVersion))

    activeAsset match {
      case Some(asset) if asset.platform == Platform.Youtube => Some(asset)
      case _ => None
    }
  }
}
