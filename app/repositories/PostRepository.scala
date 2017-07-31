package me.shoma.play_cms.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.play_cms.models.Post
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends RepositorySlick {

  import profile.api._
  
  case class DBPost(
                     id: Long,
                     title: String,
                     content: String,
                     createdAt: ZonedDateTime,
                     updatedAt: ZonedDateTime
                   )

  class Posts(tag: Tag) extends Table[DBPost](tag, "posts") {

    implicit val dateColumnType = MappedColumnType.base[ZonedDateTime, Long](d => d.toInstant.getEpochSecond, d => ZonedDateTime.ofInstant(Instant.ofEpochSecond(d), ZoneId.systemDefault()))

    def id = column[Long]("post_id", O.PrimaryKey)
    def title = column[String]("title")
    def content = column[String]("content")
    def createdAt = column[ZonedDateTime]("created_at")
    def updatedAt = column[ZonedDateTime]("updated_at")

    def * = (id, title, content, createdAt, updatedAt) <> (DBPost.tupled, DBPost.unapply _)
  }

  case class DBCategory(id: Long, name: String)

  class Categories(tag: Tag) extends Table[DBCategory](tag, "categories") {

    def id = column[Long]("category_id", O.PrimaryKey)
    def name = column[String]("name")

    def * = (id, name) <> (DBCategory.tupled, DBCategory.unapply _)
  }

  case class DBTag(id: Long, name: String)

  class Tags(tag: Tag) extends Table[DBTag](tag, "tags") {

    def id = column[Long]("tag_id", O.PrimaryKey)
    def name = column[String]("name")

    def * = (id, name) <> (DBTag.tupled, DBTag.unapply _)
  }

  // --------------------------------------------------------------------------
  // Table query definitions
  // --------------------------------------------------------------------------
  val slickPosts = TableQuery[Posts]
  val slickCategories = TableQuery[Categories]
  val slickTags = TableQuery[Tags]
}
