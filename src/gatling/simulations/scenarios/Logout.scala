package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, Headers}

object Logout {

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val BaseURL = Environment.baseURL

  /*====================================================================================
  *Manage Case Logout
  *=====================================================================================*/

  val XUILogout =

    group("XUI_999_Logout") {
      exec(http("XUI_999_005_Logout")
        .get(BaseURL + "/auth/logout")
        .headers(Headers.navigationHeader)
        )
    }
    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    .pause(MinThinkTime , MaxThinkTime)

  /*======================================================================================
  * CUI Logout
  ======================================================================================*/

  val CUILogout = 

    group("PRL_CUI_Logout") {
      exec(http("PRL_CUI_Logout")
        .get(Environment.prlURL + "/logout")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(substring("Sign in or create an account")))
    }


}