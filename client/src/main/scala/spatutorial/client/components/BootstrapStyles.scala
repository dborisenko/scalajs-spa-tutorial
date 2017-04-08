package spatutorial.client.components

import japgolly.univeq.UnivEq
import spatutorial.client.components.Bootstrap.CommonStyle

import scalacss.Defaults._
import scalacss.internal.mutable
import spatutorial.client.components.Bootstrap.CommonStyle._

class BootstrapStyles(implicit r: mutable.Register) extends StyleSheet.Inline()(r) {

  import dsl._

  implicit val styleUnivEq: UnivEq[CommonStyle.Value] = new UnivEq[CommonStyle.Value] {}

  val csDomain: Domain[Bootstrap.CommonStyle.Value] = Domain.ofValues(default, primary, success, info, warning, danger)

  val contextDomain: Domain[Bootstrap.CommonStyle.Value] = Domain.ofValues(success, info, warning, danger)

  def commonStyle[A: UnivEq](domain: Domain[A], base: String): A => StyleA = styleF(domain)(opt =>
    styleS(addClassNames(base, s"$base-$opt"))
  )

  def styleWrap(classNames: String*): StyleA = style(addClassNames(classNames: _*))

  val buttonOpt: Bootstrap.CommonStyle.Value => scalacss.Defaults.StyleA = commonStyle(csDomain, "btn")

  val button: scalacss.Defaults.StyleA = buttonOpt(default)

  val panelOpt: Bootstrap.CommonStyle.Value => scalacss.Defaults.StyleA = commonStyle(csDomain, "panel")

  val panel: scalacss.Defaults.StyleA = panelOpt(default)

  val labelOpt: Bootstrap.CommonStyle.Value => scalacss.Defaults.StyleA = commonStyle(csDomain, "label")

  val label: scalacss.Defaults.StyleA = labelOpt(default)

  val alert: Bootstrap.CommonStyle.Value => scalacss.Defaults.StyleA = commonStyle(contextDomain, "alert")

  val panelHeading: scalacss.Defaults.StyleA = styleWrap("panel-heading")

  val panelBody: scalacss.Defaults.StyleA = styleWrap("panel-body")

  // wrap styles in a namespace, assign to val to prevent lazy initialization
  object modal {
    val modal: scalacss.Defaults.StyleA = styleWrap("modal")
    val fade: scalacss.Defaults.StyleA = styleWrap("fade")
    val dialog: scalacss.Defaults.StyleA = styleWrap("modal-dialog")
    val content: scalacss.Defaults.StyleA = styleWrap("modal-content")
    val header: scalacss.Defaults.StyleA = styleWrap("modal-header")
    val body: scalacss.Defaults.StyleA = styleWrap("modal-body")
    val footer: scalacss.Defaults.StyleA = styleWrap("modal-footer")
  }

  val _modal: modal.type = modal

  object listGroup {
    val listGroup: scalacss.Defaults.StyleA = styleWrap("list-group")
    val item: scalacss.Defaults.StyleA = styleWrap("list-group-item")
    val itemOpt: Bootstrap.CommonStyle.Value => scalacss.Defaults.StyleA = commonStyle(contextDomain, "list-group-item")
  }

  val _listGroup: listGroup.type = listGroup
  val pullRight: scalacss.Defaults.StyleA = styleWrap("pull-right")
  val buttonXS: scalacss.Defaults.StyleA = styleWrap("btn-xs")
  val close: scalacss.Defaults.StyleA = styleWrap("close")

  val labelAsBadge: StyleA = style(addClassName("label-as-badge"), borderRadius(1.em))

  val navbar: scalacss.Defaults.StyleA = styleWrap("nav", "navbar-nav")

  val formGroup: scalacss.Defaults.StyleA = styleWrap("form-group")
  val formControl: scalacss.Defaults.StyleA = styleWrap("form-control")
}
