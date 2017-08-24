package me.shoma.play_cms.repositories

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TagRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list: Future[List[me.shoma.play_cms.models.Tag]] = {

    val action = Tags.sortBy(_.name.asc).to[List].result

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
      .joinLeft(Tags).on(_.tagId === _.id)
      .to[List].result
  }

  def findByPost(postIds: Seq[Long]) = {
    PostTags.filter(_.postId.inSet(postIds))
      .joinLeft(Tags)
      .on(_.tagId === _.id)
      .to[List].result
  }

  def insertOrUpdate(tags: Seq[me.shoma.play_cms.models.Tag]) = {
    DBIO.sequence(tags.map { current =>
      Tags.filter(_.name === current.name).result.headOption.flatMap {
        case Some(tag) => DBIO.successful(tag)
        case None => Tags.returning(Tags) += DBTag(0, current.name)
      }
    })
  }

  def sync(postId: Long, postTags: Seq[DBTag]) = {
    for {
      _ <- DBIO.seq(PostTags.filter(_.postId === postId).delete)
      _ <- DBIO.seq(postTags.map { c => PostTags += DBPostTag(postId = postId, tagId = c.id)}: _*)
    } yield ()
  }

  // --------------------------------------------------------------------------
  // Post - Tag
  // --------------------------------------------------------------------------
  case class DBPostTag(postId: Long, tagId: Long)

  class PostTag(tag: Tag) extends Table[DBPostTag](tag, "post_tag") {

    def postId = column[Long]("post_id")
    def tagId = column[Long]("tag_id")

    def * = (postId, tagId) <> (DBPostTag.tupled, DBPostTag.unapply _)
  }

  val PostTags = TableQuery[PostTag]
}
