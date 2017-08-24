package me.shoma.play_cms.repositories

import javax.inject.Inject

import me.shoma.play_cms.models.Category
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CategoryRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list: Future[List[Category]] = {

    val action = Categories.sortBy(_.name.asc).to[List].result

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

  def findByPost(postId: Long) = {
    PostCategories.filter(_.postId === postId)
      .joinLeft(Categories).on(_.categoryId === _.id)
      .to[List].result
  }

  def findByPost(postIds: Seq[Long]) = {
    PostCategories.filter(_.postId.inSet(postIds))
      .joinLeft(Categories)
      .on(_.categoryId === _.id)
      .to[List].result
  }

  def insertOrUpdate(categories: Seq[Category]) = {
    DBIO.sequence(categories.map { current =>
      Categories.filter(_.name === current.name).result.headOption.flatMap {
        case Some(category) => DBIO.successful(category)
        case None => Categories.returning(Categories) += DBCategory(0, current.name)
      }
    })
  }

  def sync(postId: Long, postCategories: Seq[DBCategory]) = {
    for {
      _ <- DBIO.seq(PostCategories.filter(_.postId === postId).delete)
      _ <- DBIO.seq(postCategories.map { c => PostCategories += DBPostCategory(postId = postId, categoryId = c.id)}: _*)
    } yield ()
  }
}
