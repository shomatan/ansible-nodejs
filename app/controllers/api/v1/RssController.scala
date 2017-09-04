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
    val ret = for {
      feed <- createFeed()
      entry <- createEntry(feed.getLink)
    } yield (feed, entry)

    ret.map { ent =>
      ent._1.setEntries(ent._2.asJava)
      val output = new SyndFeedOutput
      val t = output.outputString(ent._1)
      Ok(t)
    }
  }

  def createFeed(): Future[SyndFeed] = {
    settingService.all().map { s =>
      val feed = new SyndFeedImpl
      feed.setFeedType("rss_2.0")

      val url = s.find(_.key == Setting.url)
      if(url.isDefined) {
        feed.setLink(url.get.value.toString)
      }

      val title = s.find(_.key == Setting.title)
      if(title.isDefined) {
        feed.setTitle(title.get.value.toString)
      }

      val description = s.find(_.key == Setting.description)
      if(description.isDefined) {
        feed.setDescription(description.get.value.toString)
      }
      feed
    }
  }

  def createEntry(url: String): Future[List[SyndEntry]] = {
    postService.list(page = 1, perPage = 20).map { result =>
      result.posts.map { p =>
        val entry = new SyndEntryImpl()
        entry.setTitle(p.title)
        entry.setLink(url + "/" + p.id)
        entry.setPublishedDate(Date.from(p.postedAt.toInstant))
        entry.asInstanceOf[SyndEntry]
        entry
      }
    }
  }
}




