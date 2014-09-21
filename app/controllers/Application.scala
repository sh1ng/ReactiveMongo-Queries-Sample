package controllers

import play.api._
import play.api.mvc._
import org.joda.time._
import reactivemongo.queries.Query._
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._

object Application extends Controller {
  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))
  val db = connection("reactivemongoqueries")
  val collection = db("tasks")
  
  implicit object dateTimeHandler extends BSONReader[BSONDateTime, DateTime] with BSONWriter[DateTime, BSONDateTime] {
	    def read(b: BSONDateTime) = new DateTime(b.value)
	    def write(value: DateTime) = BSONDateTime(value.getMillis)
	}
  
  implicit val taskHandler = Macros.handler[Task]
  
  case class Task(id: String, label: String, text: String, createdAt: DateTime, actionDate: DateTime)

  def index = Action.async {
    for{
      tasks <- collection.find(BSONDocument(""->"")).sort(on[Task].sortAsc(_.actionDate)).cursor[Task].collect[List]()
    } yield Ok(views.html.index(tasks))
  }

}