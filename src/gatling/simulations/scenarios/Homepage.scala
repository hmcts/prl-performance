package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}

object Homepage {

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val BaseURL = Environment.baseURL
  val PrlURL = Environment.prlURL
  
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
        .check(regex("nonce=(.*)&amp;response_type").saveAs("nonce")))
    }
  
    .pause(MinThinkTime, MaxThinkTime)

  /*====================================================================================
  *PRL Citizen Homepage
  *=====================================================================================*/

  val PRLHomePage = 

    exec(flushHttpCache)
    .exec(flushCookieJar)

    .group("XUI_010_Homepage") {
      exec(http("XUI_010_005_Homepage")
        .get(PrlURL)
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))
    }

    .pause(MinThinkTime, MaxThinkTime)

}