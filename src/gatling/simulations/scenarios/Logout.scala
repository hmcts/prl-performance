package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, Headers}

object Logout {

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  /*====================================================================================
  *Manage Case Logout
  *=====================================================================================*/

  val XUILogout =

    group("XUI_999_Logout") {
      exec(http("XUI_999_005_Logout")
        .get("/auth/logout")
        .headers(Headers.navigationHeader)
        // .check(regex("Sign in - HMCTS Access"))
        )
    }

    .pause(MinThinkTime , MaxThinkTime)

}