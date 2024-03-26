package utils

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.css.CssCheckType
import jodd.lagarto.dom.NodeSelector

object CsrfCheck2 {
  def save: CheckBuilder[CssCheckType, NodeSelector] = css("input[name='csrfToken']", "value").saveAs("csrf")

  def csrfParameter: String = "csrfToken"
  def csrfTemplate: String = "#{csrf}"
}
