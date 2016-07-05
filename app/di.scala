import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.gu.pandomainauth.action.AuthActions

import data._

class AtomModule extends AbstractModule {
  def configure() = {
    bind(classOf[AuthActions])   to classOf[controllers.PanDomainAuthActions]
    bind(classOf[AtomPublisher]) to classOf[KinesisAtomPublisher]
    bind(classOf[AtomReindexer]) to classOf[KinesisAtomReindexer]
  }
}

class Module extends AtomModule
