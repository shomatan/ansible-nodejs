package me.shoma.play_cms.repositories

import javax.inject.Inject

import me.shoma.play_cms.models.Tag
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TagRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list: Future[List[me.shoma.play_cms.models.Tag]] = {

    val action = slickTags.sortBy(_.name.asc).to[List].result

    db.run(action).map { resultOption =>
      resultOption.map {
        case (tag) =>
          Tag(
            Some(tag.id),
            tag.name
          )
      }
    }
  }
}
