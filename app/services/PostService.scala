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
}
