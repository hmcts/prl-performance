package scenarios

import java.text.SimpleDateFormat
import java.util.Date

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Environment, Common, Headers}

object Solicitor_IAC {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val sdfDate = new SimpleDateFormat("yyyy-MM-dd")
  val now = new Date()
  val timeStamp = sdfDate.format(now)

  /*======================================================================================
  * IAC Case Creation
  ======================================================================================*/

  val CreateIACCase =

    //set session variables
    exec(_.setAll( "firstName" -> ("Perf" + Common.randomString(5)),
                    "lastName" -> ("Test" + Common.randomString(5)),
                    "dobDay" -> Common.getDay(),
                    "dobMonth" -> Common.getMonth(),
                    "dobYear" -> Common.getDobYear(),
                    "currentDate" -> timeStamp))

  /*======================================================================================
  *Business process : Following business process is for IAC Case Creation
  *Below group contains all the requests are when clicking on create case
  ======================================================================================*/

    .group("XUI_IAC_040_CreateCase") {
      exec(http("XUI_IAC_040_CreateCase")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("IA")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-create%2FIA%2FAsylum%2FstartAppeal"))
    }
    
    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  *Business process : Following business process is for IAC Case Creation
  *Below group contains all the requests when starting create case
  ======================================================================================*/
      
    .group("XUI_IAC_050_StartCreateCase1") {
      exec(http("XUI_IAC_050_005_StartCreateCase1")
        .get("/data/internal/case-types/Asylum/event-triggers/startAppeal?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(substring("Start your appeal")))

      .exec(Common.profile)

      .exec(Common.healthcheck("%2Fcases%2Fcase-create%2FIA%2FAsylum%2FstartAppeal%2FstartAppealchecklist"))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  *Business process : Following business process is for IAC Case Creation
  *Below group contains all the requests for starting appeal checklist
  ======================================================================================*/

    .group("XUI_IAC_060_StartAppealChecklist") {
      exec(http("XUI_IAC_060_StartAppealChecklist")
        .post("/data/case-types/Asylum/validate?pageId=startAppealchecklist")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACStartChecklist.json"))
        .check(substring("isOutOfCountryEnabled")))
    }
    .pause(MinThinkTime, MaxThinkTime)

    .group("XUI_IAC_070_StartAppealOutOfCountry") {
      exec(http("XUI_IAC_070_StartAppealOutOfCountry")
        .post("/data/case-types/Asylum/validate?pageId=startAppealoutOfCountry")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACOutOfCountry.json"))
        .check(substring("appellantInUk")))
    }
    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  *Business process : Following business process is for IAC Case Creation
  *Below group contains all the requests for appealing home office decision
  ======================================================================================*/

    .group("XUI_IAC_080_StartAppealHomeOfficeDecision") {
      exec(http("XUI_IAC_080_StartAppealHomeOfficeDecision")
      .post("/data/case-types/Asylum/validate?pageId=startAppealhomeOfficeDecision")
      .headers(Headers.commonHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "${XSRFToken}")
      .body(ElFileBody("bodies/iac/IACHomeOfficeDecision.json"))
      .check(substring("haveHearingAttendeesAndDurationBeenRecorded")))
    }
    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for uploading notification Decision
======================================================================================*/

    .group("XUI_IAC_090_UploadNoticeDecision") {
      exec(http("XUI_IAC_090_005_UploadNoticeDecision")
        .post("/documentsv2")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "${XSRFToken}")
        .bodyPart(RawFileBodyPart("files", "3MB.pdf")
        .fileName("3MB.pdf")
        .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "null")
        .formParam("jurisdictionId", "null")
        // .formParam("files", """(binary)""")
        .check(substring("originalDocumentName"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("documentHash")))
    }

    .group("XUI_IAC_090_010_StartUploadNoticeDecision") {
      exec(http("XUI_IAC_090_010_StartUploadNoticeDecision")
      .post("/data/case-types/Asylum/validate?pageId=startAppealuploadTheNoticeOfDecision")
      .headers(Headers.commonHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "${XSRFToken}")
      .body(ElFileBody("bodies/iac/IACUploadNoticeDecision.json"))
      .check(substring("uploadTheNoticeOfDecisionDocs")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for Appeal Basic Details
======================================================================================*/

    .group("XUI_IAC_100_StartAppealBasicDetails") {
      exec(http("XUI_IAC_100_StartAppealBasicDetails")
        .post("/data/case-types/Asylum/validate?pageId=startAppealappellantBasicDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppellantBasicDetails.json"))
        .check(substring("appellantGivenNames")))
    }

    .pause(MinThinkTime, MaxThinkTime)

 /*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for Appealant nationality
======================================================================================*/

    .group("XUI_IAC_110_StartAppealantNationality") {
      exec(http("XUI_IAC_110_StartAppealantNationality")
        .post("/data/case-types/Asylum/validate?pageId=startAppealappellantNationalities")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppellantNationalities.json"))
        .check(substring("hasNationality")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
* Below group contains all the requests for Appealant address search
======================================================================================*/
    
    .exec(Common.postcodeLookup)

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
* Below group contains all the requests for Appealant address
======================================================================================*/

    .group("XUI_IAC_130_StartAppealAppellantAddress") {
      exec(http("XUI_IAC_130_StartAppealAppellantAddress")
        .post("/data/case-types/Asylum/validate?pageId=startAppealappellantAddress")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppellantAddress.json"))
        .check(substring("appellantHasFixedAddress")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for contact preference
======================================================================================*/
      
    .group("XUI_IAC_140_AppellantContactPref") {
      exec(http("XUI_IAC_140_AppellantContactPref")
      .post("/data/case-types/Asylum/validate?pageId=startAppealappellantContactPreference")
      .headers(Headers.commonHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "${XSRFToken}")
      .body(ElFileBody("bodies/iac/IACContactPreference.json"))
      .check(substring("contactPreference")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for entering the details of appeal type
======================================================================================*/

    .group("XUI_IAC_150_StartAppealAppealType") {
      exec(http("XUI_IAC_150_StartAppealAppealType")
        .post("/data/case-types/Asylum/validate?pageId=startAppealappealType")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppealType.json"))
        .check(substring("appealType")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for entering the details of ground revocation
======================================================================================*/

    .group("XUI_IAC_160_StartAppealGroundsRevocation") {
      exec(http("XUI_IAC_160_StartAppealGroundsRevocation")
        .post("/data/case-types/Asylum/validate?pageId=startAppealappealGroundsHumanRightsRefusal")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppealGrounds.json"))
        .check(substring("appealGroundsDecisionHumanRightsRefusal")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for entering the details of appeal new matters
======================================================================================*/

    .group("XUI_IAC_170_StartAppealNewMatters") {
      exec(http("XUI_IAC_170_StartAppealNewMatters")
        .post("/data/case-types/Asylum/validate?pageId=startAppealdeportationOrderPage")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACDeportationOrder.json"))
        .check(substring("deportationOrderOptions")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for entering the details of appeal new matters
======================================================================================*/

    .group("XUI_IAC_180_StartAppealNewMatters") {
      exec(http("XUI_IAC_180_StartAppealNewMatters")
        .post("/data/case-types/Asylum/validate?pageId=startAppealnewMatters")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACNewMatters.json"))
        .check(substring("hasNewMatters")))
    }
    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for entering the details if appealant has any other appeals
======================================================================================*/

    .group("XUI_IAC_190_StartAppealHasOtherAppeals") {
      exec(http("XUI_IAC_190_StartAppealHasOtherAppeals")
        .post("/data/case-types/Asylum/validate?pageId=startAppealhasOtherAppeals")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACOtherAppeals.json"))
        .check(substring("hasOtherAppeals")))
    }
    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC  Case Creation
*Below group contains all the requests for entering the details of appeallant legal representative details
======================================================================================*/

    .group("XUI_IAC_200_StartAppealLegalRepresentative") {
      exec(http("XUI_IAC_200_StartAppealLegalRepresentative")
        .post("/data/case-types/Asylum/validate?pageId=startAppeallegalRepresentativeDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACLegalRepresentative.json"))
        .check(substring("legalRepReferenceNumber")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)


/*======================================================================================
*Business process : Following business process is for IAC  Case Creation
*Below group contains all the requests for selecting appeal type and fee
======================================================================================*/

    .group("XUI_IAC_210_StartAppealFeeDecision") {
      exec(http("XUI_IAC_210_StartAppealFeeDecision")
        .post("/data/case-types/Asylum/validate?pageId=startAppealhearingFeeDecision")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppealFeeDecision.json"))
        .check(substring("decisionHearingFeeOption")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC  Case Creation
*Below group contains all the requests for selecting Remission Type
======================================================================================*/

    .group("XUI_IAC_220_StartAppealPayByAccount") {
      exec(http("XUI_IAC_220_StartAppealPayByAccount")
        .post("/data/case-types/Asylum/validate?pageId=startAppealpaymentOptions")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppealPaymentOption.json"))
        .check(substring("howToPayLabel")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC  Case Creation
*Below group contains all the requests for selecting Remission Type
======================================================================================*/

  .group("XUI_IAC_230_StartAppealPaymentType") {
      exec(http("XUI_IAC_230_StartAppealPaymentType")
        .post("/data/case-types/Asylum/validate?pageId=startAppealpaymentOptions")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppealPaymentOption.json"))
        .check(substring("howToPayLabel"))
        // .check(jsonPath("").saveAs("legalRepName"))
        )

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for starting appeal case save
======================================================================================*/

    .group("XUI_IAC_240_005_StartAppealCaseSave") {
      exec(http("XUI_IAC_240_StartAppealCaseSave")
        .post("/data/case-types/Asylum/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACSaveCase.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(substring("appealStarted")))

      .exec(http("XUI_IAC_240_005_Allocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"startAppeal","jurisdiction":"IA","caseTypeId":"Asylum"}}"""))
        .check(status.in(200, 400))
        .check(substring("tasks")))
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for starting start submit appeal
======================================================================================*/

    .group("XUI_IAC_250_005_StartSubmitAppeal") {
      exec(http("XUI_IAC_250_005_StartSubmitAppeal")
        .get("/case/IA/Asylum/${caseId}/trigger/payAndSubmitAppeal")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage")))

      .exec(Common.configurationui)

      .exec(Common.configUI)

      .exec(Common.TsAndCs)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FpayAndSubmitAppeal"))

      .exec(http("XUI_IAC_250_035_SaveCaseView")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(substring("Create")))
    }

    .exec(Common.caseActivityGet)
    .pause(2)
    .exec(Common.caseActivityPost)

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for starting submit appeal
======================================================================================*/

    .group("XUI_IAC_260_PayAndSubmitAppeal") {
      exec(http("XUI_IAC_260_005_PayAndSubmitAppeal")
      .get("/data/internal/cases/${caseId}/event-triggers/payAndSubmitAppeal?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.event_token").saveAs("event_token_submit"))
        .check(substring("Pay and submit")))

      .exec(Common.isAuthenticated)

      .exec(Common.userDetails)

      .exec(Common.profile)

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsubmitAppeal%2FsubmitAppealdeclaration"))

      .exec(Common.caseActivityGet)
      .pause(2)
      .exec(Common.caseActivityPost)

    }
    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
*Below group contains all the requests for selecting PBA account and clicking continue
======================================================================================*/

    .group("XUI_IAC_270_SelectPBAAccount") {
      exec(http("XUI_IAC_270_005_SelectPBAAccount")
        .post("/data/case-types/Asylum/validate?pageId=payAndSubmitAppealenterPbaNumber")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACPBAAccountSelect.json"))
        .check(substring("paymentAccountList")))

      .exec(Common.profile)

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsubmitAppeal%2FpayAndSubmitAppealdeclaration"))

      .exec(Common.caseActivityGet)
      .pause(2)
      .exec(Common.caseActivityPost)
      
    }

    .pause(MinThinkTime, MaxThinkTime)

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
* Below group contains all the requests for confirmation statement and clicking continue
======================================================================================*/

  .group("XUI_IAC_280_ConfirmDeclaration") {
      exec(http("XUI_IAC_280_005_ConfirmDeclaration")
        .post("/data/case-types/Asylum/validate?pageId=payAndSubmitAppealdeclaration")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACAppealDeclaration.json"))
        .check(substring("hasDeclared")))

      .exec(Common.profile)

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FspayAndSubmitAppeal%2Fsubmit"))
  }

/*======================================================================================
*Business process : Following business process is for IAC Case Creation
* Below group contains all the requests for starting submit appeal declaration submitted
======================================================================================*/

    .group("XUI_IAC_290_005_AppealDeclarationSubmitted") {
      exec(http("XUI_IAC_290_AppealDeclarationSubmitted")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/iac/IACSubmitAppeal.json"))
        .check(substring("hasDeclared")))

      .exec(http("XUI_IAC_290_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"submitAppeal","jurisdiction":"IA","caseTypeId":"Asylum"}}"""))
        .check(status.in(200, 400))
        .check(substring("tasks")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FpayAndSubmitAppeal%2Fconfirm"))
    }

    .exec(Common.caseActivityGet)
    .pause(2)
    .exec(Common.caseActivityPost)

    .pause(MinThinkTime, MaxThinkTime)

/*====================================================================================
* IAC share a case
====================================================================================*/

  val shareacase =

    group("XUI_IAC_300_ShareACase") {
      exec(http("XUI_IAC_300_005_ShareACase")
        .get("/api/caseshare/cases?case_ids=${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .check(jsonPath("$..email").find(0).saveAs("user0"))
        .check(jsonPath("$..firstName").find(0).saveAs("firstName"))
        .check(jsonPath("$..lastName").find(0).saveAs("lastName"))
        .check(jsonPath("$..idamId").find(0).saveAs("idamId")))
    
      .exec(http("XUI_IAC_300_010_ShareACaseUsers")
        .get("/api/caseshare/users")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .check(jsonPath("$..email").find(0).saveAs("user1"))
        .check(jsonPath("$..firstName").find(0).saveAs("firstName1"))
        .check(jsonPath("$..lastName").find(0).saveAs("lastName1"))
        .check(jsonPath("$..idamId").find(0).saveAs("idamId1")))
    }

    .pause(MinThinkTime , MaxThinkTime)

    .group("XUI_IAC_310_ShareACaseConfirm") {
      exec(http("XUI_IAC_310_ShareACaseAssignments")
        .post("/api/caseshare/case-assignments")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .body(ElFileBody("bodies/iac/IACShareACase.json")))
    }

    .pause(MinThinkTime , MaxThinkTime )

}