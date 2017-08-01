package me.shoma.play_cms.controllers.api.v1

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import me.shoma.play_cms.models.{Category, Post, Tag}
import me.shoma.play_cms.repositories.PostRepository
import me.shoma.play_cms.utils.authentication.DefaultEnv
import play.api.mvc._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostController @Inject()(
                                cc: ControllerComponents,
                                val silhouette: Silhouette[DefaultEnv],
                                postRepository: PostRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val categoryWrites = Json.writes[Category]
  implicit val tagWrites = Json.writes[Tag]
  implicit val postWrites = Json.writes[Post]

  def list = Action.async {

    postRepository.list().map { case (posts) =>
      val json = Json.toJson(posts)
      Ok(json)
    }
  }

  def post = silhouette.SecuredAction.async {
    Future(Ok(""))
  }

}