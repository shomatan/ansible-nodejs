package me.shoma.ayumi.repositories

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.api.util.PasswordInfo
import me.shoma.ayumi.model.User
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class UserIdentity(user: User,
                        loginInfo: LoginInfo,
                        passwordInfo: Option[PasswordInfo]) extends Identity

class UserRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[UserIdentity]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield (dbUser)
    db.run(userQuery.result.headOption).map { resultOption =>
      resultOption.map {
        case (user) =>
          UserIdentity(
            User(
              UUID.fromString(user.id),
              user.firstName,
              user.lastName,
              user.email,
              user.createdAt,
              user.updatedAt
            ),
            loginInfo,
            None
          )
      }
    }
  }

  /**
    * Finds a user by its user ID.
    *
    * @param userID The ID of the user to find.
    * @return The found user or None if no user for the given ID could be found.
    */
  def find(userID: UUID): Future[Option[UserIdentity]] = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID.toString)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
      dbPasswordInfo <- slickPasswordInfos.filter(_.loginInfoId === dbLoginInfo.id)
    } yield (dbUser, dbLoginInfo, dbPasswordInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo, passwordInfo) =>
          UserIdentity(
            User(
              UUID.fromString(user.id),
              user.firstName,
              user.lastName,
              user.email,
              user.createdAt,
              user.updatedAt
            ),
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            Some(PasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt))
          )
      }
    }
  }

  def find: Future[List[UserIdentity]] = {
    val query = for {
      dbUser <- slickUsers
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
      dbPasswordInfo <- slickPasswordInfos.filter(_.loginInfoId === dbLoginInfo.id)
    } yield  (dbUser, dbLoginInfo, dbPasswordInfo)
    db.run(query.to[List].result).map { resultOption =>
      resultOption.map {
        case (user, loginInfo, passwordInfo) =>
          UserIdentity(
            User(
              UUID.fromString(user.id),
              user.firstName,
              user.lastName,
              user.email,
              user.createdAt,
              user.updatedAt
            ),
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            Some(PasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt))
          )

      }
    }
  }

  /**
    * Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: UserIdentity): Future[UserIdentity] = {
    val dbUser = DBUser(user.user.id.toString, user.user.firstName, user.user.lastName, user.user.email, user.user.createdAt, user.user.updatedAt)
    val dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.
    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === user.loginInfo.providerID &&
          info.providerKey === user.loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }
    // combine database actions to be run sequentially
    val actions = (for {
      _ <- slickUsers.insertOrUpdate(dbUser)
      loginInfo <- loginInfoAction
      _ <- slickUserLoginInfos += DBUserLoginInfo(dbUser.id, loginInfo.id.get)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }
}
