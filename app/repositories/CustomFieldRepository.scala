package me.shoma.play_cms.repositories

import javax.inject.Inject

import me.shoma.play_cms.models.{BigDecimalCustomField, CustomField, IntCustomField, StringCustomField}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global

class CustomFieldRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def findByPost(postId: Long) = {
    CustomFields.filter(_.postId === postId).joinLeft(CustomFields).to[List].result
  }

  def findByPost(postIds: Seq[Long]) = {
    CustomFields.filter(_.postId.inSet(postIds)).joinLeft(CustomFields).to[List].result
  }

  def sync(postId: Long, customFields: Seq[CustomField]) = {
    for {
      _ <- DBIO.seq(CustomFields.filter(_.postId === postId).delete)
      _ <- DBIO.sequence(customFields.map { current =>
        CustomFields.filter(_.postId === postId).filter(_.key === current.key).result.headOption.flatMap {
          case Some(cf) => DBIO.successful(cf)
          case None => CustomFields.returning(CustomFields) += DBCustomField(
            postId,
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
    } yield ()
  }
}
