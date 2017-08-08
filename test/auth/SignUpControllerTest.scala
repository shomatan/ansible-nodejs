package auth

import me.shoma.play_cms.controllers.api.v1.auth.SignUpController
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.libs.json.Json
import play.api.test.{ FakeRequest, WithApplication}
import play.api.test.Helpers._

class SignUpControllerTest extends PlaySpec with BeforeAndAfterAll with Results {

  lazy val appBuilder = new GuiceApplicationBuilder()

  lazy val injector = appBuilder.injector()

  lazy val databaseApi = injector.instanceOf[DBApi]

  val controller = appBuilder.injector.instanceOf[SignUpController]

  override def afterAll() = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
  }

  "SignUpController" must {

    "sign up new user" in new WithApplication() {

      val jsonStr = """{ "firstName": "test", "lastName": "name", "email": "aaa@example.com", "password": "aaa" }"""
      val request = FakeRequest(POST, "/api/v1/auth/signUp").withJsonBody(Json.parse(jsonStr))
      val result = call(controller.submit(), request)

      status(result) mustBe 200
      contentAsString(result) contains "token"
    }

    "sign up fail when email address exist" in new WithApplication() {
      val jsonStr = """{ "firstName": "test", "lastName": "name", "email": "aaa@example.com", "password": "aaa" }"""
      val request = FakeRequest(POST, "/api/v1/auth/signUp").withJsonBody(Json.parse(jsonStr))
      val result = call(controller.submit(), request)

      status(result) mustBe 400
      contentAsString(result) contains "The E-mail address exists."
    }
  }
}
