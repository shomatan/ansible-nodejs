package me.shoma.play_cms.controllers.api.v1

import javax.inject._

import me.shoma.play_cms.models.Tag
import me.shoma.play_cms.repositories.TagRepository
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class TagController @Inject()(
                                cc: ControllerComponents,
                                repository: TagRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val tagFormat = Json.format[Tag]

  def list = Action.async {
    repository.list.map {
      case (tags) => Ok(Json.obj("tags" -> Json.toJson(tags)))
    }
  }
}