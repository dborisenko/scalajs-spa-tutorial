package spatutorial.client.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.SPAMain.Loc
import spatutorial.client.SPAMain.TodoLoc
import spatutorial.client.components._
import spatutorial.client.services.MotdAction

import scala.language.existentials
import scala.language.higherKinds
import scala.util.Random

class Dashboard[M[_]](motd: MotdAction[M]) {

  case class Props(router: RouterCtl[Loc])

  // create dummy data for the chart
  val cp = Chart.ChartProps(
    "Test chart",
    Chart.BarChart,
    ChartData(
      Random.alphanumeric.map(_.toUpper.toString).distinct.take(10),
      Seq(ChartDataset(Iterator.continually(Random.nextDouble() * 10).take(10).toSeq, "Data1"))
    )
  )

  private val motdElement: VdomElement = new Motd(motd).apply()

  // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("Dashboard")
    // create and store the connect proxy in state for later use
    .initialState(())
    .renderPS { (_, props, _) =>
      <.div(
        // header, MessageOfTheDay and chart components
        <.h2("Dashboard"),
        motdElement,
        Chart(cp),
        // create a link to the To Do view
        <.div(props.router.link(TodoLoc)("Check your todos!"))
      )
    }
    .build

  def apply(router: RouterCtl[Loc]): VdomElement = component(Props(router))
}
