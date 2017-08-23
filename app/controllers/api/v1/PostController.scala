package me.shoma.play_cms.controllers.api.v1

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import me.shoma.play_cms.models._
import me.shoma.play_cms.repositories.PostRepository
import me.shoma.play_cms.utils.authentication.DefaultEnv
import play.api.mvc._
import play.api.libs.json.{JsError, Json}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostController @Inject()(
                                cc: ControllerComponents,
                                val silhouette: Silhouette[DefaultEnv],
                                postRepository: PostRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {

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
    (__ \ "id"   ).read[Long] and
    (__ \ "key"  ).read[String] and
    (__ \ "value").read[Any](Reads[Any](m => metaValueToJsValue(m)))
  )(CustomField)

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

  private def metaValueToJsValue(m: JsValue): JsResult[Any] = {
    m match {
      case JsString(s) => JsSuccess(s)
      case JsNumber(n) => JsSuccess(n)
      case n => JsSuccess(n.toString())
    }
  }
}