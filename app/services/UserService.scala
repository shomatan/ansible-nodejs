package me.shoma.play_cms.services

import scala.concurrent.Future
import com.mohiva.play.silhouette.api.services.IdentityService
import me.shoma.play_cms.models.User

/**
  * Handles actions to users.
  */
trait UserService extends IdentityService[User] {

  /** Saves a user.
    *
    *  @param user The user to save.
    *  @return The saved user.
    */
  def save(user: User): Future[User]
}