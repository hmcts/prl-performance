package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

object Login {
  
  val IdamUrl = Environment.idamURL
  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  /*====================================================================================
  *Manage Case Login
  *=====================================================================================*/

  val XUILogin =

    group("XUI_020_Login") {
      exec(http("XUI_020_005_Login")
        .post(IdamUrl + "/login?client_id=xuiwebapp&redirect_uri=" + BaseURL + "/oauth2/callback&state=#{state}&nonce=#{nonce}&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=")
        .formParam("username", "#{user}")
        .formParam("password", "#{password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "#{csrf}")
        .headers(Headers.navigationHeader)
        .headers(Headers.postHeader)
        .check(regex("Manage cases")))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

    }
    .pause(MinThinkTime , MaxThinkTime)


  val XUILoginCa =

    group("XUI_020_LoginCa") {
      exec(http("XUI_020_005_LoginCa")
        .post(IdamUrl + "/login?client_id=xuiwebapp&redirect_uri=" + BaseURL + "/oauth2/callback&state=#{state}&nonce=#{nonce}&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=")
        .formParam("username", "#{userCa}")
        .formParam("password", "#{passwordCa}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "#{csrf}")
        .headers(Headers.navigationHeader)
        .headers(Headers.postHeader)
        .check(regex("Manage cases")))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

    }
      .pause(MinThinkTime , MaxThinkTime)



}