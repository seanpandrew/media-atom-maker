package data

import com.google.inject.ImplementedBy
import com.gu.contentatom.thrift.ContentAtomEvent
import scala.util.Try

trait AtomPublisher {
  def publishAtomEvent(event: ContentAtomEvent): Try[Unit]
}
