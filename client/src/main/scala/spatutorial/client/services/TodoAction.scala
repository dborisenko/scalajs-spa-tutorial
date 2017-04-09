package spatutorial.client.services

import cats.Monad
import cats.implicits._
import japgolly.scalajs.react.CatsReact._
import spatutorial.client.services.Processing.Creating
import spatutorial.client.services.Processing.Deleting
import spatutorial.client.services.Processing.Reading
import spatutorial.client.services.Processing.Updating
import spatutorial.shared.TodoItem

import scala.language.higherKinds

final case class Todos(
  items: Seq[TodoItem] = Seq.empty,
  selectedItem: Option[TodoItem] = None,
  showTodoForm: Boolean = false
)

class TodoAction[M[_] : Monad](
  loadTodos: => M[Seq[TodoItem]],
  deleteTodo: TodoItem => M[Unit],
  createOrUpdateTodo: TodoItem => M[Unit]
) {
  type TodoState = State[Todos]
  val TodoReactState: ReactS.FixT[M, TodoState] = ReactS.FixT[M, TodoState]

  val RefreshTodos: ReactST[M, TodoState, Unit] = {
    val loading = Reading()
    for {
      _ <- TodoReactState.modT(_.withProcessing(loading))
      items <- TodoReactState.ret(loadTodos)
      _ <- TodoReactState.modT(_.withoutProcessing(loading).foldValue(Todos(items))(_.copy(items = items)))
    } yield ()
  }

  def UpdateAllTodos(items: Seq[TodoItem]): ReactST[M, TodoState, Unit] = {
    TodoReactState.modT(state => state.foldValue(Todos(items))(_.copy(items = items)))
  }

  def CreateOrUpdateTodo(item: TodoItem): ReactST[M, TodoState, Unit] = {
    val creatingOrUpdating = (Creating or Updating) ()
    for {
      _ <- TodoReactState.modT(_.withProcessing(creatingOrUpdating))
      items <- TodoReactState.ret(createOrUpdateTodo(item))
      _ <- RefreshTodos
    } yield ()
  }

  def DeleteTodo(item: TodoItem): ReactST[M, TodoState, Unit] = {
    val deleting = Deleting()
    for {
      _ <- TodoReactState.modT(_.withProcessing(deleting))
      items <- TodoReactState.ret(deleteTodo(item))
      _ <- RefreshTodos
    } yield ()
  }

  def OpenUpdateTodoForm(item: TodoItem): ReactST[M, TodoState, Unit] = {
    TodoReactState.modT(_.foldValue(Todos(selectedItem = Some(item), showTodoForm = true))(_.copy(selectedItem = Some(item), showTodoForm = true)))
  }
  val OpenCreateTodoForm: ReactST[M, TodoState, Unit] = {
    TodoReactState.modT(_.foldValue(Todos(selectedItem = None, showTodoForm = true))(_.copy(selectedItem = None, showTodoForm = true)))
  }
  def CloseTodoForm(item: Option[TodoItem]): ReactST[M, TodoState, Unit] = for {
    _ <- TodoReactState.modT(_.foldValue(Todos())(_.copy(selectedItem = None, showTodoForm = false)))
    _ <- item.fold(TodoReactState.retT(()))(CreateOrUpdateTodo)
  } yield ()

}
