package me.shoma.ayumi.model

import java.time.ZonedDateTime

case class Post(
                 id: Long = 0,
                 title: String,
                 content: String,
                 categories: Seq[Category],
                 tags: Seq[Tag],
                 customFields: Seq[CustomField],
                 createdAt: ZonedDateTime = ZonedDateTime.now(),
                 updatedAt: ZonedDateTime = ZonedDateTime.now(),
                 postedAt: ZonedDateTime,
                 deletedAt: Option[Long] = None
               )


case class Category(id: Option[Long] = None, name: String)

case class Tag(id: Option[Long] = None, name: String)