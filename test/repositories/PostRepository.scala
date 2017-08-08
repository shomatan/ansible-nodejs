package repositories

import me.shoma.play_cms.models.{Category, Post, Tag}
import me.shoma.play_cms.repositories.PostRepository
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.WithApplication

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.Future

class PostControllerSpec extends PlaySpec with BeforeAndAfterAll {

  lazy val appBuilder = new GuiceApplicationBuilder()

  lazy val injector = appBuilder.injector()

  lazy val databaseApi = injector.instanceOf[DBApi]

  override def afterAll() = {
    Evolutions.cleanupEvolutions(databaseApi.database("default"))
  }

  "Post repository" must {

    def postRepo(implicit app: Application) = Application.instanceCache[PostRepository].apply(app)

    "insert a row with empty tags and categories" in new WithApplication() {
      val post = Post(title = "play-cms title", content = "play-cms content", categories = Seq.empty[Category], tags = Seq.empty[Tag])
      val savedPost = await(postRepo.save(post))

      savedPost.id mustBe 1
      savedPost.title mustBe "play-cms title"
      savedPost.content mustBe "play-cms content"
      savedPost.categories.length mustBe 0
      savedPost.tags.length mustBe 0
    }

    "insert a row with array tags and categories" in new WithApplication() {
      val categories = Seq(Category(name = "cat - A"), Category(name = "cat - B"))
      val tags = Seq(Tag(name = "tag - A"), Tag(name = "tag - B"))
      val post = Post(title = "postID 2", content = "with categories and tags", categories = categories, tags = tags)
      val savedPost = await(postRepo.save(post))

      savedPost.id mustBe 2
      savedPost.categories.length mustBe 2
      savedPost.tags.length mustBe 2
    }

    "get all rows and satisfy the specifications" in new WithApplication() {
      val posts = await(postRepo.list())

      posts.length mustBe 2
      // sorted
      posts(0).id mustBe 2
    }

    //    "get single rows" in new  WithApplication()  {
    //      val result = await(empRepo.getById(1))
    //      result.isDefined === true
    //      result.get.name === "Vikas"
    //    }
    //

    //
    //    "insert multiple rows" in new  WithApplication()  {
    //      val result = empRepo.insertAll(List(Employee("sky1", "sky1@knoldus.com", "knoldus","Senior Consultant"),
    //        Employee("sky2", "sky2@knoldus.com", "knoldus","Senior Consultant")))
    //      val knolIds = await(result)
    //      knolIds === Seq(5, 6)
    //    }
    //
    //    "update a row" in new  WithApplication()  {
    //      val result = await(empRepo.update(Employee("sky", "sky@knoldus.com",  "knoldus","Senior Consultant", Some(1))))
    //      result === 1
    //    }
    //
    //    "delete a row" in new  WithApplication()  {
    //      val result = await(empRepo.delete(1))
    //      result === 1
    //    }
  }
  def await[T](v: Future[T]): T = Await.result(v, Duration.Inf)
}
