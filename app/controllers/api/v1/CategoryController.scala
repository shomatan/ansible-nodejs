package me.shoma.ayumi.controllers.api.v1

import javax.inject._

import me.shoma.ayumi.repositories.CategoryRepository
import me.shoma.ayumi.model.Category
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
      case (categories) => Ok(Json.toJson(categories))
    }
  }
}