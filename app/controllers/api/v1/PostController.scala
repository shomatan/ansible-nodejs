package me.shoma.play_cms.controllers.api.v1

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import me.shoma.play_cms.models.{Category, Post, Tag}
import me.shoma.play_cms.repositories.PostRepository
import me.shoma.play_cms.utils.authentication.DefaultEnv
import play.api.mvc._
import play.api.libs.json.{JsError, Json}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostController @Inject()(
                                cc: ControllerComponents,
                                val silhouette: Silhouette[DefaultEnv],
                                postRepository: PostRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val categoryFormat = Json.format[Category]
  implicit val tagFormat = Json.format[Tag]
  implicit val postFormat = Json.format[Post]

  def list = Action.async {

    postRepository.list().map { case (posts) =>
      val json = Json.toJson(posts)
      Ok(json)
    }
  }

  def find(id: Long) = Action.async { implicit request =>
    postRepository.find(id).map {
      case Some(p) => Ok(Json.toJson(p))
      case _ => NotFound(Json.obj("result" ->"failure", "error" -> "The requested ID does not exist."))
    }
  }

  def post = Action.async(parse.json) { implicit request =>
  //def post() = silhouette.SecuredAction.async {

    request.body.validate[Post].map { post =>

      postRepository.save(post).map { p =>
        Ok(Json.obj("result" -> "success", "post" -> p)) }
    }.recoverTotal { e =>
      Future {
        BadRequest(Json.obj("result" ->"failure", "error" -> JsError.toJson(e)))
      }
    }
  }

}