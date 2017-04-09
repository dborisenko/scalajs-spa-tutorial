package spatutorial.client.services

import autowire._
import spatutorial.shared.Api
import boopickle.Default._
import cats.Id
import spatutorial.shared.TodoItem

import scala.concurrent.Await
import scala.concurrent.duration._
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
  val motdAction: MotdAction[Id] = new MotdAction[Id](
    loadMotd = {
      AjaxClient[Api].welcomeMsg("User X").call().map(motdCache = _)
      motdCache
    }
  )
}
