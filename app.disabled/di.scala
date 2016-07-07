import com.google.inject.name.Names
import com.google.inject.AbstractModule
import com.gu.pandomainauth.action.AuthActions
import play.api.libs.concurrent.AkkaGuiceSupport
import data._
import util.reindex.ReindexManager

class Module() extends AbstractModule
    with AkkaGuiceSupport {
  def configure() = {
    bind(classOf[AuthActions]) to classOf[controllers.PanDomainAuthActions]
    bind(classOf[AtomPublisher]) to classOf[KinesisAtomPublisher]
    bind(classOf[AtomReindexer]) to classOf[KinesisAtomReindexer]
    bindActor[ReindexManager]("reindex-lifecycle-manager")
  }
}
