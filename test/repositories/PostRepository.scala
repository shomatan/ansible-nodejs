package repositories

import java.time.ZonedDateTime

import me.shoma.ayumi.models.{Category, CustomField, Post, Tag}
import me.shoma.ayumi.repositories.PostRepository
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

//    "insert a row with 1 category and 1 tag" in new WithApplication() {
//      val categories = Seq(Category(name = "post1: category"))
//      val tags = Seq(Tag(name = "post1: tag"))
//      val customFields = Seq(CustomField(key = "string test", value = "is string"), CustomField(key = "integer test", value = 9282))
//      val post = Post(
//        title = "play-cms title",
//        content = "play-cms content",
//        categories = categories,
//        tags = tags,
//        customFields = customFields,
//        postedAt = ZonedDateTime.now())
//      val savedPost = await(postRepo.save(post))
//
//      savedPost.id mustBe 1
//      savedPost.title mustBe "play-cms title"
//      savedPost.content mustBe "play-cms content"
//      savedPost.categories.length mustBe 1
//      savedPost.tags.length mustBe 1
//    }

//    "insert a row with array tags and categories" in new WithApplication() {
//      val categories = Seq(Category(name = "cat - A"), Category(name = "cat - B"))
//      val tags = Seq(Tag(name = "tag - A"), Tag(name = "tag - B"))
//      val post = Post(
//        title = "postID 2",
//        content = "with categories and tags",
//        categories = categories,
//        tags = tags,
//        customFields = Seq.empty[CustomField],
//        postedAt = ZonedDateTime.now())
//      val savedPost = await(postRepo.save(post))
//
//      savedPost.id mustBe 2
//      savedPost.categories.length mustBe 2
//      savedPost.tags.length mustBe 2
//    }

//    "edit a post" in new WithApplication() {
//      val p = await(postRepo.find(2)).get
//
//      p.categories.length mustBe 2
//      p.tags.length mustBe 2
//
//      val categories = Seq(Category(name = "cat - A"), Category(name = "cat - B"), Category(name = "cat - C"))
//      val tags = Seq(Tag(name = "tag - A"), Tag(name = "tag - B update"))
//
//      val post = p.copy(title = "updated title", content = "updated content", categories = categories, tags = tags)
//      val savedPost = await(postRepo.save(post))
//
//      savedPost.id mustBe 2
//      savedPost.title mustBe "updated title"
//      savedPost.content mustBe "updated content"
//      savedPost.categories.length mustBe 3
//      savedPost.tags.length mustBe 2
//      savedPost.tags.contains(Tag(name = "tag - B update")) mustBe true
//      savedPost.tags.contains(Tag(name = "tag - B")) mustBe false
//    }

//    "get all rows and satisfy the specifications" in new WithApplication() {
//      val posts = await(postRepo.list())
//
//      posts.length mustBe 2
//      // sorted
//      posts(0).id mustBe 2
//    }

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
