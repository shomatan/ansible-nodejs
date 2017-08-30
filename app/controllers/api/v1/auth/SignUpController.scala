package me.shoma.ayumi.controllers.api.v1.auth

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.i18n.Messages
import play.api.libs.json.{JsError, Json}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.{AbstractController, ControllerComponents}
import me.shoma.ayumi.models.User
import me.shoma.ayumi.services.UserService
import me.shoma.ayumi.utils.authentication.DefaultEnv

class SignUpController @Inject() (
                                   cc: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   userService: UserService,
                                   authInfoRepository: AuthInfoRepository,
                                   passwordHasher: PasswordHasher)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  implicit val jsonFormat = Json.format[SignUpData]

  case class SignUpData(
                         firstName: String,
                         lastName: String,
                         email: String,
                         password: String
                       )

  def submit = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpData].fold(
      errors => {
        Future.successful(Unauthorized(Json.obj("message" -> "invalid.data", "errors" -> JsError.toJson(errors))))
      },
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => Future.successful(BadRequest(Json.obj("message" -> "The E-mail address exists.")))
          case None =>
            val user = User(
              id = UUID.randomUUID(),
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              email = Some(data.email),
              passwordInfo = None,
              createdAt = ZonedDateTime.now(),
              updatedAt = ZonedDateTime.now()
            )

            val authInfo = passwordHasher.hash(data.password)
            for {
              user <- userService.save(user)
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- silhouette.env.authenticatorService.create(loginInfo)
              token <- silhouette.env.authenticatorService.init(authenticator)
            } yield {
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              silhouette.env.eventBus.publish(LoginEvent(user, request))
              Ok(Json.obj("token" -> token))
            }
        }
      }
    )
  }

}
