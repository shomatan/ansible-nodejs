package me.shoma.play_cms.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait DBTableDefinitions extends HasDatabaseConfigProvider[JdbcProfile] {

  protected val driver: JdbcProfile

  import profile.api._

  // --------------------------------------------------------------------------
  // User
  // --------------------------------------------------------------------------
  case class DBUser(
                     id: String,
                     firstName: Option[String],
                     lastName: Option[String],
                     email: Option[String],
                     createdAt: ZonedDateTime,
                     updatedAt: ZonedDateTime
                   )

  class Users(tag: Tag) extends Table[DBUser](tag, "users") {

    implicit val dateColumnType = MappedColumnType.base[ZonedDateTime, Long](d => d.toInstant.getEpochSecond, d => ZonedDateTime.ofInstant(Instant.ofEpochSecond(d), ZoneId.systemDefault()))

    def id = column[String]("id", O.PrimaryKey)
    def firstName = column[Option[String]]("first_name")
    def lastName = column[Option[String]]("last_name")
    def email = column[Option[String]]("email")
    def createdAt = column[ZonedDateTime]("created_at")
    def updatedAt = column[ZonedDateTime]("updated_at")

    def * = (id, firstName, lastName, email, createdAt, updatedAt) <> (DBUser.tupled, DBUser.unapply _)
  }

  // --------------------------------------------------------------------------
  // Login info
  // --------------------------------------------------------------------------
  case class DBLoginInfo(
                          id: Option[Long],
                          providerID: String,
                          providerKey: String
                        )

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def * = (id.?, providerID, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  // --------------------------------------------------------------------------
  // User login info
  // --------------------------------------------------------------------------
  case class DBUserLoginInfo(
                              userID: String,
                              loginInfoId: Long
                            )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "user_login_info") {
    def userID = column[String]("user_id")
    def loginInfoId = column[Long]("login_info_id")
    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  // --------------------------------------------------------------------------
  // Passowrd info
  // --------------------------------------------------------------------------
  case class DBPasswordInfo(
                             hasher: String,
                             password: String,
                             salt: Option[String],
                             loginInfoId: Long
                           )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "password_info") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("login_info_id")
    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  // --------------------------------------------------------------------------
  // Category
  // --------------------------------------------------------------------------
  case class DBCategory(id: Long, name: String)

  class Categories(tag: Tag) extends Table[DBCategory](tag, "categories") {

    def id = column[Long]("category_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("category")

    def * = (id, name) <> (DBCategory.tupled, DBCategory.unapply _)
  }

  // --------------------------------------------------------------------------
  // Tag
  // --------------------------------------------------------------------------
  case class DBTag(id: Long, name: String)

  class Tags(tag: Tag) extends Table[DBTag](tag, "tags") {

    def id = column[Long]("tag_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("tag")

    def * = (id, name) <> (DBTag.tupled, DBTag.unapply _)
  }

  // --------------------------------------------------------------------------
  // CustomField
  // --------------------------------------------------------------------------
  case class DBCustomField(postId: Long, key: String, value: String, customFieldType: Int)

  class CustomFields(tag: Tag) extends Table[DBCustomField](tag, "post_custom_fields") {

    def postId = column[Long]("post_id", O.PrimaryKey)
    def key = column[String]("key_name", O.PrimaryKey)
    def value = column[String]("value")
    def valueType = column[Int]("value_type")

    def * = (postId, key, value, valueType) <> (DBCustomField.tupled, DBCustomField.unapply _)
  }

  // --------------------------------------------------------------------------
  // Table query definitions
  // --------------------------------------------------------------------------
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickCategories = TableQuery[Categories]
  val slickTags = TableQuery[Tags]
  val CustomFields = TableQuery[CustomFields]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)


}
