package me.shoma.ayumi.services

import scala.concurrent.Future
import com.mohiva.play.silhouette.api.services.IdentityService
import me.shoma.ayumi.repositories.UserIdentity

/**
  * Handles actions to users.
  */
trait UserService extends IdentityService[UserIdentity] {

  /** Saves a user.
    *
    *  @param user The user to save.
    *  @return The saved user.
    */
  def save(user: UserIdentity): Future[UserIdentity]
}