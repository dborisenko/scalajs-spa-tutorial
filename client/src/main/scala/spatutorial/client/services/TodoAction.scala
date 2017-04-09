package spatutorial.client.services

import cats.Monad
import cats.implicits._
import japgolly.scalajs.react.Callback
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

class TodoAction[S[_] : Monad, A[_]: Monad](
  loadTodos: => A[Seq[TodoItem]],
  deleteTodo: TodoItem => A[Unit],
  createOrUpdateTodo: TodoItem => A[Unit]
) {
  type TodoState = State[Todos]
  val TodoReactState: ReactS.FixT[S, TodoState] = ReactS.FixT[S, TodoState]

  def RefreshTodos(run: ReactST[S, TodoState, Unit] => Callback): Callback = {
    val loading = Reading()
    for {
      _ <- run(TodoReactState.modT(_.withProcessing(loading)))
      _ = for {
        items <- loadTodos
      } yield run(TodoReactState.modT(_.withoutProcessing(loading).foldValue(Todos(items))(_.copy(items = items)))).runNow()
    } yield ()
  }

  def UpdateAllTodos(run: ReactST[S, TodoState, Unit] => Callback)(items: Seq[TodoItem]): Callback = {
    run(TodoReactState.modT(state => state.foldValue(Todos(items))(_.copy(items = items))))
  }

  def CreateOrUpdateTodo(run: ReactST[S, TodoState, Unit] => Callback)(item: TodoItem): Callback = {
    val creatingOrUpdating = (Creating or Updating) ()
    for {
      _ <- run(TodoReactState.modT(_.withProcessing(creatingOrUpdating)))
      _ = for {
        _ <- createOrUpdateTodo(item)
        _ = RefreshTodos(run)
      } yield ()
    } yield ()
  }

  def DeleteTodo(run: ReactST[S, TodoState, Unit] => Callback)(item: TodoItem): Callback = {
    val deleting = Deleting()
    for {
      _ <- run(TodoReactState.modT(_.withProcessing(deleting)))
      _ = for {
        _ <- deleteTodo(item)
        _ = RefreshTodos(run)
      } yield ()
    } yield ()
  }

  def OpenUpdateTodoForm(run: ReactST[S, TodoState, Unit] => Callback)(item: TodoItem): Callback = {
    run(TodoReactState.modT(_.foldValue(Todos(selectedItem = Some(item), showTodoForm = true))(_.copy(selectedItem = Some(item), showTodoForm = true))))
  }
  def OpenCreateTodoForm(run: ReactST[S, TodoState, Unit] => Callback): Callback = {
    run(TodoReactState.modT(_.foldValue(Todos(selectedItem = None, showTodoForm = true))(_.copy(selectedItem = None, showTodoForm = true))))
  }
  def CloseTodoForm(run: ReactST[S, TodoState, Unit] => Callback)(item: Option[TodoItem]): Callback = for {
    _ <- run(TodoReactState.modT(_.foldValue(Todos())(_.copy(selectedItem = None, showTodoForm = false))))
    _ <- item.fold(run(TodoReactState.retT(())))(CreateOrUpdateTodo(run))
  } yield ()

}
