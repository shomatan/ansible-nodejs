package me.shoma.ayumi.services

import javax.inject.Inject

import me.shoma.ayumi.model.{BigDecimalType, IntType, Setting, StringType}
import me.shoma.ayumi.repositories.SettingRepository
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SettingService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                               (settingRepository: SettingRepository) extends HasDatabaseConfigProvider[JdbcProfile] {

  def find(key: String): Future[Option[Setting]] = {
    db.run(settingRepository.find(key)).map { resultOption =>
      resultOption.map {
        case (setting) =>
          Setting(
            key = setting.key,
            value = setting.valueType match {
              case StringType.typeId => setting.value.toString
              case IntType.typeId => setting.value.toInt
              case BigDecimalType.typeId => BigDecimal(setting.value)
            }
          )
      }
    }
  }

  def all(): Future[List[Setting]] = {
    db.run(settingRepository.all).map { resultOption =>
      resultOption.map {
        case (setting) =>
          Setting(
            key = setting.key,
            value = setting.valueType match {
              case StringType.typeId => setting.value.toString
              case IntType.typeId => setting.value.toInt
              case BigDecimalType.typeId => BigDecimal(setting.value)
            }
          )
      }
    }
  }

  def save(setting: Setting): Future[Setting] =
    db.run(settingRepository.update(setting)).map(_ => setting)
}
