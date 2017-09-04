package me.shoma.ayumi.repositories

import javax.inject.Inject

import me.shoma.ayumi.model._
import play.api.db.slick.DatabaseConfigProvider

class SettingRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def find(key: String) = Settings.filter(_.key === key).result.headOption

  def all() = Settings.to[List].result

  def update(setting: Setting) = {
    val dbSetting = DBSetting(
      key = setting.key,
      value = setting.value.toString,
      valueType = setting.value match {
        case v: Int => IntType.typeId
        case v: BigDecimal => BigDecimalType.typeId
        case v: Boolean => BoolType.typeId
        case v => StringType.typeId
      }
    )
    Settings.returning(Settings).insertOrUpdate(dbSetting)
  }

  // --------------------------------------------------------------------------
  // Table query definitions
  // --------------------------------------------------------------------------
  case class DBSetting(key: String, value: String, valueType: Int)

  class Settings(tag: Tag) extends Table[DBSetting](tag, "settings") {

    def key = column[String]("setting_name", O.PrimaryKey)
    def value = column[String]("value")
    def valueType = column[Int]("value_type")

    def * = (key, value, valueType) <> (DBSetting.tupled, DBSetting.unapply _)
  }

  val Settings = TableQuery[Settings]
}
