package scenarios

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Environment
import utils.Environment.{idamAPIURL, idamURL, prlCafcasURL}

object CafcasAPI {

  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")

  //userType must be "Caseworker", "Legal" or "Citizen"
  def Auth(userType: String) =

    exec(session => userType match {
      case "Caseworker" => session.set("emailAddressCCD", "ccdloadtest-cw@gmail.com").set("passwordCCD", "Password12").set("microservice", "ccd_data")
      case "Legal" => session.set("emailAddressCCD", "ccdloadtest-la@gmail.com").set("passwordCCD", "Password12").set("microservice", "ccd_data")
      case "Solicitor" => session.set("emailAddressCCD", "cafcass@hmcts.net").set("passwordCCD", "Cafcass12").set("microservice", " fis_hmc_api")
    })

    .exec(http("XUI_000_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"${microservice}"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("XUI_000_GetBearerToken")
      .post(idamURL + "/o/token") //change this to idamapiurl if this not works
      .formParam("grant_type", "password")
      .formParam("username", "${emailAddressCCD}")
      .formParam("password", "${passwordCCD}")
      .formParam("client_id", "cafcaas-idam-id")
     // .formParam("client_secret", clientSecret)
      .formParam("client_secret", "RB4B4JLQYZUXYO5U")
      .formParam("scope", "profile roles openid")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("bearerToken")))

    .pause(1)

    /*.exec(http("XUI_000_GetIdamID")
      .get(IdamAPIURL + "/details")
      .header("Authorization", "Bearer ${bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))

    .pause(1)*/

  val searchCasesByDates =

    exec(Auth("Solicitor"))

      .exec(http("XUI_000_GetCCDEventToken")
        .get("/cases/searchCases")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "${authToken}")
        .header("Content-Type", "application/json")
        .formParam("start_date", "01/02/2023")
        .formParam("end_date", "28/02/2023")
        //.check(jsonPath("$.token").saveAs("eventToken")))
      )


    .pause(1)

  val AssignCase1 =

    exec(Auth("Solicitor"))

      .exec(http("XUI_000_GetCCDEventToken")
        .get(prlCafcasURL + "/cases/searchCases")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "${authToken}")
        .header("Content-Type", "application/json")
        .formParam("start_date", "01/02/2023")
        .formParam("end_date", "28/02/2023")
        //.check(jsonPath("$.token").saveAs("eventToken")))
      )


      .pause(1)

  val AssignCase =

    exec(Auth("Solicitor"))

      .exec(http("XUI_000_GetCCDEventToken")
        .get(prlCafcasURL + "/cases/searchCases")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "${authToken}")
        .header("Content-Type", "application/json")
        .formParam("start_date", "01/02/2023")
        .formParam("end_date", "28/02/2023")
        //.check(jsonPath("$.token").saveAs("eventToken")))
      )


      .pause(1)








}
