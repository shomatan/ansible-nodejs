package me.shoma.ayumi.controllers.api.v1

import javax.inject.Inject

import play.api.libs.json.{JsError, Json}
import play.api.libs.mailer._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class EmailController @Inject()(cc: ControllerComponents,
                                mailerClient: MailerClient)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  implicit val categoryFormat = Json.format[me.shoma.ayumi.model.Email]

  def send = Action.async(parse.json) { implicit request =>

    request.body.validate[me.shoma.ayumi.model.Email].map { m =>
      val email = Email(
        m.title,
        m.from,
        m.to,
        bodyText = m.bodyText,
        bodyHtml = m.bodyHtml
      )
      mailerClient.send(email)
      Future.successful(Ok(Json.toJson(m)))
    }.recoverTotal { e =>
      Future {
        BadRequest(Json.obj("error" -> JsError.toJson(e)))
      }
    }
  }
}
