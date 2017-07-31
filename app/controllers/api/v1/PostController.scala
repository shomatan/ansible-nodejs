package me.shoma.play_cms.controllers.api.v1

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import me.shoma.play_cms.utils.authentication.DefaultEnv
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostController @Inject()(
                                cc: ControllerComponents,
                                val silhouette: Silhouette[DefaultEnv])(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def list = Action.async {
    Future(Ok(""))
  }

  def post = silhouette.SecuredAction.async {
    Future(Ok(""))
  }

}