package me.shoma.ayumi.controllers.api.v1

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import me.shoma.ayumi.model._
import me.shoma.ayumi.repositories.PostRepository
import me.shoma.ayumi.utils.authentication.DefaultEnv

import play.api.mvc._
import play.api.libs.json.{JsError, Json}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import me.shoma.ayumi.services.PostService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostController @Inject()(
                                cc: ControllerComponents,
                                val silhouette: Silhouette[DefaultEnv],
                                postRepository: PostRepository,
                                postService: PostService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val zonedDateTimeWrites = new Writes[ZonedDateTime] {
    def writes(d: ZonedDateTime): JsValue = JsString(d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
  }

  implicit val customFieldWrites = new Writes[CustomField] {
    override def writes(customField: CustomField): JsValue = {
      Json.obj(
        "postId" -> Json.toJson(customField.postId),
        "key" -> Json.toJson(customField.key)
      ) ++ {
        customField.value match {
          case a: Int => Json.obj("value" -> a)
          case a: String => Json.obj("value" -> a)
          case a => Json.obj("value" -> a.toString)
        }
      }
    }
  }

  implicit val customFieldReads = (
    (__ \ "postId").read[Long] and
    (__ \ "key"   ).read[String] and
    (__ \ "value" ).read[Any](Reads[Any](m => metaValueToJsValue(m)))
  )(CustomField)

  implicit val categoryFormat = Json.format[Category]
  implicit val tagFormat = Json.format[Tag]
  implicit val postFormat = Json.format[Post]

  def list(page: Option[Int], perPage: Option[Int]) = Action.async {
    val pageNumber = page.getOrElse(1)
    val perPageNumber = perPage.getOrElse(10)

    postService.list(page = pageNumber, perPage = perPageNumber).map { case (result) =>
      Ok(Json.obj(
        "posts" -> Json.toJson(result.posts),
        "page" -> JsNumber(pageNumber),
        "perPage" -> JsNumber(perPageNumber)
      )).withHeaders("X-Total-Count" -> result.total.toString)
    }
  }

  def find(id: Long) = Action.async { implicit request =>
    postService.find(id).map {
      case Some(p) => Ok(Json.obj("post" -> Json.toJson(p)))
      case _ => NotFound(Json.obj("error" -> Json.obj("message" -> "The requested ID does not exist.")))
    }
  }

  def post = Action.async(parse.json) { implicit request =>
  //def post() = silhouette.SecuredAction.async {

    request.body.validate[Post].map { post =>
      postService.save(post).map { p =>
        Ok(Json.obj("post" -> p))
      }
    }.recoverTotal { e =>
      Future {
        BadRequest(Json.obj("error" -> JsError.toJson(e)))
      }
    }
  }

  private def metaValueToJsValue(m: JsValue): JsResult[Any] = {
    m match {
      case JsString(s) => JsSuccess(s)
      case JsNumber(n) => JsSuccess(n)
      case n => JsSuccess(n.toString())
    }
  }
}