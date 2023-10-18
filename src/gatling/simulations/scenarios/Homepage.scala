package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}

object Homepage {

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val BaseURL = Environment.baseURL
  
  /*====================================================================================
  *Manage Case Homepage
  *=====================================================================================*/
  
  val XUIHomePage =

    exec(flushHttpCache)
    .exec(flushCookieJar)

    .group("XUI_010_Homepage") {
      exec(http("XUI_010_005_Homepage")
        .get(BaseURL)
        .headers(Headers.navigationHeader)
        .header("sec-fetch-site", "none"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(http("XUI_010_010_AuthLogin")
        .get(BaseURL + "/auth/login")
        .headers(Headers.navigationHeader)
        .check(CsrfCheck.save)
        .check(regex("/oauth2/callback&amp;state=(.*)&amp;nonce=").saveAs("state"))
        .check(regex("&nonce=(.*)&response_type").saveAs("nonce")))
    }
  
  .pause(MinThinkTime, MaxThinkTime)

}