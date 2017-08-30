package me.shoma.ayumi.repositories

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TagRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list: Future[List[me.shoma.ayumi.model.Tag]] = {

    val action = Tags.sortBy(_.name.asc).to[List].result

    db.run(action).map { resultOption =>
      resultOption.map {
        case (tag) =>
          me.shoma.ayumi.model.Tag(
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

  def insertOrUpdate(tags: Seq[me.shoma.ayumi.model.Tag]) = {
    DBIO.sequence(tags.map { current =>
      Tags.filter(_.name === current.name).result.headOption.flatMap {
        case Some(tag) => DBIO.successful(tag)
        case None => Tags.returning(Tags) += DBTag(0, current.name)
      }
    })
  }

  def sync(post: DBPost, postTags: Seq[DBTag]) = {
    for {
      _ <- DBIO.seq(PostTags.filter(_.postId === post.id).delete)
      _ <- DBIO.seq(postTags.map { c => PostTags += DBPostTag(postId = post.id, tagId = c.id)}: _*)
    } yield ()
  }

  // --------------------------------------------------------------------------
  // Tag
  // --------------------------------------------------------------------------
  case class DBTag(id: Long, name: String)

  class Tags(tag: Tag) extends Table[DBTag](tag, "tags") {

    def id = column[Long]("tag_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("tag")

    def * = (id, name) <> (DBTag.tupled, DBTag.unapply _)
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

  // --------------------------------------------------------------------------
  // Table query definitions
  // --------------------------------------------------------------------------
  val Tags = TableQuery[Tags]
  val PostTags = TableQuery[PostTag]
}
