package me.shoma.ayumi.model

case class Setting(key: String, value: Any) {
  def get[A]: A = value.asInstanceOf[A]
}

object Setting {
  val title = "title"
  val url = "url"
  val description = "description"
  val permalink = "permalink"
  val feedCount = "feed-count"
}