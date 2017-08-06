package me.shoma.play_cms.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}
import javax.inject.Inject

import me.shoma.play_cms.models.{Category, Post}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def list(page: Int = 0, pageSize: Int = 10): Future[List[Post]] = {

    val offset = pageSize * page

    val action = (for {
      query <- slickPosts.sortBy(_.id.desc).drop(offset).take(pageSize).to[List].result
      postCategory <- categoriesQuery(query.map(_.id)).result
      postTag <- tagsQuery(query.map(_.id)).result
    } yield (query, postCategory, postTag)).transactionally

    db.run(action).map { resultOption =>
      resultOption._1.map {
        case (post) =>
          Post(
            post.id,
            post.title,
            post.content,
            resultOption._2.filter(_._1.postId == post.id).map(_._2).map { c => Category(Option(c.get.id), c.get.name) },
            resultOption._3.filter(_._1.postId == post.id).map(_._2).map { t => me.shoma.play_cms.models.Tag(Option(t.get.id), t.get.name) },
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.createdAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.updatedAt), ZoneId.systemDefault())
          )
      }
    }
  }

  def find(id: Long): Future[Option[Post]] = {
    val query = for {
      dbPost <- slickPosts.filter(_.id === id)
      dbCategories <- categoriesQuery(id)
      dbTags <- tagsQuery(id)
    } yield (dbPost, dbCategories, dbTags)

    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (post, categories, tags) =>
          Post(
            post.id,
            post.title,
            post.content,
            categories._2.map { c => Category(Option(c.id), c.name) } toSeq,
            tags._2.map { t => me.shoma.play_cms.models.Tag(Option(t.id), t.name) } toSeq,
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.createdAt), ZoneId.systemDefault()),
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(post.updatedAt), ZoneId.systemDefault())
          )
      }
    }
  }

  def save(post: Post): Future[Post] = {

    val dbPost = DBPost(post.id, post.title, post.content, post.createdAt.toInstant.getEpochSecond, post.updatedAt.toInstant.getEpochSecond)
    val dbCategories = post.categories.map { c => DBCategory(c.id.getOrElse(0), c.name) }
    val dbTags = post.tags.map { t => DBTag(t.id.getOrElse(0), t.name) }

    // combine database actions to be run sequentially
    val actions = (for {
      actionPost <- slickPosts.returning(slickPosts).insertOrUpdate(dbPost)

      actionCategory <- DBIO.sequence(dbCategories.map { c => slickCategories.returning(slickCategories).insertOrUpdate(c) })
      actionTag <- DBIO.sequence(dbTags.map { t => slickTags.returning(slickTags).insertOrUpdate(t) })

      // Delete intermediate tables
      _ <- DBIO.seq(slickPostCategories.filter(_.postId === actionPost.getOrElse(dbPost).id).delete)
      _ <- DBIO.seq(slickPostTags.filter(_.postId === actionPost.getOrElse(dbPost).id).delete)

      // Assign intermediate tables - Category
      _ <- DBIO.seq(actionCategory.filter(_.nonEmpty).map { c => slickPostCategories += DBPostCategory(postId = actionPost.getOrElse(dbPost).id, categoryId = c.get.id) }: _*)
      _ <- DBIO.seq(dbCategories.filter(_.id > 0).map { c => slickPostCategories += DBPostCategory(postId = actionPost.getOrElse(dbPost).id, categoryId = c.id) }: _*)

      // Assign intermediate tables - Tag
      _ <- DBIO.seq(actionTag.filter(_.nonEmpty).map { t => slickPostTags += DBPostTag(postId = actionPost.getOrElse(dbPost).id, tagId = t.get.id) }: _*)
      _ <- DBIO.seq(dbTags.filter(_.id > 0).map { t => slickPostTags += DBPostTag(postId = actionPost.getOrElse(dbPost).id, tagId = t.id) }: _*)
    } yield (actionPost.getOrElse(dbPost).id, actionCategory, actionTag)).transactionally
    // run actions and return user afterwards
    db.run(actions).map {
        case (postId, newCategories, newTags) => {
          // update categories
          val categories = post.categories.map {
            case n if n.id.isEmpty => n
            case n => {
              val cat = newCategories.filter(_.nonEmpty).filter(_.get.name == n.name).head
              n.copy(id = Option(cat.get.id))
            }
          }
          // update tags
          val tags = post.tags.map {
            case n if n.id.isEmpty => n
            case n => {
              val tag = newTags.filter(_.nonEmpty).filter(_.get.name == n.name).head
              n.copy(id = Option(tag.get.id))
            }
          }
          // return updated new post
          post.copy(id = postId, categories = categories, tags = tags)
        }
    }
  }

  def categoriesQuery(postId: Long) = slickPostCategories.filter(_.postId === postId).joinLeft(slickCategories).on(_.categoryId === _.id)

  def categoriesQuery(ids: Seq[Long]) = slickPostCategories.filter(_.postId.inSet(ids)).joinLeft(slickCategories).on(_.categoryId === _.id)

  def tagsQuery(postId: Long) = slickPostTags.filter(_.postId === postId).joinLeft(slickTags).on(_.tagId === _.id)

  def tagsQuery(ids: Seq[Long]) = slickPostTags.filter(_.postId.inSet(ids)).joinLeft(slickTags).on(_.tagId === _.id)

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
