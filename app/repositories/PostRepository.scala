package me.shoma.ayumi.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.ayumi.model._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list(page: Int = 1, perPage: Int = 10) = {
    val offset = perPage * (page - 1)
    Posts
      .filter(_.deletedAt.isEmpty)
      .sortBy(_.id.desc)
      .drop(offset)
      .take(perPage)
      .to[List].result
  }

  def find(id: Long) = {
    Posts
      .filter(_.deletedAt.isEmpty)
      .filter(_.id === id)
      .result.headOption
  }

  def save(post: Post) = {

    val dbPost = DBPost(
      post.id,
      post.title,
      post.content,
      post.createdAt.toInstant.getEpochSecond,
      post.updatedAt.toInstant.getEpochSecond,
      post.postedAt.toInstant.getEpochSecond,
      post.deletedAt
    )

    Posts.returning(Posts).insertOrUpdate(dbPost)
  }

  def softDelete(id: Long) =
    Posts
      .filter(_.id === id)
      .map(_.deletedAt)
      .update(Some(ZonedDateTime.now.toEpochSecond))


  def forceDelete(id: Long) =
    Posts.filter(_.id === id).delete

  def total = {
    Posts.filter(_.deletedAt.isEmpty)
      .filter(_.postedAt < ZonedDateTime.now.toEpochSecond)
      .length.result
  }
}
