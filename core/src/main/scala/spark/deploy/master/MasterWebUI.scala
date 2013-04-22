package spark.deploy.master

import akka.actor.{ActorRef, ActorContext, ActorRefFactory}
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing.Directives
import spray.routing.directives._
import spray.httpx.TwirlSupport._
import spray.httpx.SprayJsonSupport._
import spray.http.MediaTypes._

import spark.deploy._
import spark.deploy.JsonProtocol._

/**
 * Web UI server for the standalone master.
 */
private[spark]
class MasterWebUI(master: ActorRef)(implicit val context: ActorContext) extends Directives {
  import context.dispatcher

  val actorSystem         = context.system
  val RESOURCE_DIR = "spark/deploy/master/webui"
  val STATIC_RESOURCE_DIR = "spark/deploy/static"

  implicit val timeout = Timeout(10 seconds)

  val handler = {
    get {
      (path("") & parameters('format ?)) {
        case Some(js) if js.equalsIgnoreCase("json") =>
          val future = (master ? RequestMasterState).mapTo[MasterState]
          respondWithMediaType(`application/json`) { ctx =>
            ctx.complete(future.mapTo[MasterState])
          }
        case _ =>
          complete {
            val future = (master ? RequestMasterState).mapTo[MasterState]
            future.map {
              masterState => spark.deploy.master.html.index.render(masterState)
            }
          }
      } ~
      path("app") {
        parameters("appId", 'format ?) {
          case (appId, Some(js)) if (js.equalsIgnoreCase("json")) =>
            val future = master ? RequestMasterState
            val appInfo = for (masterState <- future.mapTo[MasterState]) yield {
              masterState.activeApps.find(_.id == appId).getOrElse({
                masterState.completedApps.find(_.id == appId).getOrElse(null)
              })
            }
<<<<<<< HEAD
            respondWithMediaType(`application/json`) { ctx =>
              ctx.complete(jobInfo.mapTo[JobInfo])
            }
          case (jobId, _) =>
            complete {
              val future = (master ? RequestMasterState).mapTo[MasterState]
              future.map { masterState =>
                masterState.activeJobs.find(_.id == jobId) match {
                  case Some(job) => spark.deploy.master.html.job_details.render(job)
                  case _ => masterState.completedJobs.find(_.id == jobId) match {
                    case Some(job) => spark.deploy.master.html.job_details.render(job)
                    case _ => null
                  }
                }
=======
            respondWithMediaType(MediaTypes.`application/json`) { ctx =>
              ctx.complete(appInfo.mapTo[ApplicationInfo])
            }
          case (appId, _) =>
            completeWith {
              val future = master ? RequestMasterState
              future.map { state =>
                val masterState = state.asInstanceOf[MasterState]
                val app = masterState.activeApps.find(_.id == appId).getOrElse({
                  masterState.completedApps.find(_.id == appId).getOrElse(null)
                })
                spark.deploy.master.html.app_details.render(app)
>>>>>>> 17e076de800ea0d4c55f2bd657348641f6f9c55b
              }
            }
        }
      } ~
      pathPrefix("static") {
        getFromResourceDirectory(STATIC_RESOURCE_DIR)
      } ~
      getFromResourceDirectory(RESOURCE_DIR)
    }
  }
}
