package repositories

import javax.inject.Inject

import me.shoma.play_cms.repositories.DBTableDefinitions
import play.api.db.slick.DatabaseConfigProvider

class PostCategoryRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def sync(postId: Long) = {
    (for {
      _ <-DBIO.seq(PostCategories.filter(_.postId === actionPost.getOrElse(dbPost).id).delete)
    } yield ())
  }
}
