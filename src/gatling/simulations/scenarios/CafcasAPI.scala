package scenarios

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.Common.randomDateWithinMonth
import utils.{Common, Environment}
import utils.Environment.{idamAPIURL, idamURL, prlCafcasURL}
import java.time.LocalDate

object CafcasAPI {

  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL
  val prlCafcasURL = Environment.prlCafcasURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")
  val FileName = "120KB.pdf"
  val casedocIds = csv("CafcassDocIds.csv").circular
  val cafcaseIds = csv("CafcasscaseIds.csv").circular

  // declare random dates function

  val (randomStartDate, randomEndDate) = randomDateWithinMonth("2022-02-01T00:00:00")

  //userType must be "Caseworker", "Legal" or "Citizen"
  def Auth (userType: String) =
    exec(session => userType match {
      case "Caseworker" => session.set("emailAddressCCD", "ccdloadtest-cw@gmail.com").set("passwordCCD", "Password12").set("microservice", "ccd_data")
      case "Legal" => session.set("emailAddressCCD", "ccdloadtest-la@gmail.com").set("passwordCCD", "Password12").set("microservice", "ccd_data")
      case "Solicitor" => session.set("emailAddressCCD", "cafcass@hmcts.net").set("passwordCCD", "Cafcass12").set("microservice", "api_gw")
    })

      .exec(http("CAFCAS_000_Auth")
        .post(RpeAPIURL + "/testing-support/lease")
        .body(StringBody("""{"microservice":"${microservice}"}""")).asJson
        .check(regex("(.+)").saveAs("authToken")))

      .pause(MinThinkTime,MaxThinkTime)

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
  
      .pause(MinThinkTime,MaxThinkTime)

  /*.exec(http("XUI_000_GetIdamID")
    .get(IdamAPIURL + "/details")
    .header("Authorization", "Bearer ${bearerToken}")
    .check(jsonPath("$.id").saveAs("idamId")))

  .pause(1)*/

  /*======================================================================================
  * Cafcass API Calls - Call all the API calls, need to refactor in future based on requirement
  ======================================================================================*/

  val getCasesBetweenDates =
    exec(Auth("Solicitor"))
      // set the values for random start date and random end date end date
      .exec(_.setAll(
        "randomStartDate" -> randomStartDate,
        "randomEndDate" -> randomEndDate
      ))

  /*======================================================================================
  * Cafcass API Call - Request case data between two dates
  ======================================================================================*/


   .exec(http("CafcasAPI_000_searchCasesByDates")
   .get( "/cases/searchCases?start_date=2023-03-14T11:26:34&end_date=2023-03-14T15:26:34")
  //   .get( "/cases/searchCases?start_date=${randomStartDate}&end_date=${randomEndDate}")
    // .get( "/cases/searchCases?start_date=2022-01-13T00:00:00&end_date=2023-04-16T15:38:00")
   .header("Authorization", "Bearer ${bearerToken}")
   .header("ServiceAuthorization", "Bearer ${authToken}")
   .header("Content-Type", "application/json")
   .check(status.is(200))
   )
      .pause(MinThinkTime, MaxThinkTime)


  val downloadByDocId =
    exec(Auth("Solicitor"))

      /*======================================================================================
      * Cafcass API Call - to download the document related to case from document Id
      ======================================================================================*/
      .feed(casedocIds)
      .exec(http("CafcasAPI_000_downloadDocument")
        //.get( "/8487f33f-9e64-43dc-b0a9-c3bfbd9edcbf/download")
        .get(prlCafcasURL + "/cases/documents/${documentId}/binary")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "Bearer ${authToken}")
        .header("Content-Type", "application/json")
        .check(status.is(200))
      )
      .pause(MinThinkTime, MaxThinkTime)


  val uploadDocToCase =
    exec(Auth("Solicitor"))

      /*======================================================================================
      * Cafcass API Call - to upload the relevant document for the given caseId
      ======================================================================================*/

      .feed(cafcaseIds)
      .exec(http("CafcasAPI_000_uploadDocument")
     // .post("http://prl-cos-perftest.service.core-compute-perftest.internal/1682074324904901/document")
        .post(prlCafcasURL + "/${cafcaseId}/document")
        .header("Authorization", "Bearer ${bearerToken}")
        .header("ServiceAuthorization", "Bearer ${authToken}")
        .header("Content-Type", "multipart/form-data")
        /*.formParam("typeOfDocument", "Safeguarding_Letter_Update")
        .bodyPart(RawFileBodyPart("files", "120KB.pdf")
          .fileName("120KB.pdf")
          .transferEncoding("binary"))*/
        .bodyPart(RawFileBodyPart("file", "120KB.pdf"))
        .formParam("typeOfDocument", "Safeguarding_Letter_Update")
        .check(status.is(200))
      )
      .pause(MinThinkTime, MaxThinkTime)
  
  
      
}
