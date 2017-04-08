package spatutorial.client.components

import japgolly.scalajs.react.CatsReact._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.services.MotdAction
import spatutorial.client.services.State

import scala.language.higherKinds

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
class Motd[M[_]](action: MotdAction[M]) {

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
        Button(Button.Props($.runState(action.UpdateMotd), CommonStyle.danger), Icon.refresh, "Update")
      )
    )
    .componentDidMount(scope => scope.runState(action.UpdateMotd))
    .build

  def apply(): VdomElement = Motd()
}
