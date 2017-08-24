package me.shoma.play_cms.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.play_cms.models._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list(page: Int = 0, pageSize: Int = 10) = {
    val offset = pageSize * page
    Posts.sortBy(_.id.desc).drop(offset).take(pageSize).to[List].result
  }

  def find(id: Long) = {
    Posts.filter(_.id === id).result.headOption
  }

  def save(post: Post) = {

    val dbPost = DBPost(
      post.id,
      post.title,
      post.content,
      post.createdAt.toInstant.getEpochSecond,
      post.updatedAt.toInstant.getEpochSecond,
      post.postedAt.toInstant.getEpochSecond
    )

    Posts.returning(Posts).insertOrUpdate(dbPost)
  }

  // --------------------------------------------------------------------------
  // Post
  // --------------------------------------------------------------------------
  case class DBPost(
                     id: Long,
                     title: String,
                     content: String,
                     createdAt: Long,
                     updatedAt: Long,
                     postedAt: Long
                   )

  class Posts(tag: Tag) extends Table[DBPost](tag, "posts") {

    def id = column[Long]("post_id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def content = column[String]("content")
    def createdAt = column[Long]("created_at")
    def updatedAt = column[Long]("updated_at")
    def postedAt = column[Long]("posted_at")

    def * = (id, title, content, createdAt, updatedAt, postedAt) <> (DBPost.tupled, DBPost.unapply _)
  }

  val Posts = TableQuery[Posts]
}
