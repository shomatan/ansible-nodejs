package me.shoma.play_cms.controllers.api.v1

import javax.inject._

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostController @Inject()(cc: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def posts = Action.async {
    Future(Ok(""))
  }

}