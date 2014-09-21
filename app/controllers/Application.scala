package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import org.joda.time._
import org.joda.time.format._
import reactivemongo.queries.Query._
import reactivemongo.api._
import scala.concurrent.Future
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
  
  case class Task(id: String, name: String, label: String, createdAt: DateTime, actionDate: DateTime)
  
  val taskForm = Form(
    tuple(
      "label" -> text,
      "actionDate" -> text
    ) 
  )
  
  def allTasks(name: String) = 
    collection.find(on[Task].eq(_.name, name)).sort(on[Task].sortAsc(_.actionDate)).cursor[Task].collect[List]()

  def index = Action{
    Ok(views.html.index())
  }
  
  def tasks(name: String) = Action.async {
    for{
      tasks <- allTasks(name) 
    } yield Ok(views.html.tasks(tasks, name, taskForm))
  }
  
  def createTask(name: String) = Action.async { implicit request =>
  taskForm.bindFromRequest.fold(
    errors => allTasks(name).map(p => BadRequest(views.html.tasks(p , name, errors))),
    form => {
      val task = Task(BSONObjectID.generate.stringify, name, form._1,
          DateTime.now(), DateTime.parse(form._2, DateTimeFormat.forPattern("MM/dd/yyyy")))
      collection.insert(task).map(p=>Redirect(routes.Application.tasks(name)))
      })
  }
  
 def deleteTask(name: String, id: String) = Action.async {
   for{
     delete <- collection.remove(on[Task].eq(_.id, id))
   } yield Redirect(routes.Application.tasks(name))
 } 
}