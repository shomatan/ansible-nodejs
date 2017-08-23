package me.shoma.play_cms.models

sealed abstract class FieldType(val typeId: Int)

case object StringField extends FieldType(0)
case object IntField extends FieldType(1)
case object BoolField extends FieldType(2)