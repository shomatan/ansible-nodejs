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

  def save(post: Post): Future[Post] = {

    val dbPost = DBPost(
      post.id,
      post.title,
      post.content,
      post.createdAt.toInstant.getEpochSecond,
      post.updatedAt.toInstant.getEpochSecond,
      post.postedAt.toInstant.getEpochSecond
    )

    // combine database actions to be run sequentially
    val actions = (for {
      actionPost <- Posts.returning(Posts).insertOrUpdate(dbPost)

      // Find categories
      actionCategory <- DBIO.sequence(post.categories.map { current =>
        slickCategories.filter(_.name === current.name).result.headOption.flatMap {
          case Some(category) => DBIO.successful(category)
          case None => slickCategories.returning(slickCategories) += DBCategory(0, current.name)
        }
      })

      // Find tags
      actionTag <- DBIO.sequence(post.tags.map { current =>
        slickTags.filter(_.name === current.name).result.headOption.flatMap {
          case Some(tag) => DBIO.successful(tag)
          case None => slickTags.returning(slickTags) += DBTag(0, current.name)
        }
      })

      // Delete intermediate tables
      _ <- DBIO.seq(PostCategories.filter(_.postId === actionPost.getOrElse(dbPost).id).delete)
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

  def customFieldQuery(ids: Seq[Long]) = CustomFields.filter(_.postId.inSet(ids)).joinLeft(CustomFields)

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
  val Posts = TableQuery[Posts]
  val PostCategories = TableQuery[PostCategory]
  val slickPostTags = TableQuery[PostTag]
}
