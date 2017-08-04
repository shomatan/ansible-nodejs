package me.shoma.play_cms.repositories

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import me.shoma.play_cms.models.User
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield (dbUser)
    db.run(userQuery.result.headOption).map { resultOption =>
      resultOption.map {
        case (user) =>
          User(
            UUID.fromString(user.id),
            loginInfo,
            user.firstName,
            user.lastName,
            user.email,
            None,
            user.createdAt,
            user.updatedAt
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
  def find(userID: UUID): Future[Option[User]] = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID.toString)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
      dbPasswordInfo <- slickPasswordInfos.filter(_.loginInfoId === dbLoginInfo.id)
    } yield (dbUser, dbLoginInfo, dbPasswordInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (user, loginInfo, passwordInfo) =>
          User(
            UUID.fromString(user.id),
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.firstName,
            user.lastName,
            user.email,
            Some(PasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)),
            user.createdAt,
            user.updatedAt
          )
      }
    }
  }

  def find: Future[List[User]] = {
    val query = for {
      dbUser <- slickUsers
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
      dbPasswordInfo <- slickPasswordInfos.filter(_.loginInfoId === dbLoginInfo.id)
    } yield  (dbUser, dbLoginInfo, dbPasswordInfo)
    db.run(query.to[List].result).map { resultOption =>
      resultOption.map {
        case (user, loginInfo, passwordInfo) =>
          User(
            UUID.fromString(user.id),
            LoginInfo(loginInfo.providerID, loginInfo.providerKey),
            user.firstName,
            user.lastName,
            user.email,
            Some(PasswordInfo(passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)),
            user.createdAt,
            user.updatedAt
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
  def save(user: User): Future[User] = {
    val dbUser = DBUser(user.id.toString, user.firstName, user.lastName, user.email, user.createdAt, user.updatedAt)
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
