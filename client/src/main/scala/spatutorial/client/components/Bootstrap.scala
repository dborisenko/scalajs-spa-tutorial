package spatutorial.client.components

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.language.implicitConversions
import scala.scalajs.js
import scalacss.Defaults._

/**
 * Common Bootstrap components for scalajs-react
 */
object Bootstrap {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  @js.native
  trait BootstrapJQuery extends JQuery {
    def modal(action: String): BootstrapJQuery = js.native
    def modal(options: js.Any): BootstrapJQuery = js.native
  }

  implicit def jq2bootstrap(jq: JQuery): BootstrapJQuery = jq.asInstanceOf[BootstrapJQuery]

  // Common Bootstrap contextual styles
  object CommonStyle extends Enumeration {
    val default, primary, success, info, warning, danger = Value
  }

  object Button {

    case class Props(onClick: Callback, style: CommonStyle.Value = CommonStyle.default, addStyles: Seq[StyleA] = Seq())

    private val component = ScalaComponent.builder[Props]("Button")
      .renderPC((_, p, c) =>
        <.button(^.style := bss.buttonOpt(p.style), ^.style := p.addStyles, ^.tpe := "button", ^.onClick --> p.onClick, c)
      ).build

    def apply(props: Props, children: ChildArg*): VdomElement = component(props)(children: _*)
  }

  object Panel {

    case class Props(heading: String, style: CommonStyle.Value = CommonStyle.default)

    private val component = ScalaComponent.builder[Props]("Panel")
      .renderPC((_, p, c) =>
        <.div(^.style := bss.panelOpt(p.style),
          <.div(^.style := bss.panelHeading, p.heading),
          <.div(^.style := bss.panelBody, c)
        )
      ).build

    def apply(props: Props, children: ChildArg*): VdomElement = component(props)(children: _*)
  }

  object Modal {

    // header and footer are functions, so that they can get access to the the hide() function for their buttons
    case class Props(header: Callback => VdomElement, footer: Callback => VdomElement, closed: Callback, backdrop: Boolean = true,
                     keyboard: Boolean = true)

    private class Backend(t: BackendScope[Props, Unit]) {
      private def hide = t.getDOMNode.map { elem =>
        // instruct Bootstrap to hide the modal
        jQuery(elem).modal("hide")
      }

      // jQuery event handler to be fired when the modal has been hidden
      def hidden(e: JQueryEventObject): js.Any = {
        // inform the owner of the component that the modal was closed/hidden
        t.props.flatMap(_.closed).runNow()
      }

      def render(p: Props, c: PropsChildren): VdomElement = {
        val modalStyle = bss.modal
        <.div(^.style := modalStyle.modal, ^.style := modalStyle.fade, ^.role := "dialog", ^.aria.hidden := true,
          <.div(^.style := modalStyle.dialog,
            <.div(^.style := modalStyle.content,
              <.div(^.style := modalStyle.header, p.header(hide)),
              <.div(^.style := modalStyle.body, c),
              <.div(^.style := modalStyle.footer, p.footer(hide))
            )
          )
        )
      }
    }

    private val component = ScalaComponent.builder[Props]("Modal")
      .renderBackend[Backend]
      .componentDidMount(scope => Callback {
        val p = scope.props
        // instruct Bootstrap to show the modal
        jQuery(scope.getDOMNode).modal(js.Dynamic.literal("backdrop" -> p.backdrop, "keyboard" -> p.keyboard, "show" -> true))
        // register event listener to be notified when the modal is closed
        jQuery(scope.getDOMNode).on("hidden.bs.modal", null, null, scope.backend.hidden _)
      })
      .build

    def apply(props: Props, children: ChildArg*): VdomElement = component.applyGeneric(props)(children: _*)
  }

}
