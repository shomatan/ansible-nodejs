package me.shoma.ayumi.utils.authentication

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import me.shoma.ayumi.repositories.UserIdentity

/** The default Silhouette Environment.
  */
trait DefaultEnv extends Env {

  /** Identity
    */
  type I = UserIdentity

  /** Authenticator used for identification.
    *  [[BearerTokenAuthenticator]] could've also been used for REST.
    */
  type A = JWTAuthenticator

}
