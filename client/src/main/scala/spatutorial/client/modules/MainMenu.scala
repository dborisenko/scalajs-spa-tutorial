package spatutorial.client.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.SPAMain.DashboardLoc
import spatutorial.client.SPAMain.Loc
import spatutorial.client.SPAMain.TodoLoc
import spatutorial.client.components.Icon._
import spatutorial.client.components._

object MainMenu {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(router: RouterCtl[Loc], currentLoc: Loc)

  private case class MenuItem(idx: Int, label: (Props) => TagMod, icon: Icon, location: Loc)

  //  // build the Todo menu item, showing the number of open todos
  //  private def buildTodoMenu(props: Props): VdomElement = {
  //    val todoCount = props.proxy().getOrElse(0)
  //    <.span(
  //      <.span("Todo "),
  //      todoCount > 0 ?= <.span(bss.labelOpt(CommonStyle.danger), bss.labelAsBadge, todoCount)
  //    )
  //  }

  private val menuItems = Seq(
    MenuItem(1, _ => "Dashboard", Icon.dashboard, DashboardLoc),
    MenuItem(2, _ => "Todo", Icon.check, TodoLoc)
  )

  private class Backend($: BackendScope[Props, Unit]) {

    def render(props: Props): VdomElement = {
      // build a list of menu items
      val tags: Seq[TagMod] = for (item <- menuItems) yield {
        <.li(^.key := item.idx, (^.className := "active") when (props.currentLoc == item.location),
          props.router.link(item.location)(item.icon, " ", item.label(props))
        )
      }
      <.ul(tags: _*, ^.style := bss.navbar)
    }
  }

  private val component = ScalaComponent.builder[Props]("MainMenu")
    .renderBackend[Backend]
    .build

  def apply(ctl: RouterCtl[Loc], currentLoc: Loc): VdomElement =
    component(Props(ctl, currentLoc))
}
