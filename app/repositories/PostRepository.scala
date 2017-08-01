package me.shoma.play_cms.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.play_cms.models.{Category, Post}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends RepositorySlick {

  import profile.api._

  def list(page: Int = 0, pageSize: Int = 10): Future[Option[Post]] = {
    val offset = pageSize * page
    val postQuery = (for {
      (postAndRelation, category) <- slickPosts joinLeft slickPostCategories on (_.id === _.postId) joinLeft slickCategories on (_._2.map(_.categoryId) === _.id)
      (_, tag) <- slickPosts joinLeft slickPostTags on (_.id === _.postId) joinLeft slickTags on (_._2.map(_.tagId) === _.id)
    } yield (postAndRelation._1, category, tag))
      .sortBy(rows => rows._1.updatedAt.desc)
      .drop(offset)
      .take(pageSize)
    db.run(postQuery.result.headOption).map { resultOption =>
      resultOption.map {
        case (post, category, tag) =>
          Post(
            post.id,
            post.title,
            post.content,
            category.map{ c => Category(c.id, c.name) } toList,
            tag.map{ t => me.shoma.play_cms.models.Tag(t.id, t.name) } toList,
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.createdAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.updatedAt), ZoneId.systemDefault())
          )
      }
    }
  }

  case class DBPost(
                     id: Long,
                     title: String,
                     content: String,
                     createdAt: Long,
                     updatedAt: Long
                   )

  class Posts(tag: Tag) extends Table[DBPost](tag, "posts") {

    def id = column[Long]("post_id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def content = column[String]("content")
    def createdAt = column[Long]("created_at")
    def updatedAt = column[Long]("updated_at")

    def * = (id, title, content, createdAt, updatedAt) <> (DBPost.tupled, DBPost.unapply _)
  }

  case class DBCategory(id: Long, name: String)

  class Categories(tag: Tag) extends Table[DBCategory](tag, "categories") {

    def id = column[Long]("category_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("category")

    def * = (id, name) <> (DBCategory.tupled, DBCategory.unapply _)
  }

  case class DBTag(id: Long, name: String)

  class Tags(tag: Tag) extends Table[DBTag](tag, "tags") {

    def id = column[Long]("tag_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("tag")

    def * = (id, name) <> (DBTag.tupled, DBTag.unapply _)
  }

  case class DBPostTag(postId: Long, tagId: Long)

  class PostTag(tag: Tag) extends Table[DBPostTag](tag, "post_tag") {

    def postId = column[Long]("post_id")
    def tagId = column[Long]("tag_id")

    def * = (postId, tagId) <> (DBPostTag.tupled, DBPostTag.unapply _)
  }

  case class DBPostCategory(postId: Long, categoryId: Long)

  class PostCategory(tag: Tag) extends Table[DBPostCategory](tag, "post_category") {

    def postId = column[Long]("post_id")
    def categoryId = column[Long]("category_id")

    def * = (postId, categoryId) <> (DBPostCategory.tupled, DBPostCategory.unapply _)
  }

  // --------------------------------------------------------------------------
  // Table query definitions
  // --------------------------------------------------------------------------
  val slickPosts = TableQuery[Posts]
  val slickCategories = TableQuery[Categories]
  val slickTags = TableQuery[Tags]
  val slickPostCategories = TableQuery[PostCategory]
  val slickPostTags = TableQuery[PostTag]
}
