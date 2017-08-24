package me.shoma.play_cms.repositories

import javax.inject.Inject

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
          me.shoma.play_cms.models.Tag(
            Some(tag.id),
            tag.name
          )
      }
    }
  }

  def findByPost(postId: Long) = {
    PostTags.filter(_.postId === postId)
      .joinLeft(slickCategories).on(_.tagId === _.id)
      .to[List].result
  }

  def findByPost(postIds: Seq[Long]) = {
    PostTags.filter(_.postId.inSet(postIds))
      .joinLeft(slickCategories)
      .on(_.tagId === _.id)
      .to[List].result
  }

  def insertOrUpdate(tags: Seq[me.shoma.play_cms.models.Tag]) = {
    DBIO.sequence(tags.map { current =>
      slickTags.filter(_.name === current.name).result.headOption.flatMap {
        case Some(tag) => DBIO.successful(tag)
        case None => slickTags.returning(slickTags) += DBTag(0, current.name)
      }
    })
  }

  case class DBPostTag(postId: Long, tagId: Long)

  class PostTag(tag: Tag) extends Table[DBPostTag](tag, "post_tag") {

    def postId = column[Long]("post_id")
    def tagId = column[Long]("tag_id")

    def * = (postId, tagId) <> (DBPostTag.tupled, DBPostTag.unapply _)
  }

  val PostTags = TableQuery[PostTag]
}
