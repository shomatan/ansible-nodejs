package me.shoma.play_cms.models

// sealed trait CustomField

case class CustomField(postId: Long, key: String, value: Any)

//final case class CustomFieldString(postId: Long, key: String, value: String)
//final case class CustomFieldInt(postId: Long, key: String, value: Int)

sealed abstract class CustomFieldType(val typeId: Int)

case object StringCustomField extends CustomFieldType(0)
case object IntCustomField extends CustomFieldType(1)
