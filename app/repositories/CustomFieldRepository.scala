package me.shoma.play_cms.repositories

import javax.inject.Inject

import me.shoma.play_cms.models.{BigDecimalCustomField, CustomField, IntCustomField, StringCustomField}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global

class CustomFieldRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def findByPost(postId: Long) = {
    CustomFields.filter(_.postId === postId).to[List].result
  }

  def findByPost(postIds: Seq[Long]) = {
    CustomFields.filter(_.postId.inSet(postIds)).to[List].result
  }

  def sync(postId: Long, customFields: Seq[CustomField]) = {

    println(customFields)
    for {
      _ <- DBIO.seq(CustomFields.filter(_.postId === postId).delete)
      _ <- DBIO.sequence(customFields.map { current =>
        CustomFields.filter(_.postId === postId).filter(_.key === current.key).result.headOption.flatMap {
          case Some(cf) => println("succes"); DBIO.successful(cf)
          case None => println(current.postId); CustomFields.returning(CustomFields) += DBCustomField(
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

  // --------------------------------------------------------------------------
  // Table query definitions
  // --------------------------------------------------------------------------
  case class DBCustomField(postId: Long, key: String, value: String, customFieldType: Int)

  class CustomFields(tag: Tag) extends Table[DBCustomField](tag, "post_custom_fields") {

    def postId = column[Long]("post_id", O.PrimaryKey)
    def key = column[String]("key_name", O.PrimaryKey)
    def value = column[String]("value")
    def valueType = column[Int]("value_type")

    def * = (postId, key, value, valueType) <> (DBCustomField.tupled, DBCustomField.unapply _)
  }

  val CustomFields = TableQuery[CustomFields]
}
