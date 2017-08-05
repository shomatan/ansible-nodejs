package me.shoma.play_cms.models

import java.time.ZonedDateTime

case class Post(
                 id: Long = 0,
                 title: String,
                 content: String,
                 categories: Seq[Category],
                 tags: Seq[Tag],
                 createdAt: ZonedDateTime = ZonedDateTime.now(),
                 updatedAt: ZonedDateTime = ZonedDateTime.now(),
               )


case class Category(id: Long = 0, name: String)

case class Tag(id: Long = 0, name: String)
