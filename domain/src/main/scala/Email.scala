package me.shoma.ayumi.model

case class Email(
                title: String,
                from: String,
                to: Seq[String],
                bodyText: Option[String],
                bodyHtml: Option[String]
                )
