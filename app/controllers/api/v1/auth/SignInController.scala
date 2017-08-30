package me.shoma.ayumi.controllers.api.v1.auth

import javax.inject.Inject

import net.ceedubs.ficus.Ficus._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import me.shoma.ayumi.services.UserService
import me.shoma.ayumi.utils.authentication.DefaultEnv
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject() (
                                   cc: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   userService: UserService,
                                   credentialsProvider: CredentialsProvider,
                                   configuration: Configuration,
                                   clock: Clock)(implicit exec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  implicit val jsonFormat = Json.format[SignInData]

  case class SignInData(
                         email: String,
                         password: String,
                         rememberMe: Boolean
                       )

  def submit = Action.async(parse.json) { implicit request =>

    request.body.validate[SignInData].fold(
      errors => {
        Future.successful(Unauthorized(Json.obj("message" -> "invalid.data", "errors" -> JsError.toJson(errors))))
      },
      data => {
        credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
              case authenticator if data.rememberMe =>
                val c = configuration.underlying
                authenticator.copy(
                  expirationDateTime = clock.now.plusDays(c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry").toDays.toInt),
                  idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
                )
              case authenticator => authenticator
            }.flatMap { authenticator =>
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              silhouette.env.authenticatorService.init(authenticator).map { token =>
                Ok(Json.obj("token" -> token))
              }
            }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Unauthorized(Json.obj("message" -> "invalid.credentials"))
        }
      })
  }
}
