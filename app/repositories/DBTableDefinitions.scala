package me.shoma.play_cms.repositories

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.mohiva.play.silhouette.api.LoginInfo
import slick.jdbc.JdbcProfile

trait DBTableDefinitions {

  protected val driver: JdbcProfile

  import driver.api._

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
  // Table query definitions
  // --------------------------------------------------------------------------
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)


}
