package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object SSCShearing {
  
  val BaseURL = Environment.baseURL
  val prlURL = "https://privatelaw.${env}.platform.hmcts.net"
  val IdamUrl = Environment.idamURL
  val PRLcases = csv("submittedCases.csv").circular
  val UserFeederHearingCases = csv("UserHearingCases.csv").circular


  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime



  val SendToHearing =

  /*======================================================================================
  * Select Send to Hearing
  ======================================================================================*/


    group("XUI_UploadResponse_031_SelectSendtoHearing") {

      exec(_.setAll(
        "PRLRandomString" -> (Common.randomString(7)))
      )
        .feed(UserFeederHearingCases)



        .exec(http("XUI_Common_000_SSCS")
          .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/adminSendToHearing/caseType/Benefit/jurisdiction/SSCS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .header("sec-fetch-site", "same-origin")
          .check(status.in(200, 304, 403)))


        .exec(http("XUI_UploadResponse_031_005_SelectSendtoWithFTA")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/adminSendToHearing?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("event_token")))
      //  .check(jsonPath("$.id").is("adminSendToWithDwp")))
      //     .check(substring("access_granted").optional.saveAs("STANDARD")))


    }


      .group("XUI_FTA_032_SubmitSendHearing") {

        exec(http("XUI_FTA_032_005_SubmitSendHearing")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
          .body(ElFileBody("bodies/PRL/CaseFlags/SubmitSendHearing.json"))
          .check(substring("hearing")))


      }
      .pause(MinThinkTime, MaxThinkTime)


}