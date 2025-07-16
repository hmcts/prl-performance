package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*===============================================================================================================
* Court Manager FL401 case progression. C8 Confidentiality Check and Serve
================================================================================================================*/

object CaseManager_PRL_FL401_ProgressCase {

  val BaseURL = Environment.baseURL
  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CaseManagerConfidentialityCheck =

    exec(Common.isAuthenticated)

    //see xui-webapp cookie capture in the Homepage scenario for details of why this is being used
    .exec(addCookie(Cookie("xui-webapp", "#{xuiWebAppCookie}")
      .withMaxAge(28800)
      .withSecure(true)))

    .exec(http("XUI_PRL_XXX_685_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
      .check(jsonPath("$.tabs[7].fields[3].value.firstName").saveAs("ApplicantFirstName"))
      .check(jsonPath("$.tabs[7].fields[3].value.lastName").saveAs("ApplicantLastName"))
      .check(jsonPath("$.tabs[8].fields[11].value.firstName").saveAs("RespondentFirstName"))
      .check(jsonPath("$.tabs[8].fields[11].value.lastName").saveAs("RespondentLastName"))
      .check(jsonPath("$.case_id").is("#{caseId}")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

    .exec(Common.waJurisdictions)
    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.caseActivityGet)
    .exec(Common.isAuthenticated)

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
   * Select task tab
   ======================================================================================*/

    .exec(http("XUI_PRL_XXX_686_SelectCase")
      .get(BaseURL + "/cases/case-details/#{caseId}/task")
      .headers(Headers.xuiHeader)
      .check(substring("HMCTS Manage cases")))

      // Add cookie to jar manually from session
    .exec(
      addCookie(
        Cookie("xui-webapp", "${xuiWebAppCookie}")
          .withDomain("manage-case.perftest.platform.hmcts.net") // match exactly
          .withPath("/") // match what the cookie requires
      )
    )

    .exec(_.set("taskId", "")) // Reset the sessionId so that its empty ahead of the check

    // .exec(http("XUI_PRL_XXX_687_SelectCaseTask")
      // .get(BaseURL + "/workallocation/case/task/#{caseId}")
      // .headers(Headers.xuiHeader)
      // .header("Accept", "application/json, text/plain, */*")
      // .header("x-xsrf-token", "#{XSRFToken}")
      // .check(jsonPath("$[*].id").findAll.saveAs("taskIds"))
      // .check(jsonPath("$[*].type").findAll.saveAs("taskTypes"))
      // .check(jsonPath("$..[?(@.type=='confidentialCheckSOA')].id").optional.saveAs("taskId")))

    .pause(20) //Wait for task to appear

    //.exec { session =>
    //val ids = session("taskIds").as[Seq[String]]
    //val types = session("taskTypes").as[Seq[String]]
    //val targetType = "confidentialCheckSOA" // Task type we are looking for
    //val matchedIndex = types.indexOf(targetType)

    // Loop until the taskId is captured
    .asLongAs(session => session("taskId").asOption[String].forall(_.isEmpty)) {
      exec(http("XUI_PRL_XXX_687_SelectCaseTask")
        .get(BaseURL + "/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        //.check(jsonPath("$[*].id").findAll.saveAs("taskIds"))
        //.check(jsonPath("$[*].type").findAll.saveAs("taskTypes]"))
        .check(jsonPath("$..[?(@.type=='confidentialCheckSOA')].id").optional.saveAs("taskId")))

        .pause(5, 10) // Wait between retries
    }

    //==========================================
    // Select correct task type and ID logic
    //==========================================
    // // Get the index of the task type we are looking for, then capture the task ID from the same index
    // .exec { session =>
    //   val ids = session("taskIds").as[Seq[String]]
    //   val types = session("taskTypes").as[Seq[String]]
    //   val targetType = "confidentialCheckSOA" // Task type we are looking for
    //   val matchedIndex = types.indexOf(targetType)

    // if (matchedIndex == -1) {
    //   throw new RuntimeException(s"ID $targetType not found")
    // }

    // Get the corresponding ID and Type using the matched index
    // val matchedId = ids(matchedIndex)
    // val matchedType = types(matchedIndex)

    // Logger Debuggo
    // println(s"Matched Index: $matchedIndex")
    // println(s"Matched ID: $matchedId")
    // println(s"Matched Type: $matchedType")

    // Set the matched values as session variables
    // session
    //   .set("matchedIndex", matchedIndex)
    //   .set("matchedTaskId", matchedId)
    //   .set("matchedTaskType", matchedType)
    // }

  /*=====================================================================================
  * Claim the task
  ======================================================================================*/

  .exec(http("XUI_PRL_XXX_689_ClaimTask")
      .post(BaseURL + "/workallocation/task/#{taskId}/claim")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("""{}"""))
      .check(status.in(200, 204)))

  .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Confidentiality Check  (Case Manager)
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_690_ConfidentialityCheck")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/confidentialityCheck/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json")
      .check(jsonPath("$.task_required_for_event").is("true")))

    .exec(http("XUI_PRL_XXX_700_AmmendRespondentsDetailsEventTrigger")
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/confidentialityCheck?ignore-warning=false")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.case_fields[1].value.partyIds[0].id").saveAs("applicantPartyID"))
      .check(jsonPath("$.case_fields[1].value.partyIds[0].value").saveAs("applicantPartyIDValue"))
      .check(jsonPath("$.case_fields[1].value.packDocument[*].id").findAll.saveAs("applicantPackDocID"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[1].id").saveAs("applicantPackDocID1"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[2].id").saveAs("applicantPackDocID2"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[3].id").saveAs("applicantPackDocID3"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[4].id").saveAs("applicantPackDocID4"))
      .check(jsonPath("$.case_fields[1].value.packDocument[*].value.document_url").findAll.saveAs("applicantPackDocURL"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[0].value.document_url").saveAs("finalDocURL"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[1].value.document_url").saveAs("threeMBDocURL"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[2].value.document_url").saveAs("privacyNoticeDocURL"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[3].value.document_url").saveAs("blankOrderDocURL"))
      //.check(jsonPath("$.case_fields[1].value.packDocument[4].value.document_url").saveAs("oneTwentyKBDocURL"))
      .check(jsonPath("$.case_fields[2].value.packDocument[0].value.document_url").saveAs("coverLetterDocURL"))
      .check(jsonPath("$.case_fields[2].value.coverLettersMap[0].value.coverLetters[0].id").saveAs("coverLetterDocID"))
      .check(jsonPath("$.case_fields[2].value.coverLettersMap[0].id").saveAs("coverLettersMapID"))
      .check(jsonPath("$.case_fields[2].value.packDocument[1].value.document_url").saveAs("flDocURL"))
      .check(jsonPath("$.case_fields[7].value.document_url").saveAs("cEightDocURL"))
      .check(jsonPath("$.case_fields[1].value.packCreatedDate").saveAs("packCreatedDate"))
      .check(jsonPath("$.case_fields[2].value.partyIds[0].id").saveAs("respondentPartyID"))
      .check(jsonPath("$.case_fields[2].value.partyIds[0].value").saveAs("respondentPartyIDValue"))
      .check(jsonPath("$.case_fields[2].value.packDocument[*].id").findAll.saveAs("respondentPackDocID"))
      //.check(jsonPath("$.case_fields[2].value.packDocument[0].id").saveAs("respondentPackDocID"))
      //.check(jsonPath("$.case_fields[2].value.packDocument[1].id").saveAs("respondentPackDocID1"))
      //.check(jsonPath("$.case_fields[2].value.packDocument[2].id").saveAs("respondentPackDocID2"))
      //.check(jsonPath("$.case_fields[2].value.packDocument[3].id").saveAs("respondentPackDocID3"))
      //.check(jsonPath("$.case_fields[2].value.packDocument[4].id").saveAs("respondentPackDocID4"))
      //check(jsonPath("$.case_fields[2].value.packDocument[5].id").saveAs("respondentPackDocID5"))
      //.check(jsonPath("$.case_fields[2].value.packDocument[6].id").saveAs("respondentPackDocID6"))
      .check(jsonPath("$.case_fields[2].value.packDocument[0].value.document_creation_date").saveAs("coverDocCreationDate"))
    //.check(jsonPath("$.data.unServedRespondentPack.packDocument[0].value.document_url").saveAs("unservedCoverLetterDocURL"))
    //.check(jsonPath("$.data.unServedRespondentPack.packDocument[0].value.document_creation_date").saveAs("unservedCoverLetterDocCreationDate"))
    //.check(jsonPath("$.data.unServedRespondentPack.coverLettersMap[0].value.coverLetters[0].id").saveAs("unservedCoverLetterDocID"))
    //.check(jsonPath("$.event_data.unServedRespondentPack.coverLettersMap[0].id").saveAs("unservedCoverLettersMapID"))
      .check(jsonPath("$.id").is("confidentialityCheck"))
      .check(status.in(200, 403)))

    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    .exec(http("XUI_PRL_XXX_710_AmmendRespondentDetailsEvent")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/confidentialityCheck/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json"))


    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Confidentiality Check  (Case Manager) - Can the Order be served - Yes & Continue
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_720_SelectServeOrder&Continue")
      .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=confidentialityCheck1")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLConfidentialityCheck.json"))
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Confidentiality Check  (Case Manager) - Save & Continue
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_730_ServiceSubmit")
      .post(BaseURL + "/data/cases/#{caseId}/events")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLConfidentialityCheckEvent.json"))
      .check(jsonPath("$.after_submit_callback_response.confirmation_header").is("# The application is ready for personal service")))


    //Write case to file to know which ones are progressed to correct state
    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("FL401caseNumberProgressed.csv", true))
      try {
        fw.write(session("caseId").as[String] + "\r\n")
      } finally fw.close()
      session
    }

}
