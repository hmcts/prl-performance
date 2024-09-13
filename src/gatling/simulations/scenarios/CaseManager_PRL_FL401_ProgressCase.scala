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

  /*=====================================================================================
  * Select Case  (Case Manager)
  ======================================================================================*/

    exec(Common.isAuthenticated)

    .exec(http("XUI_PRL_XXX_685_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .check(jsonPath("$.tabs[7].fields[3].value.firstName").saveAs("ApplicantFirstName"))
      .check(jsonPath("$.tabs[7].fields[3].value.lastName").saveAs("ApplicantLastName"))
      .check(jsonPath("$.tabs[8].fields[11].value.firstName").saveAs("RespondentFirstName"))
      .check(jsonPath("$.tabs[8].fields[11].value.lastName").saveAs("RespondentLastName"))
      .check(jsonPath("$.case_id").is("#{caseId}")))

    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    .exec(Common.waJurisdictions)
    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.caseActivityGet)
    .exec(Common.isAuthenticated)

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Confidentiality Check  (Case Manager)
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_690_ConfidentialityCheck")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/confidentialityCheck/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json")
      .check(jsonPath("$.task_required_for_event").is("false")))


    .exec(http("XUI_PRL_XXX_700_AmmendRespondentsDetailsEventTrigger")
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/confidentialityCheck?ignore-warning=false")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*") 
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.case_fields[1].value.partyIds[0].id").saveAs("applicantPartyID"))
      .check(jsonPath("$.case_fields[1].value.partyIds[0].value").saveAs("applicantPartyIDValue"))
      .check(jsonPath("$.case_fields[1].value.packDocument[0].id").saveAs("applicantPackDocID"))
      .check(jsonPath("$.case_fields[1].value.packDocument[1].id").saveAs("applicantPackDocID1"))
      .check(jsonPath("$.case_fields[1].value.packDocument[2].id").saveAs("applicantPackDocID2"))
      .check(jsonPath("$.case_fields[1].value.packDocument[3].id").saveAs("applicantPackDocID3"))
      .check(jsonPath("$.case_fields[1].value.packDocument[4].id").saveAs("applicantPackDocID4"))
      .check(jsonPath("$.case_fields[1].value.packDocument[0].value.document_url").saveAs("finalDocURL"))
      .check(jsonPath("$.case_fields[1].value.packDocument[1].value.document_url").saveAs("threeMBDocURL"))
      .check(jsonPath("$.case_fields[1].value.packDocument[2].value.document_url").saveAs("privacyNoticeDocURL"))
      .check(jsonPath("$.case_fields[1].value.packDocument[3].value.document_url").saveAs("blankOrderDocURL"))
      .check(jsonPath("$.case_fields[1].value.packDocument[4].value.document_url").saveAs("oneTwentyKBDocURL"))
      .check(jsonPath("$.case_fields[2].value.packDocument[0].value.document_url").saveAs("coverLetterDocURL"))
      .check(jsonPath("$.case_fields[2].value.packDocument[1].value.document_url").saveAs("flDocURL"))
      .check(jsonPath("$.case_fields[6].formatted_value.document_url").saveAs("cEightDocURL"))
      .check(jsonPath("$.case_fields[1].value.packCreatedDate").saveAs("packCreatedDate"))
      .check(jsonPath("$.case_fields[2].value.partyIds[0].id").saveAs("respondentPartyID"))
      .check(jsonPath("$.case_fields[2].value.partyIds[0].value").saveAs("respondentPartyIDValue"))
      .check(jsonPath("$.case_fields[2].value.packDocument[0].id").saveAs("respondentPackDocID"))
      .check(jsonPath("$.case_fields[2].value.packDocument[1].id").saveAs("respondentPackDocID1"))
      .check(jsonPath("$.case_fields[2].value.packDocument[2].id").saveAs("respondentPackDocID2"))
      .check(jsonPath("$.case_fields[2].value.packDocument[3].id").saveAs("respondentPackDocID3"))
      .check(jsonPath("$.case_fields[2].value.packDocument[4].id").saveAs("respondentPackDocID4"))
      .check(jsonPath("$.case_fields[2].value.packDocument[5].id").saveAs("respondentPackDocID5"))
      .check(jsonPath("$.case_fields[2].value.packDocument[6].id").saveAs("respondentPackDocID6"))
      .check(jsonPath("$.case_fields[2].value.packDocument[0].value.document_creation_date").saveAs("coverDocCreationDate"))
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
