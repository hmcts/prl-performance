package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

/*======================================================================================
* Create a new Probate application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_Probate {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  /*======================================================================================
  * Part 1 - Click the Create Case link
  ======================================================================================*/

  val CreateProbateCase =

    //Set session variables
    exec(_.setAll(  "caseEmail" -> ("exui-probate-" + Common.randomString(6) + "@mailtest.gov.uk"),
                    "appRef" -> ("RefPerf" + Common.randomString(6)),
                    "deceasedForename" -> ("Perf" + Common.randomString(5)),
                    "deceasedSurname" -> ("Test" + Common.randomString(5)),
                    "dobDay" -> Common.getDay(),
                    "dobMonth" -> Common.getMonth(),
                    "dobYear" -> Common.getDobYear(),
                    "dodDay" -> Common.getDay(),
                    "dodMonth" -> Common.getMonth(),
                    "dodYear" -> Common.getDodYear(),
                    "caseId" -> "0")) // initialise the caseId to 0 for the activity calls

    .group("XUI_Probate_030_CreateCase") {
      exec(http("XUI_Probate_030_005_CreateCase")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("PROBATE")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction = Manage Probate Applications; Case Type = Grant Of Representation; Event = Apply For Probate
    ======================================================================================*/

    .group("XUI_Probate_050_SelectCaseType") {
      exec(http("XUI_Probate_050_005_StartApplication")
        .get("/data/internal/case-types/GrantOfRepresentation/event-triggers/solicitorCreateApplication?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token_probate"))
        .check(jsonPath("$.id").is("solicitorCreateApplication")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Save & Continue
    ======================================================================================*/

    .group("XUI_Probate_060_CreateApplication") {
      exec(http("XUI_Probate_060_005_CreateApplication")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorCreateApplicationsolicitorCreateApplicationPage1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateCreateApplication1.json"))
        .check(substring("""{"data":{}""")))

      .exec(http("XUI_Probate_060_010_CreateApplicationDraft")
        .post("/data/internal/case-types/GrantOfRepresentation/drafts/")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-draft-create.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateCreateApplication1Draft.json"))
        .check(jsonPath("$.id").saveAs("draftId")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Postcode Lookup
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Complete the basic details for the application
    ======================================================================================*/

    .group("XUI_Probate_070_CreateApplication2") {
      exec(http("XUI_Probate_070_005_CreateApplication2")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorCreateApplicationsolicitorCreateApplicationPage2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateCreateApplication2.json")))

      .exec(http("XUI_Probate_070_010_Draft")
        .put("/data/internal/case-types/GrantOfRepresentation/drafts/${draftId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-draft-update.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateCreateApplication2Draft.json"))
        .check(status.in(200, 400, 500)))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers - Click Save & Continue
    ======================================================================================*/

    .group("XUI_Probate_080_CaseSubmitted") {
      exec(http("XUI_Probate_080_005_CaseSubmitted")
        .post("/data/case-types/GrantOfRepresentation/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateCaseSubmitted.json"))
        .check(jsonPath("$.id").optional.saveAs("caseId")))

      .exec(http("XUI_Probate_080_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.name").is("Application created (deceased details)"))
        .check(substring("Add Probate practitioner details")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Part 2 - Click 'Add deceased details' link
  ======================================================================================*/

  val AddDeceasedDetails =

    group("XUI_Probate_090_AddDeceasedDetails") {
      exec(http("XUI_Probate_090_005_DeceasedDetails")
        .get("/cases/case-details/${caseId}/trigger/solicitorUpdateApplication/solicitorUpdateApplicationsolicitorUpdateApplicationPage1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.userDetails)

      .exec(Common.configUI)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(http("XUI_Probate_090_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Application created (deceased details)")))

      .exec(http("XUI_Probate_090_015_UpdateApplication")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitorUpdateApplication?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token_probate"))
        .check(substring("solicitorUpdateApplication")))

      .exec(Common.caseActivityGet)

      .exec(Common.profile)

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Deceased Details - Name, DOD, DOB
    ======================================================================================*/

    .group("XUI_Probate_100_SubmitNameDodDob") {
      exec(http("XUI_Probate_100_005_SubmitNameDodDob")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateApplicationsolicitorUpdateApplicationPage1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateDeceasedNameDodDob.json"))
        .check(substring("deceasedDateOfDeath")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Postcode Lookup
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Deceased Details - Domicile, Address and Tax
    ======================================================================================*/

    .group("XUI_Probate_110_SubmitNameDodDob") {
      exec(http("XUI_Probate_110_005_SubmitNameDodDob")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateApplicationsolicitorUpdateApplicationPage2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateDeceasedAddressIHT.json"))
        .check(substring("deceasedAddress")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Type of Application
    ======================================================================================*/

      .group("XUI_Probate_120_SubmitTypeOfApplication") {
        exec(http("XUI_Probate_120_005_SubmitTypeOfApplication")
          .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateApplicationsolicitorUpdateApplicationPage3")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/probate/ProbateTypeOfApplication.json"))
          .check(substring("solsWillType")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Estate in England or Wales
    ======================================================================================*/

    .group("XUI_Probate_130_SubmitEnglandOrWales") {
      exec(http("XUI_Probate_130_005_SubmitEnglandOrWales")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateApplicationsolicitorUpdateApplicationPage4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateEnglandOrWales.json"))
        .check(substring("appointExec")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confirm deceased details - check your answers
    ======================================================================================*/

    .group("XUI_Probate_140_ConfirmDeceasedDetails") {
      exec(http("XUI_Probate_140_005_ConfirmDeceasedDetails")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateConfirmDeceasedDetails.json"))
        .check(jsonPath("$.state").is("SolProbateCreated")))

      .exec(Common.activity)

      .exec(http("XUI_Probate_140_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"solicitorUpdateApplication","jurisdiction":"PROBATE","caseTypeId":"GrantOfRepresentation"}}"""))
        .check(status.in(200, 400)))

      .exec(http("XUI_Probate_140_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Grant of probate created")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Part 3 - Click 'Add application details' link
  ======================================================================================*/

  val AddApplicationDetails =

    group("XUI_Probate_150_GrantOfProbateDetails") {
      exec(http("XUI_Probate_150_005_GrantOfProbateDetails")
        .get("/cases/case-details/${caseId}/trigger/solicitorUpdateProbate/solicitorUpdateProbatesolicitorUpdateProbatePage1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.userDetails)

      .exec(Common.configUI)

      .exec(Common.isAuthenticated)

      .exec(http("XUI_Probate_150_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Grant of probate created")))

      .exec(http("XUI_Probate_150_015_GrantOfProbateDetails")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitorUpdateProbate?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token_probate"))
        .check(substring("solicitorUpdateProbate")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Grant of Probate Details
    ======================================================================================*/

    //first set the will and codicil dates:
    //    - will is dated 10 years before date of death
    //    - codicil is dated 8 years before date of death
    .exec { session =>
      session
        .set("willYear", session("dodYear").as[Int] - 10)
        .set("codicilYear", session("dodYear").as[Int] - 8)
    }

    .group("XUI_Probate_160_SubmitGrantOfProbateDetails") {
      exec(http("XUI_Probate_160_005_SubmitGrantOfProbateDetails")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateProbatesolicitorUpdateProbatePage1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateGrantOfProbateDetails.json"))
        .check(substring("willHasCodicils"))
        .check(jsonPath("$.data.codicilAddedDateList[0].id").saveAs("newCodicilId")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Postcode Lookup for Firm
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Executor Details
    ======================================================================================*/

    .group("XUI_Probate_170_SubmitExecutorDetails") {
      exec(http("XUI_Probate_170_005_SubmitExecutorDetails")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateProbatesolicitorUpdateProbatePage2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateExecutorDetails.json"))
        .check(jsonPath("$.data.titleAndClearingType").is("TCTPartSuccPowerRes"))
        .check(jsonPath("$.data.otherPartnersApplyingAsExecutors[0].id").saveAs("newSolExecId")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Postcode Lookup for new executor
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Extra Executor Details
    ======================================================================================*/

    .group("XUI_Probate_180_SubmitExtraExecDetails") {
      exec(http("XUI_Probate_180_005_SubmitExtraExecDetails")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateProbatesolicitorUpdateProbatePage4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateExtraExecDetails.json"))
        .check(jsonPath("$.data.solsAdditionalExecutorList[0].id").saveAs("newExecId")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Extra Executor Comments
    ======================================================================================*/

    .group("XUI_Probate_190_SubmitExtraEvidence") {
      exec(http("XUI_Probate_190_005_SubmitExtraEvidence")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateProbatesolicitorUpdateProbatePage5")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateExtraEvidence.json"))
        .check(substring("furtherEvidenceForApplication")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Extra Information
    ======================================================================================*/

    .group("XUI_Probate_200_SubmitExtraInfo") {
      exec(http("XUI_Probate_200_005_SubmitExtraInfo")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorUpdateProbatesolicitorUpdateProbatePage6")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateExtraInfo.json"))
        .check(substring("solsAdditionalInfo")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confirm grant of probate details - check your answers
    ======================================================================================*/

    .group("XUI_Probate_210_ConfirmGrantOfProbateDetails") {
      exec(http("XUI_Probate_210_005_ConfirmGrantOfProbateDetails")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateConfirmGrantOfProbateDetails.json"))
        .check(jsonPath("$.state").is("SolAppUpdated")))

      .exec(http("XUI_Probate_210_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"solicitorUpdateProbate","jurisdiction":"PROBATE","caseTypeId":"GrantOfRepresentation"}}"""))
        .check(status.in(200, 400)))

      .exec(http("XUI_Probate_210_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Application updated")))

      .exec(Common.activity)
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Part 4 - Click 'Review and sign legal statement and submit application'
  ======================================================================================*/

  val ReviewAndSubmitApplication =

    group("XUI_Probate_220_CompleteAppliction") {
      exec(http("XUI_Probate_220_005_CompleteApplication")
        .get("/cases/case-details/1623056630472980/trigger/solicitorReviewAndConfirm/solicitorReviewAndConfirmsolicitorReviewLegalStatementPage1")
        .headers(Headers.navigationHeader)
        .check(substring("HMCTS Manage cases")))

      .exec(Common.activity)

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.userDetails)

      .exec(Common.configUI)

      .exec(Common.isAuthenticated)

      .exec(http("XUI_Probate_220_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Application updated")))

      .exec(http("XUI_Probate_220_015_ReviewAndConfirm")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitorReviewAndConfirm?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.case_fields[?(@.id=='solsLegalStatementDocument')].value.document_url").saveAs("documentURL"))
        .check(jsonPath("$.event_token").saveAs("event_token_probate"))
        .check(jsonPath("$.id").is("solicitorReviewAndConfirm")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Legal Statement
    ======================================================================================*/

    .group("XUI_Probate_230_SubmitLegalStatement") {
      exec(http("XUI_Probate_230_005_SubmitLegalStatement")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorReviewAndConfirmsolicitorReviewLegalStatementPage1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateLegalStatement.json"))
        .check(substring("solsLegalStatementDocument")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Document
    ======================================================================================*/

      .group("XUI_Probate_240_UploadStatementOfTruth") {
        exec(http("XUI_Probate_240_005_UploadStatementOfTruth")
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
          .formParam("caseTypeId", "GrantOfRepresentation")
          .formParam("jurisdictionId", "PROBATE")
          .check(substring("originalDocumentName"))
          .check(jsonPath("$.documents[0].hashToken").saveAs("documentHash"))
          .check(jsonPath("$.documents[0]._links.self.href").saveAs("SOTDocumentURL")))
      }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
     * Submit Legal Statement
     ======================================================================================*/

    .group("XUI_Probate_250_SubmitSOTDoc") {
      exec(http("XUI_Probate_250_005_SubmitSOTDoc")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorReviewAndConfirmsolicitorReviewLegalStatementPage3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateSOTDoc.json"))
        .check(substring("document_filename")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Statement of Truth (SOT)
    ======================================================================================*/

    .group("XUI_Probate_260_SubmitStatementOfTruth") {
      exec(http("XUI_Probate_260_005_SubmitStatementOfTruth")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorReviewAndConfirmsolicitorReviewLegalStatementPage4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateReviewAndConfirm.json"))
        .check(substring("BelieveTrue")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Extra Copies
    ======================================================================================*/

    .group("XUI_Probate_270_SubmitExtraCopies") {
      exec(http("XUI_Probate_270_005_SubmitExtraCopies")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorReviewAndConfirmsolicitorConfirmPage2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateExtraCopies.json"))
        .check(substring("extraCopiesOfGrant")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Payment Method
    ======================================================================================*/

    .group("XUI_Probate_280_SubmitPaymentMethod") {
      exec(http("XUI_Probate_280_005_SubmitPaymentMethod")
        .post("/data/case-types/GrantOfRepresentation/validate?pageId=solicitorReviewAndConfirmsolicitorConfirmPage3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbatePaymentMethod.json"))
        .check(substring("solsPaymentMethods")))

      .exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Application
    ======================================================================================*/

    .group("XUI_Probate_290_SubmitApplication") {
      exec(http("XUI_Probate_290_005_SubmitApplication")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/probate/ProbateSubmitApplication.json"))
        .check(jsonPath("$.state").is("CaseCreated"))
        .check(substring("This probate application has now been submitted")))

      .exec(http("XUI_Probate_290_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"solicitorReviewAndConfirm","jurisdiction":"PROBATE","caseTypeId":"GrantOfRepresentation"}}"""))
        .check(status.in(200, 401)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Return to Case
    ======================================================================================*/

    .group("XUI_Probate_300_ViewCase") {
      exec(http("XUI_Probate_300_005_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.id").is("CaseCreated")))
    }

    .pause(MinThinkTime, MaxThinkTime)

}
