package spatutorial.client

import cats.Id
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.CatsReact._
import japgolly.scalajs.react.vdom.TagOf
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.html.Div
import spatutorial.client.components.GlobalStyles
import spatutorial.client.logger._
import spatutorial.client.modules._
import spatutorial.client.services.AppAction

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scalacss.Defaults._
import scalacss.ScalaCssReact._

@JSExport("SPAMain")
object SPAMain extends js.JSApp {

  // Define the locations (pages) used in this application
  sealed trait Loc

  case object DashboardLoc extends Loc

  case object TodoLoc extends Loc

  val dashboard: Dashboard[Id, Future] = new Dashboard[Id, Future](AppAction.motdAction)
  val todo: Todo[Id, Future] = new Todo[Id, Future](AppAction.todoAction)

  // configure the router
  val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    (staticRoute(root, DashboardLoc) ~> renderR(dashboard(_))
      | staticRoute("#todo", TodoLoc) ~> renderR(_ => todo())
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]): TagOf[Div] = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top",
        <.div(^.className := "container",
          <.div(^.className := "navbar-header", <.span(^.className := "navbar-brand", "SPA Tutorial")),
          <.div(^.className := "collapse navbar-collapse",
            // connect menu to model, because it needs to update when the number of open todos changes
            MainMenu(c, r.page)
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container", r.render())
    )
  }

  @JSExport
  def main(): Unit = {
    log.warn("Application starting")
    // send log messages also to the server
    log.enableServerLogging("/logging")
    log.info("This message goes to server as well")

    // create stylesheet
    GlobalStyles.addToDocument()
    // create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    // tell React to render the router in the document body
    router().renderIntoDOM(dom.document.getElementById("root"))
  }
}
