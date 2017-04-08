package spatutorial.client.services

import cats.implicits._
import autowire._
import spatutorial.shared.Api
import boopickle.Default._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


object AppAction {
  val todoAction: TodoAction[Future] = new TodoAction[Future](
    loadTodos = AjaxClient[Api].getAllTodos().call(),
    deleteTodo = item => AjaxClient[Api].deleteTodo(item.id).call(),
    createOrUpdateTodo = item => AjaxClient[Api].updateTodo(item).call()
  )
  val motdAction: MotdAction[Future] = new MotdAction[Future](
    loadMotd = AjaxClient[Api].welcomeMsg("User X").call()
  )
}
