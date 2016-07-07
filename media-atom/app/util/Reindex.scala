package util.reindex

import akka.actor.PoisonPill
import play.api.libs.json.{ JsString, JsObject }
import scala.collection.mutable.{ Map => MMap }

import scala.concurrent.Future

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

import java.util.Date

sealed trait JobMsg
case class Start(query: data.Query) extends JobMsg
case class Completed(job: ReindexJob) extends JobMsg
case class GetStatus(id: String) extends JobMsg
case class ReindexJob(
    id: String,
    size: Int,
    completed: Int = 0, // amount of completed atoms
    startTime: Date = new Date(),
    endTime: Option[Date] = None
) {

  def duration = endTime.map(end => (end.getTime - startTime.getTime).millis)
  def complete(count: Int) = {
    copy(endTime = Some(new Date()), completed = count)
  }
}

/*
 * this actor handles the work done within a single job
 */

class ReindexJobHandler extends Actor {

  private var countSoFar = 0

  case class Tick(count: Int)

  def tick(count: Int) = context.system.scheduler.scheduleOnce(1.seconds, self, Tick(count))

  def runJob(job: ReindexJob): Receive = {
    case Tick(count) =>
      countSoFar = count
      println(s"runJob tick: $count")
      if (count < job.size) tick(count + 1)
      else context.parent ! Completed(job complete count)
    case GetStatus(id) =>
      sender ! job.copy(completed = countSoFar)
  }

  def receive = {
    case job: ReindexJob =>
      context become runJob(job)
      tick(0)
    case GetStatus(_) =>
      sender ! JsString("not running")
  }
}

/*
 * this actor is responsible for the lifecycle of reindexing jobs
 */

class ReindexManager extends Actor {
  private var nextId: Int = 0
  def receive = {
    case Completed(job) =>
      println(s"job ${job} completed")
      sender ! PoisonPill
    case Start(query) =>
      val id = "job-" + nextId.toString
      println(s"reindex manager: $self")
      val child = context.actorOf(Props(classOf[ReindexJobHandler]), id)
      val job = ReindexJob(id = id, size = 10)
      child ! job
      sender ! job
      nextId += 1
    case GetStatus(id) =>
      context.child(id) match {
        case None => sender ! JsObject(Map("err" -> JsString("not found"), "id" -> JsString(id)))
        case Some(actor) => actor.forward(GetStatus(id))
      }
  }
}
