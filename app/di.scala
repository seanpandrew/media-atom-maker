import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.gu.pandomainauth.action.AuthActions

import data._

class Module() extends AbstractModule {
  def configure() = {

    bind(classOf[AuthActions]) to classOf[controllers.PanDomainAuthActions]

    bind(classOf[AtomPublisher]) to (classOf[KinesisAtomPublisher])

    (bind(classOf[AtomPublisher])
       annotatedWith Names.named("reindex")) to (classOf[KinesisAtomReindexer])
  }
}
