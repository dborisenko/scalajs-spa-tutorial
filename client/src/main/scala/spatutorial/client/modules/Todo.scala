package spatutorial.client.modules

import cats.Monad
import cats.~>
import japgolly.scalajs.react._
import japgolly.scalajs.react.CatsReact._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.components._
import spatutorial.client.logger._
import spatutorial.client.services._
import spatutorial.shared._
import scalacss.ScalaCssReact._

import scala.language.higherKinds

class Todo[M[_]](action: TodoAction[M])(implicit t: M ~> CallbackTo, m: Monad[M]) {
  // create the React component for To Do management
  private val component = ScalaComponent.builder[Unit]("TODO")
    .initialState(State[Todos]())
    .renderS(($, state) =>
      state match {
        case State.Value(todos) =>
          val list: VdomNode = <.div(
            TodoList(todos.items, $.runStateFn(action.CreateOrUpdateTodo), $.runStateFn(action.OpenUpdateTodoForm), $.runStateFn(action.DeleteTodo)),
            Button(Button.Props($.runState(action.OpenCreateTodoForm)), Icon.plusSquare, " New")
          )
          val form: Option[VdomNode] = {
            // if the dialog is open, add it to the panel
            if (todos.showTodoForm)
              Some(TodoForm(TodoForm.Props(todos.selectedItem, $.runStateFn(action.CloseTodoForm))))
            else // otherwise add an empty placeholder
              None
          }
          Panel(Panel.Props("What needs to be done"), Seq(Some(list), form).flatten: _*)
        case State.Processing(_) => <.p("Loading...")
        case _                   => <.p("Failed to load")
      }
    )
    .componentDidMount($ => $.runState(action.RefreshTodos))
    .build

  /** Returns a function compatible with router location system while using our own props */
  def apply(): VdomElement = component()
}

object TodoForm {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(item: Option[TodoItem], submitHandler: Option[TodoItem] => Callback)

  case class State(item: TodoItem, cancelled: Boolean = true)

  private class Backend(t: BackendScope[Props, State]) {
    def submitForm(): Callback = {
      // mark it as NOT cancelled (which is the default)
      t.modState(s => s.copy(cancelled = false))
    }

    def formClosed(state: State, props: Props): Callback =
      // call parent handler with the new item and whether form was OK or cancelled
      props.submitHandler(if (state.cancelled) None else Some(state.item))

    def updateDescription(e: ReactEventFromInput): Callback = {
      val text = e.target.value
      // update TodoItem content
      t.modState(s => s.copy(item = s.item.copy(content = text)))
    }

    def updatePriority(e: ReactEventFromInput): Callback = {
      // update TodoItem priority
      val newPri = e.currentTarget.value match {
        case p if p == TodoHigh.toString => TodoHigh
        case p if p == TodoNormal.toString => TodoNormal
        case p if p == TodoLow.toString => TodoLow
      }
      t.modState(s => s.copy(item = s.item.copy(priority = newPri)))
    }

    def render(p: Props, s: State): VdomElement = {
      log.debug(s"User is ${if (s.item.id == "") "adding" else "editing"} a todo or two")
      val headerText = if (s.item.id == "") "Add new todo" else "Edit todo"
      Modal(Modal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> hide, Icon.close), <.h4(headerText)),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(Button(Button.Props(submitForm() >> hide), "OK")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p),
        children = <.div(
          <.div(bss.formGroup,
            <.label(^.`for` := "description", "Description"),
            <.input.text(bss.formControl, ^.id := "description", ^.value := s.item.content,
              ^.placeholder := "write description", ^.onChange ==> updateDescription)),
          <.div(bss.formGroup,
            <.label(^.`for` := "priority", "Priority"),
            // using defaultValue = "Normal" instead of option/selected due to React
            <.select(bss.formControl, ^.id := "priority", ^.value := s.item.priority.toString, ^.onChange ==> updatePriority,
              <.option(^.value := TodoHigh.toString, "High"),
              <.option(^.value := TodoNormal.toString, "Normal"),
              <.option(^.value := TodoLow.toString, "Low")
            )
          )
        )
      ))
    }
  }

  private val component = ScalaComponent.builder[Props]("TodoForm")
    .initialState_P(p => State(p.item.getOrElse(TodoItem("", 0, "", TodoNormal, completed = false))))
    .renderBackend[Backend]
    .build

  def apply(props: Props): VdomElement = component(props)
}