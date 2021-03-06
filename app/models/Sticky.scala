package models

import java.sql._
import java.util.Calendar

import scala.concurrent.Future
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class Sticky(id: Option[Long], bid: Long, name: Option[String], content: String, xPos: Option[Int], yPos: Option[Int], creationTime: Timestamp, lastModified: Option[Timestamp])

class Stickies(tag: Tag) extends Table[Sticky](tag, "stickies") {
	def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
	def bid = column[Long]("board_id")
	def name = column[String]("name", O.Default(""))
	def content = column[String]("content")
	def xPos = column[Int]("x", O.Default(0))
	def yPos = column[Int]("y", O.Default(0))
	def creationTime = column[Timestamp]("creation_time")
	def lastModified = column[Option[Timestamp]]("last_modified", O.Default(None))

	def * = (id.?, bid, name.?, content, xPos.?, yPos.?, creationTime, lastModified) <> ((Sticky.apply _).tupled, Sticky.unapply)
	def board = foreignKey("b_fk", bid, Board.boards)(_.id, onDelete=ForeignKeyAction.Cascade)
}

/*
 * This acts as the data access layer.
 */
object Sticky {
	val stickies = TableQuery[Stickies]

	def create(sticky: Sticky) = {
		val calendar : Calendar = Calendar.getInstance()
		val now : java.util.Date = calendar.getTime()
 		val action = stickies.map(s => (s.bid, s.content, s.creationTime)) += (sticky.bid, sticky.content, new Timestamp(now.getTime()))

 		Global.db.run(action)
	}

 	def findAll() : Future[Seq[Sticky]] = {
 		val query = stickies

 		val result : Future[Seq[Sticky]] = Global.db.run(query.result)
 		result
 	}

 	def findById(id: Long) : Future[Sticky] = {
 		val query = stickies.filter(_.id === id)

 		val result : Future[Sticky] = Global.db.run(query.result.head)
 		result
 	}

 	def findByBid(bid: Long) : Future[Seq[Sticky]] = {
 		val query = stickies.filter(_.bid === bid)

 		val result : Future[Seq[Sticky]] = Global.db.run(query.result)
 		result
 	}

 	// Maybe combine this one with the one below
 	def updateName(id: Long, name: String) = {
 		val action = stickies.filter(_.id === id)
 							 .map(b => b.name)
							 .update(name)

 		Global.db.run(action)
 	}

 	def updateContent(id: Long, content: String) = {
 		val action = stickies.filter(_.id === id)
 							 .map(b => b.content)
 							 .update(content)

 		Global.db.run(action)
 	}

 	def delete(id: Long) = {
 		val action = stickies.filter(_.id === id)
 							 .delete

 		Global.db.run(action)
 	}
}