package me.shoma.ayumi.model

sealed abstract class ValueType(val typeId: Int)

case object StringType extends ValueType(1)
case object IntType extends ValueType(2)
case object BigDecimalType extends ValueType(3)
case object BoolType extends ValueType(4)
