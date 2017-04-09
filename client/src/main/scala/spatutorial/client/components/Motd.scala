package spatutorial.client.components

import cats.Monad
import cats.~>
import japgolly.scalajs.react.CatsReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.services.MotdAction
import spatutorial.client.services.State
import spatutorial.client.logger._

import scala.language.higherKinds

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
class Motd[S[_], A[_]](action: MotdAction[S, A])(implicit t: S ~> CallbackTo, m: Monad[S]) {

  // create the React component for holding the Message of the Day
  private val Motd = ScalaComponent.builder[Unit]("Motd")
    .initialState(State[String]())
    .renderS(($, state) =>
      Panel(Panel.Props("Message of the day"),
        state match {
          case State.Value(data)   => <.p(data)
          case State.Processing(_) => <.p("Loading...")
          case _                   => <.p("Failed to load")
        },
        Button(Button.Props(action.UpdateMotd($.runState(_)), CommonStyle.danger), Icon.refresh, "Update")
      )
    )
    .componentDidMount(scope => action.UpdateMotd(scope.runState(_)))
    .build

  def apply(): VdomElement = Motd()
}
