package me.shoma.ayumi.controllers.api.v1

import javax.inject.Inject
import java.util.Date

import scala.collection.JavaConverters._
import play.api.mvc._
import com.rometools.rome.feed.synd._
import com.rometools.rome.io.SyndFeedOutput
import me.shoma.ayumi.model.Setting
import me.shoma.ayumi.services.{PostService, SettingService}

import scala.concurrent.{ExecutionContext, Future}

class RssController @Inject()( cc: ControllerComponents,
                               postService: PostService,
                               settingService: SettingService)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  case class RssSetting(title: String, url: String, description: String)

  def feed = Action.async { implicit request =>

    val feed = for {
      settings <- settingService.all()
      feed <- createFeed(settings)
    } yield (feed)

    feed.map { f =>
      val output = new SyndFeedOutput
      val xml = output.outputString(f)
      Ok(xml)
    }
  }

  def createFeed(settings: List[Setting]): Future[SyndFeed] = {

    val feed = new SyndFeedImpl
    feed.setFeedType("rss_2.0")

    val url = settings.find(_.key == Setting.url) match {
      case Some(s) => s.value.toString
      case _ => ""
    }

    feed.setLink(url)

    settings.find(_.key == Setting.title) match {
      case Some(s) => feed.setTitle(s.value.toString)
      case _ => feed.setTitle("")
    }

    settings.find(_.key == Setting.description) match {
      case Some(s) => feed.setDescription(s.value.toString)
      case _ => feed.setDescription("")
    }

    postService.list(page = 1, perPage = 20).map { result =>
      val entries = result.posts.map { p =>
        val entry = new SyndEntryImpl()
        settings.find(_.key == Setting.permalink) match {
          case Some(s) => entry.setLink(s"$url${s.value.toString}${p.id}")
          case _ => entry.setLink(s"$url/${p.id}")
        }
        val categories = p.categories.map { c =>
          val category = new SyndCategoryImpl()
          category.setName(c.name)
          category.asInstanceOf[SyndCategory]
        }
        entry.setCategories(categories.asJava)
        entry.setTitle(p.title)
        entry.setPublishedDate(Date.from(p.postedAt.toInstant))
        entry.asInstanceOf[SyndEntry]
      }

      feed.setEntries(entries.asJava)
      feed
    }
  }
}




