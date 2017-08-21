package me.shoma.play_cms.repositories

import javax.inject.Inject

import me.shoma.play_cms.models.Category
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CategoryRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list: Future[List[Category]] = {

    val action = slickCategories.sortBy(_.name.asc).to[List].result

    db.run(action).map { resultOption =>
      resultOption.map {
        case (category) =>
          Category(
            Some(category.id),
            category.name
          )
      }
    }
  }
}
