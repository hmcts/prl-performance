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

    .exec(http("CAFCAS_000_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"${microservice}"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("CAFCAS_000_GetBearerToken")
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
  .exec(_.setAll(
    "FileName1" -> "1MB.pdf"))

      /*.exec(http("CafcasAPI_000_searchCasesByDates")
        .get("/cases/searchCases")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "${authToken}")
        .header("Content-Type", "application/json")
        .formParam("start_date", "01/02/2023")
        .formParam("end_date", "28/02/2023")
        //.check(jsonPath("$.token").saveAs("eventToken")))
      )*/

    .pause(1)
// below is to retrieve the document from document Id
      .exec(http("CafcasAPI_000_downloadDocument")
        .get( "/c525ddc0-9a59-40a8-bdf8-5549fa528a8d/download")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "${authToken}")
        //.header("Content-Type", "application/json")
        //.check(jsonPath("$.token").saveAs("eventToken")))
      )


      .pause(1)

      .exec(http("CafcasAPI_000_uploadDocument")
        .post( "/1677144326732696/document?typeOfDocument=pdf")
        .header("ServiceAuthorization", "Bearer #{bearerToken}")
        .header("accept", "application/json")
        .header("Content-Type", "multipart/form-data")
        .bodyPart(RawFileBodyPart("files", "#{FileName}")
          .fileName("#{FileName1}")
          .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(regex("""documents/(.+?)/binary""").saveAs("Document_ID4")))

}
