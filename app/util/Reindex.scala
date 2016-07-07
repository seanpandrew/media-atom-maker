package util.reindex

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
case class GetStatus(id: Option[Int]) extends JobMsg
case class ReindexJob(
    id: Int,
    size: Int,
    completed: Int = 0, // amount of completed atoms
    startTime: Date = new Date(),
    endTime: Option[Date] = None
) {

  def duration = endTime.map(end => (end.getTime - startTime.getTime).millis)
  def complete = {
    copy(endTime = Some(new Date()))
  }
}

/*
 * this actor handles the work done within a single job
 */

case class ReindexJobHandler(job: ReindexJob) extends Actor {
  private var count = 0
  println(s"reindex job handler actor ${job.id}")
  context.system.scheduler.scheduleOnce(10.seconds) {
    println(s"PMR 1754 scheduler tick ${context.parent}")
    context.parent ! Completed(job)
  }
  def receive = {
    case GetStatus(None) | GetStatus(Some(job.id)) => sender ! job.copy(completed = count)
  }
}

/*
 * this actor is responsible for the lifecycle of reindexing jobs
 */

class ReindexManager extends Actor {
  private var nextId = 0
  def receive = {
    case Completed(job) =>
      println(s"job ${job.id} completed (#children: ${context.children.size}")
      context.stop(sender)
    case Start(query) =>
      println(s"reindex manager: $self")
      val job = ReindexJob(id = nextId, size = 0)
      nextId += 1
      val child = context.actorOf(Props(classOf[ReindexJobHandler], job))
      sender ! job
  }
}
