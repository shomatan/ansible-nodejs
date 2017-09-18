package me.shoma.ayumi.services

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.ayumi.model._
import me.shoma.ayumi.repositories._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class PostResult(posts: List[Post], page: Int, pageSize: Int, total: Long)

class PostService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                            (postRepository: PostRepository,
                             categoryRepository: CategoryRepository,
                             tagRepository: TagRepository,
                             customFieldRepository: CustomFieldRepository) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def list(page: Int = 0, perPage: Int = 10): Future[PostResult] = {

    val action = (for {
      query <- postRepository.list(page, perPage)
      total <- postRepository.total
      categories <- categoryRepository.findByPost(query.map(_.id))
      postTag <- tagRepository.findByPost(query.map(_.id))
      postCustomField <- customFieldRepository.findByPost(query.map(_.id))
    } yield (query, categories, postTag, postCustomField, total)).transactionally

    db.run(action).map { resultOption =>
      val posts = resultOption._1.map {
        case (post) =>
          Post(
            post.id,
            post.title,
            post.content,
            resultOption._2.filter(_._1.postId == post.id).map(_._2).map { c => Category(Option(c.get.id), c.get.name) },
            resultOption._3.filter(_._1.postId == post.id).map(_._2).map { t => Tag(Option(t.get.id), t.get.name) },
            resultOption._4.filter(_.postId == post.id).map { cf =>
              CustomField(
                post.id,
                cf.key,
                cf.customFieldType match {
                  case StringCustomField.typeId => cf.value.toString
                  case IntCustomField.typeId => cf.value.toInt
                  case BigDecimalCustomField.typeId => BigDecimal(cf.value)
                }
              )
            },
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.createdAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.updatedAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.postedAt), ZoneId.systemDefault()),
            post.deletedAt
          )
      }
      PostResult(posts, page, perPage, resultOption._5)
    }
  }

  def find(id: Long): Future[Option[Post]] = {

    val action = (for {
      post <- postRepository.find(id)
      categories <- categoryRepository.findByPost(id)
      tags <- tagRepository.findByPost(id)
      customFields <- customFieldRepository.findByPost(id)
    } yield (post, categories, tags, customFields)).transactionally

    db.run(action).map {
      case (Some(post), categories, tags, customFields) => {

        Some(Post(
          post.id,
          post.title,
          post.content,
          categories.map { c => Category(Option(c._2.get.id), c._2.get.name) },
          tags.map { t => me.shoma.ayumi.model.Tag(Option(t._2.get.id), t._2.get.name) },
          customFields.map { c =>
            CustomField(
              post.id,
              c.key,
              c.customFieldType match {
                case StringCustomField.typeId => c.value.toString
                case IntCustomField.typeId => c.value.toInt
                case BigDecimalCustomField.typeId => BigDecimal(c.value)
              }
            )
          },
          ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.createdAt), ZoneId.systemDefault()),
          ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.updatedAt), ZoneId.systemDefault()),
          ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.postedAt), ZoneId.systemDefault()),
          post.deletedAt
        ))
      }
      case _ => None
    }
  }

  def save(post: Post): Future[Post] = {

    val dbPost = DBPost(
      post.id,
      post.title,
      post.content,
      post.createdAt.toInstant.getEpochSecond,
      post.updatedAt.toInstant.getEpochSecond,
      post.postedAt.toInstant.getEpochSecond,
      post.deletedAt
    )

    val actions = (for {
      // Find Post
      actionPost <- postRepository.save(post)
      // Find categories
      actionCategory <- categoryRepository.insertOrUpdate(post.categories)
      // Find tags
      actionTag <- tagRepository.insertOrUpdate(post.tags)
      // Assign intermediate tables - Category
      _ <- categoryRepository.sync(actionPost.getOrElse(dbPost), actionCategory)
      // Assign intermediate tables - Tag
      _ <- tagRepository.sync(actionPost.getOrElse(dbPost), actionTag)
      // Assign custom fields
      _ <- customFieldRepository.sync(actionPost.getOrElse(dbPost), post.customFields)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => post)
  }

  def softDelete(id: Long) = db.run(postRepository.softDelete(id)).map(_ => id)

  def forceDelete(id: Long) = db.run(postRepository.forceDelete(id)).map(_ => id)
}
