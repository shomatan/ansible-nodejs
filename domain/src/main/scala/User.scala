package me.shoma.ayumi.model

import java.time.ZonedDateTime
import java.util.UUID

case class User(
                 id: UUID,
                 firstName: Option[String],
                 lastName: Option[String],
                 email: Option[String],
                 createdAt: ZonedDateTime = ZonedDateTime.now(),
                 updatedAt: ZonedDateTime
               )


