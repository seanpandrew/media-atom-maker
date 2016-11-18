package util

import com.gu.contentatom.thrift.Atom

object Paginator {
  def selectPage(atoms: Seq[Atom], pageNumber: Int, pageSize: Int): Seq[Atom] = {
    val startIndex = (pageNumber - 1) * pageSize
    atoms.slice(startIndex, startIndex + pageSize)
  }
}
