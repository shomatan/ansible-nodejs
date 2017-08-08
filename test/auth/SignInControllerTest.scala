package auth

import me.shoma.play_cms.controllers.api.v1.auth.SignInController
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.Helpers.{POST, call, contentAsString, status}
import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._

class SignInControllerTest extends PlaySpec with BeforeAndAfterAll with Results {

  lazy val appBuilder = new GuiceApplicationBuilder()

  lazy val injector = appBuilder.injector()

  lazy val databaseApi = injector.instanceOf[DBApi]

  val controller = appBuilder.injector.instanceOf[SignInController]

  override def afterAll() = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
  }

  "SignInController" must {

    "sign in exists user" in new WithApplication() {

      val jsonStr = """{ "email": "admin@example.com", "password": "admin", "rememberMe": true }"""
      val request = FakeRequest(POST, "/api/v1/auth/signIn").withJsonBody(Json.parse(jsonStr))
      val result = call(controller.submit(), request)

      status(result) mustBe 200
      contentAsString(result) contains "token"
    }

    "sign in not exists user" in new WithApplication() {

      val jsonStr = """{ "email": "test@example.com", "password": "test", "rememberMe": false }"""
      val request = FakeRequest(POST, "/api/v1/auth/signIn").withJsonBody(Json.parse(jsonStr))
      val result = call(controller.submit(), request)

      status(result) mustBe 401
    }
  }

}
