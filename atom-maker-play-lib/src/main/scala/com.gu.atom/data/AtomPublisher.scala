package com.gu.atom.data

import cats.data.Xor
import com.gu.contentatom.thrift.{ Atom, ContentAtomEvent }
import scala.util.Try

trait AtomPublisher {
  def publishAtomEvent(event: ContentAtomEvent): Xor[Throwable, Unit]
}
