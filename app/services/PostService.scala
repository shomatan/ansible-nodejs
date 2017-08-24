package services

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.play_cms.models._
import me.shoma.play_cms.repositories._

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostService @Inject() (postRepository: PostRepository,
                             categoryRepository: CategoryRepository,
                             tagRepository: TagRepository,
                             customFieldRepository: CustomFieldRepository) {

  val db = Database.forConfig("db.Default")

  def list(page: Int = 0, pageSize: Int = 10): Future[List[Post]] = {

    val action = (for {
      query <- postRepository.list(page, pageSize)
      categories <- categoryRepository.findByPost(query.map(_.id))
      postTag <- tagRepository.findByPost(query.map(_.id))
      postCustomField <- customFieldRepository.findByPost(query.map(_.id))
    } yield (query, categories, postTag, postCustomField)).transactionally

    db.run(action).map { resultOption =>
      resultOption._1.map {
        case (post) =>
          Post(
            post.id,
            post.title,
            post.content,
            resultOption._2.filter(_._1.postId == post.id).map(_._2).map { c => Category(Option(c.get.id), c.get.name) },
            resultOption._3.filter(_._1.postId == post.id).map(_._2).map { t => me.shoma.play_cms.models.Tag(Option(t.get.id), t.get.name) },
            resultOption._4.filter(_._1.postId == post.id).map(_._2).map { cf =>
              CustomField(
                post.id,
                cf.get.key,
                cf.get.customFieldType match {
                  case StringCustomField.typeId => cf.get.value.toString
                  case IntCustomField.typeId => cf.get.value.toInt
                }
              )
            },
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.createdAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.updatedAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.postedAt), ZoneId.systemDefault())
          )
      }
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
          tags.map { t => me.shoma.play_cms.models.Tag(Option(t._2.get.id), t._2.get.name) },
          customFields.map { c =>
            val cf = c._1
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
          ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.postedAt), ZoneId.systemDefault())
        ))
      }
      case _ => None
    }
  }

  def save(post: Post): Future[Post] = {

    val actions = (for {
      actionPost <- postRepository.save(post)

      // Find categories
      actionCategory <- categoryRepository.insertOrUpdate(post.categories)

      // Find tags
      actionTag <- tagRepository.insertOrUpdate(post.tags)

      // Delete intermediate tables
      _ <-
      _ <- DBIO.seq(slickPostTags.filter(_.postId === actionPost.getOrElse(dbPost).id).delete)
      _ <- DBIO.seq(CustomFields.filter(_.postId === actionPost.getOrElse(dbPost).id).delete)

      // Assign intermediate tables - Category
      _ <- DBIO.seq(actionCategory.map { c => PostCategories += DBPostCategory(postId = actionPost.getOrElse(dbPost).id, categoryId = c.id)}: _*)

      // Assign intermediate tables - Tag
      _ <- DBIO.seq(actionTag.map { c => slickPostTags += DBPostTag(postId = actionPost.getOrElse(dbPost).id, tagId = c.id)}: _*)

      // Find and insert custom fields
      actionCustomField <- DBIO.sequence(post.customFields.map { current =>
        CustomFields.filter(_.postId === actionPost.getOrElse(dbPost).id).filter(_.key === current.key).result.headOption.flatMap {
          case Some(cf) => DBIO.successful(cf)
          case None => CustomFields.returning(CustomFields) += DBCustomField(
            actionPost.getOrElse(dbPost).id,
            current.key,
            current.value.toString,
            current.value match {
              case _: Int => IntCustomField.typeId
              case _: BigDecimal => BigDecimalCustomField.typeId
              case _: String => StringCustomField.typeId
            }
          )
        }
      })
    } yield (actionPost.getOrElse(dbPost).id, actionCategory, actionTag, actionCustomField)).transactionally
    // run actions and return user afterwards
    db.run(actions).map {
      case (postId, newCategories, newTags, newCustomFields) => {
        // update categories
        val categories = post.categories.map {
          case n if n.id.isEmpty => n
          case n => {
            val cat = newCategories.filter(_.name == n.name).head
            n.copy(id = Option(cat.id))
          }
        }
        // update tags
        val tags = post.tags.map {
          case n if n.id.isEmpty => n
          case n => {
            val tag = newTags.filter(_.name == n.name).head
            n.copy(id = Option(tag.id))
          }
        }
        // update custom fields
        val customFields = post.customFields.map { cf =>
          cf.copy(postId = post.id)
        }
        // return updated new post
        post.copy(id = postId, categories = categories, tags = tags, customFields = customFields)
      }
    }
  }
}
