package data

import com.google.inject.ImplementedBy
import com.gu.contentatom.thrift.{ Atom, ContentAtomEvent }
import scala.util.Try

trait AtomPublisher {
  def publishAtomEvent(event: ContentAtomEvent): Try[Unit]
}

trait AtomReindexer extends AtomPublisher {
  def reindexAtoms(atoms: TraversableOnce[Atom]): Try[Long]
}
