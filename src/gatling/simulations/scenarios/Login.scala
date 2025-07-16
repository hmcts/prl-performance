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
        .formParam("azureLoginEnabled", "true")
        .formParam("mojLoginEnabled", "true")
        .formParam("selfRegistrationEnabled", "false")
        .formParam("_csrf", "#{csrf}")
        .headers(Headers.navigationHeader)
        .headers(Headers.postHeader)
        .check(regex("Manage cases")))

      //see xui-webapp cookie capture in the Homepage scenario for details of why this is being used
      .exec(addCookie(Cookie("xui-webapp", "#{xuiWebAppCookie}")
        .withMaxAge(28800)
        .withSecure(true)))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)

    //see xui-webapp cookie capture in the Homepage scenario for details of why this is being used
    .exec(addCookie(Cookie("xui-webapp", "#{xuiWebAppCookie}")
      .withMaxAge(28800)
      .withSecure(true)))

    // Add cookie to jar manually
    .exec(
      addCookie(
        Cookie("xui-webapp", "${xuiWebAppCookie}")
          .withDomain("manage-case.perftest.platform.hmcts.net") 
          .withPath("/") 
      )
    )
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

  /*====================================================================================
  *PRL Citizen Login
  *=====================================================================================*/

  val PrlLogin = 

    group("PRL_CUI_020_Login") {
      exec(http("PRL_CUI_020_005_Login")
        .post(IdamUrl + "/login?client_id=prl-citizen-frontend&response_type=code&redirect_uri=" + Environment.prlURL + "/receiver")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("username", "#{user}")
        .formParam("password", "#{password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "true")
        .formParam("_csrf", "#{csrf}")
        .check(substring("Child arrangements and family injunction cases")))
    }


    .pause(MinThinkTime, MaxThinkTime)

}