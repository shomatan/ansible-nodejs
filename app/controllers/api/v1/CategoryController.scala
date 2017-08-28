package me.shoma.play_cms.controllers.api.v1

import javax.inject._

import me.shoma.play_cms.models.Category
import me.shoma.play_cms.repositories.CategoryRepository
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(
                                cc: ControllerComponents,
                                repository: CategoryRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val categoryFormat = Json.format[Category]

  def list = Action.async {
    repository.list.map {
      case (categories) => Ok(Json.obj("categories" -> Json.toJson(categories)))
    }
  }
}