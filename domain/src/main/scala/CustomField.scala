package me.shoma.ayumi.model

// sealed trait CustomField

case class CustomField(postId: Long = 0, key: String, value: Any)

//final case class CustomFieldString(postId: Long, key: String, value: String)
//final case class CustomFieldInt(postId: Long, key: String, value: Int)

sealed abstract class CustomFieldType(val typeId: Int)

case object StringCustomField extends CustomFieldType(1)
case object IntCustomField extends CustomFieldType(2)
case object BigDecimalCustomField extends CustomFieldType(3)