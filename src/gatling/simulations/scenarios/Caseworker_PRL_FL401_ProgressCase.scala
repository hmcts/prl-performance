package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*===============================================================================================================
* Court Admin FL401 case progression. Send to local court --> Sent to Gatekeeper --> Add an order --> Serve 
================================================================================================================*/

object Caseworker_PRL_FL401_ProgressCase {
  
  val BaseURL = Environment.baseURL
  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

    //set session variables
    exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "JudgeFirstName" -> (Common.randomString(4) + "judgefirst"),
      "JudgeLastName" -> (Common.randomString(4) + "judgeLast"),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth(),
      "todayDate" -> Common.getDate(),
      "LegalAdviserName" -> (Common.randomString(4) + " " + Common.randomString(4) + "legAdv")))

  val CourtAdminCheckApplication =

    exec(http("XUI_PRL_XXX_290_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .check(jsonPath("$.tabs[6].fields[3].value.firstName").saveAs("ApplicantFirstName"))
      .check(jsonPath("$.tabs[6].fields[3].value.lastName").saveAs("ApplicantLastName"))
      .check(jsonPath("$.tabs[6].fields[8].value.firstName").saveAs("RespondentFirstName"))
      .check(jsonPath("$.tabs[6].fields[8].value.lastName").saveAs("RespondentLastName"))
      .check(jsonPath("$.case_id").is("#{caseId}")))
      
    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
     .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

    .exec(Common.waJurisdictions)
    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.caseActivityGet)
    .exec(Common.isAuthenticated)

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Select Issue and send to local Court
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_360_IssueAndSendToLocalCourt")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/issueAndSendToLocalCourtCallback/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json")
      .check(jsonPath("$.task_required_for_event").is("true")))

    .exec(Common.activity)
    .exec(Common.profile)

    .exec(http("XUI_PRL_XXX_370_IssueAndSendToLocalCourtEventTrigger")  //*** SAVE THE Courtlist response here for use in later post requests **
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/issueAndSendToLocalCourtCallback?ignore-warning=false")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.id").is("issueAndSendToLocalCourtCallback"))
      .check(status.in(200, 403)))

    .exec(http("XUI_PRL_XXX_380_IssueAndSendToLocalCourtEvent")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/issueAndSendToLocalCourtCallback/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json"))
     
    .exec(Common.userDetails)
    .exec(Common.caseActivityOnlyGet)

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
   * Select Court from dropdown and submit
   ======================================================================================*/

    .exec(http("XUI_PRL_XXX_390_SelectCourt")
      .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=issueAndSendToLocalCourtCallback1")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLLocalCourt.json"))
      .check(jsonPath("$.data.courtList.value.code").is("234946:")))  //Value does not change for now. 

    .pause(MinThinkTime, MaxThinkTime)

    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.activity)

    .exec(http("XUI_PRL_XXX_410_SubmitToLocalCourtEvent")
      .post(BaseURL + "/data/cases/#{caseId}/events")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"))
      .check(jsonPath("$.data.courtList.value.code").is("234946:")))  //Value does not change for now. 

    .pause(MinThinkTime, MaxThinkTime)

  val CourtAdminSendToGateKeeper = 

    exec(http("XUI_PRL_XXX_440_SelectCase")
      .get(BaseURL + "/cases/case-details/#{caseId}/task")
      .headers(Headers.xuiHeader)
      .check(substring("HMCTS Manage cases")))

    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
      
    .exec(Common.activity)
    .exec(Common.configUI)
    .exec(Common.configJson)
    .exec(Common.userDetails)

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Click on 'Send to Gate Keeper'
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_490_SendToGateKeeper")
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
      .headers(Headers.navigationHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.id").is("sendToGateKeeper")))

      .exec(Common.userDetails)
      //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Add Gate Keeper
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_500_AddGateKeeper")
      .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLAddGateKeeper.json"))
      .check(substring("isJudgeOrLegalAdviserGatekeeping")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Send to Gate Keeper Submit
  ======================================================================================*/

    .group("XUI_PRL_XXX_510_GateKeeperSubmit") {
      exec(http("XUI_PRL_XXX_510_GateKeeperSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLAddGateKeeperSubmit.json"))
        .check(substring("gatekeepingDetails")))

      .exec(http("XUI_PRL_XXX_510_SelectCase")
        .get(BaseURL + "/data/internal/cases/#{caseId}")
        .headers(Headers.xuiHeader)
        .check(jsonPath("$.case_type.name").is("C100 & FL401 Applications")))
    } 

    .pause(MinThinkTime, MaxThinkTime)

  val CourtAdminManageOrders = 

    /*======================================================================================
    * Click on 'Manage Orders'
    ======================================================================================*/

    exec(http("XUI_PRL_XXX_515_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .check(jsonPath("$.tabs[6].fields[3].value.firstName").saveAs("ApplicantFirstName"))
      .check(jsonPath("$.tabs[6].fields[3].value.lastName").saveAs("ApplicantLastName"))
      .check(jsonPath("$.tabs[6].fields[8].value.firstName").saveAs("RespondentFirstName"))
      .check(jsonPath("$.tabs[6].fields[8].value.lastName").saveAs("RespondentLastName"))
      .check(jsonPath("$.case_id").is("#{caseId}")))

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Click on 'Manage Orders'
    ======================================================================================*/

    .group("XUI_PRL_XXX_520_ManageOrders") {
      exec(http("XUI_PRL_XXX_520_005_ManageOrders")
        .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/manageOrders?ignore-warning=false")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("manageOrders")))

        .exec(Common.userDetails)
    }
    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Create an Order
    ======================================================================================*/

    .group("XUI_PRL_XXX_530_CreateOrder") {
      exec(http("XUI_PRL_XXX_530_005_CreateOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders1")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLCreateOrder.json"))
        .check(substring("isSdoSelected")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Order - Blank Order
    ======================================================================================*/

    .group("XUI_PRL_XXX_540_SelectOrder") {
      exec(http("XUI_PRL_XXX_540_005_SelectOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin//PRLSelectOrderFL401.json"))
        .check(substring("caApplicant3InternalFlags")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Details
    ======================================================================================*/

    .group("XUI_PRL_XXX_550_OrderDetails") {
      exec(http("XUI_PRL_XXX_550_005_OrderDetails")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders5")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderDetailsFL401.json"))
        .check(substring("isEngDocGen")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Outcome
    ======================================================================================*/

    .group("XUI_PRL_XXX_560_HearingOutcome") {
      exec(http("XUI_PRL_XXX_560_005_HearingOutcome")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders12")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLHearingOutcome.json"))
        .check(jsonPath("$.data.previewOrderDoc.document_url").saveAs("document_url"))
        .check(jsonPath("$.data.previewOrderDoc.document_hash").saveAs("document_hash"))
        .check(substring("OrgPolicyCaseAssignedRole")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Type
    ======================================================================================*/

    .group("XUI_PRL_XXX_570_HearingType") {
      exec(http("XUI_PRL_XXX_570_005_HearingType")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders19")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLHearingType.json"))
        .check(jsonPath("$.data.previewOrderDoc.document_url").saveAs("document_url"))
        .check(jsonPath("$.data.previewOrderDoc.document_filename").saveAs("document_filename"))
        .check(jsonPath("$.data.previewOrderDoc.document_hash").saveAs("document_hash"))
        .check(jsonPath("$.data.ordersHearingDetails[0].id").saveAs("hearingId"))
        .check(substring("previewOrderDoc")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Order
    ======================================================================================*/

    .group("XUI_PRL_XXX_580_CheckYourOrder") {
      exec(http("XUI_PRL_XXX_580_005_CheckYourOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders20")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLCheckOrderFL401.json"))
        .check(substring("previewOrderDoc")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Recipients
    ======================================================================================*/

    .group("XUI_PRL_XXX_590_OrderRecipients") {
      exec(http("XUI_PRL_XXX_590_005_OrderRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders24")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderRecipientsFL401.json"))
        .check(substring("amendOrderSelectCheckOptions")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Serve
    ======================================================================================*/

    .group("XUI_PRL_XXX_600_OrderServe") {
      exec(http("XUI_PRL_XXX_600_005_OrderServe")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders26")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderServeFL401.json"))
        .check(jsonPath("$.data.serveOrderDynamicList.value[0].code").saveAs("orderCode"))
        .check(jsonPath("$.data.serveOrderDynamicList.value[0].label").saveAs("orderLabel"))
        .check(substring("orderRecipients")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order To Serve List
    ======================================================================================*/

    .group("XUI_PRL_XXX_610_OrderServe") {
      exec(http("XUI_PRL_XXX_610_005_OrderToServeList")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders27")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderToServeListFL401.json"))
        .check(substring("orderWithoutGivingNoticeToRespondent")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Serve to Respondent Options
    ======================================================================================*/

    .group("XUI_PRL_XXX_620_ServeToRespondentOptions") {
      exec(http("XUI_PRL_XXX_620_005_ServeToRespondentOptions")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders28")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderToServeRespondentOptionsFL401.json"))
        .check(substring("submitCountyCourtSelection")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Submit
    ======================================================================================*/

    .group("XUI_PRL_XXX_630_OrderSubmit") {
      exec(http("XUI_PRL_XXX_630_005_OrderSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderSubmitFL401.json"))
        .check(substring("JUDICIAL_REVIEW")))
    }

    .pause(MinThinkTime, MaxThinkTime)

val CourtAdminServiceApplication =

  /*======================================================================================
  * Click on 'Service of Application'
  ======================================================================================*/

    group("XUI_PRL_XXX_640_ServiceOfApplication") {
      exec(http("XUI_PRL_XXX_640_005_ServiceOfApplication")
        .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/serviceOfApplication?ignore-warning=false")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[7].value.list_items[0].code")saveAs("serviceOfApplicationScreenCode"))
        .check(jsonPath("$.case_fields[7].value.list_items[0].label")saveAs("serviceOfApplicationScreenLabel"))
        .check(jsonPath("$.id").is("serviceOfApplication")))

        .exec(Common.userDetails)
    }
    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Safe Of Notice letter Upload
  ======================================================================================*/

    .group("XUI_PRL_XXX_650_SoNUpload") {
      exec(http("XUI_PRL_XXX_650_005_SoNUpload")
        .post(BaseURL + "/documentsv2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "#{XSRFToken}")
        .bodyPart(RawFileBodyPart("files", "TestFile.pdf")
        .fileName("TestFile.pdf")
        .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "PRLAPPS")
        .formParam("jurisdictionId", "PRIVATELAW")
        .check(substring("originalDocumentName"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("documentHashSoN"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLSoN")))
     }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application document uploads
  ======================================================================================*/

    .group("XUI_PRL_XXX_660_DocumentUpload") {
      exec(http("XUI_PRL_660_005_DocumentUpload")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplication2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoADocumentsFL401.json"))
        .check(substring("additionalDocuments")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application Confirm recipients
  ======================================================================================*/

    .group("XUI_PRL_XXX_670_ServiceRecipients") {
      exec(http("XUI_PRL_XXX_670_005_ServiceRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplication4")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoARecipientsFL401.json"))
        .check(substring("soaServingRespondents")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application Submit
  ======================================================================================*/

    .group("XUI_PRL_XXX_680_ServiceSubmit") {
      exec(http("XUI_PRL_XXX_680_005_ServiceSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoASubmitFL401.json"))
        .check(jsonPath("$.data.caseInvites[0].value.accessCode").saveAs("prlAccessCodeApplicant"))
        .check(jsonPath("$.data.caseInvites[1].value.accessCode").saveAs("prlAccessCodeRespondent")))
    }

    //Write applicant access code to file
    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("FL401caseNumberAndCodeApplicant.csv", true))
      try {
        fw.write(session("caseId").as[String] + "," + session("prlAccessCodeApplicant").as[String] + "\r\n")
      } finally fw.close()
      session
    }
    //Write respondent access code to file
    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("FL401caseNumberAndCodeRespondent.csv", true))
      try {
        fw.write(session("caseId").as[String] + "," + session("prlAccessCodeRespondent").as[String] + "\r\n")
      } finally fw.close()
      session
    }

}
