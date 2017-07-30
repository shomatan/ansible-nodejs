package me.shoma.play_cms.models

import java.time.ZonedDateTime

case class Post(
                 id: Long,
                 title: String,
                 content: String,
                 createdAt: ZonedDateTime = ZonedDateTime.now(),
                 updatedAt: ZonedDateTime
               )


case class Category(id: Long, name: String)

case class Tag(id: Long, name: String)
