package me.shoma.play_cms.repositories

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider

class CustomFieldRepository @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DBTableDefinitions {

  import profile.api._

  def findByPost(postId: Long) = {
    CustomFields.filter(_.postId === postId).joinLeft(CustomFields).to[List].result
  }

  def findByPost(postIds: Seq[Long]) = {
    CustomFields.filter(_.postId.inSet(postIds)).joinLeft(CustomFields).to[List].result
  }
}
