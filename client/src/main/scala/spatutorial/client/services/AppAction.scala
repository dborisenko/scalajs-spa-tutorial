package spatutorial.client.services

import autowire._
import spatutorial.shared.Api
import boopickle.Default._
import cats.Id
import cats.implicits._
import japgolly.scalajs.react.Callback
import spatutorial.shared.TodoItem
import spatutorial.client.logger._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


object AppAction {
  var todosCache: Seq[TodoItem] = Seq.empty
  var motdCache: String = ""

  val todoAction: TodoAction[Id] = new TodoAction[Id](
    loadTodos = {
      AjaxClient[Api].getAllTodos().call().map(todosCache = _)
      todosCache
    },
    deleteTodo = item => AjaxClient[Api].deleteTodo(item.id).call(),
    createOrUpdateTodo = item => AjaxClient[Api].updateTodo(item).call()
  )
  val motdAction: MotdAction[Id, Future] = new MotdAction[Id, Future](
    loadMotd = AjaxClient[Api].welcomeMsg("User X").call()
  )
}
