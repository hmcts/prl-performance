package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

/*======================================================================================
* Create a new Divorce application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_NFD {

  val IdamUrl = Environment.idamURL
  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CreateNFDCase =

    //set session variables
    exec(_.setAll(
      "applicant1FirstName" -> ("App1" + Common.randomString(5)),
      "applicant1LastName" -> ("Test" + Common.randomString(5)),
      "applicant2FirstName" -> ("App2" + Common.randomString(5)),
      "applicant2LastName" -> ("Test" + Common.randomString(5)),
      "marriageDay" -> Common.getDay(),
      "marriageMonth" -> Common.getMonth(),
      "marriageYear" -> Common.getMarriageYear()))

    /*======================================================================================
    * Click the Create Case link
    ======================================================================================*/

    .group("XUI_NFD_030_CreateCase") {
      exec(Common.healthcheck("%2Fcases%2Fcase-filter"))

      .exec(http("XUI_NFD_030_005_CreateCase")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("DIVORCE")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction = Family Divorce; Case Type = New Law Case; Event = Apply: divorce or dissolution
    ======================================================================================*/

    .group("XUI_NFD_040_SelectCaseType") {
      exec(Common.healthcheck("%2Fcases%2Fcase-create%2FDIVORCE%2FNFD%2Fsolicitor-create-application"))

      .exec(http("XUI_NFD_040_005_StartApplication")
        .get("/data/internal/case-types/NFD/event-triggers/solicitor-create-application?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitor-create-application")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-create%2FDIVORCE%2FNFD%2Fsolicitor-create-application%2Fsolicitor-create-applicationhowDoYouWantToApplyForDivorce"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select case type - Divorce (Sole)
    ======================================================================================*/

    .group("XUI_NFD_050_ChooseSoleOrJoint") {
      exec(http("XUI_NFD_050_005_ChooseSoleOrJoint")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationhowDoYouWantToApplyForDivorce")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDCaseType.json"))
        .check(substring("applicationType")))

      //select respondent solicitor org now, as this call will be retrieved from cache in future
      //requests, where checks aren't performed by Gatling https://gatling.io/docs/gatling/reference/current/http/protocol/#caching
      .exec(http("XUI_NFD_050_010_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * About the Solicitor
    ======================================================================================*/

    .group("XUI_NFD_060_AboutTheSolicitor") {
      exec(http("XUI_NFD_060_005_AboutTheSolicitor")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationSolAboutTheSolicitor")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDApplicant1SolicitorDetails.json"))
        .check(substring("applicant1SolicitorName")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Has the Marriage broken down?
    ======================================================================================*/

    .group("XUI_NFD_070_MarriageBrokenDown") {
      exec(http("XUI_NFD_070_005_MarriageBrokenDown")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationMarriageIrretrievablyBroken")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDMarriageBrokenDown.json"))
        .check(substring("applicant1ScreenHasMarriageBroken")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * About the Applicant
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .group("XUI_NFD_080_AboutTheApplicant") {
      exec(http("XUI_NFD_080_005_AboutTheApplicant")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationSolAboutApplicant1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDApplicant1Details.json"))
        .check(substring("applicant1FirstName")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * About the Other Party
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .group("XUI_NFD_090_AboutTheOtherParty") {
      exec(http("XUI_NFD_090_005_AboutTheOtherParty")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationSolAboutApplicant2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDApplicant2Details.json"))
        .check(substring("applicant2FirstName")))

      .exec(http("XUI_NFD_090_010_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Is the Respondent Represented by a Solicitor?
    ======================================================================================*/

    //Not calling postcode lookup here, as it will override the applicant's address in the session.
    // Will just use the same address for both applicants

    //.exec(Common.postcodeLookup)

    .group("XUI_NFD_100_RespondentRepresented") {
      exec(http("XUI_NFD_100_005_RespondentRepresented")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationApplicant2ServiceDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDApplicant2SolicitorDetails.json"))
        .check(substring("applicant2SolicitorName")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Marriage Certificate Details
    ======================================================================================*/

    .group("XUI_NFD_110_MarriageCertificateDetails") {
      exec(http("XUI_NFD_110_005_MarriageCertificateDetails")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationMarriageCertificateDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDMarriageCertificateDetails.json"))
        .check(substring("marriageDate")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction - Both applicants domiciled in the UK (connection = F)
    ======================================================================================*/

    .group("XUI_NFD_120_Jurisdiction") {
      exec(http("XUI_NFD_120_005_Jurisdiction")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationJurisdictionApplyForDivorce")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJurisdiction.json"))
        .check(substring("jurisdictionConnections")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Other Legal Proceedings - No
    ======================================================================================*/

    .group("XUI_NFD_130_OtherLegalProceedings") {
      exec(http("XUI_NFD_130_005_OtherLegalProceedings")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationOtherLegalProceedings")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDOtherLegalProceedings.json"))
        .check(substring("applicant1LegalProceedings")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Financial Order - No
    ======================================================================================*/

    .group("XUI_NFD_140_FinancialOrder") {
      exec(http("XUI_NFD_140_005_FinancialOrder")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationFinancialOrders")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDFinancialOrder.json"))
        .check(substring("applicant1FinancialOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Document
    ======================================================================================*/

    .group("XUI_NFD_150_UploadDocument") {
      exec(http("XUI_NFD_150_005_UploadDocument")
        .post("/documents")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("X-XSRF-TOKEN", "${XSRFToken}")
        .bodyPart(RawFileBodyPart("files", "3MB.pdf")
          .fileName("3MB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .check(jsonPath("$._embedded.documents[0]._links.self.href").saveAs("DocumentURL")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Document
    ======================================================================================*/

    .group("XUI_NFD_160_SubmitDocument") {
      exec(http("XUI_NFD_160_005_SubmitDocument")
        .post("/data/case-types/NFD/validate?pageId=solicitor-create-applicationUploadSupportingDocuments")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDSubmitDocument.json"))
        .check(substring("applicant1DocumentsUploaded")))

      .exec(Common.userDetails)

      .exec(http("XUI_NFD_160_010_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_NFD_170_CheckYourAnswers") {
      exec(http("XUI_NFD_170_005_CheckYourAnswers")
        .post("/data/case-types/NFD/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDCheckYourAnswers.json"))
        .check(jsonPath("$.state").is("Draft"))
        .check(jsonPath("$.id").saveAs("caseId")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_170_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("Draft")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val JointInviteApplicant2 =

    /*======================================================================================
    * Joint Select 'Invite Applicant 2' from the dropdown
    ======================================================================================*/

    group("XUI_NFD_180_JointInviteApplicant2") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Finvite-applicant2"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_180_005_JointInviteApplicant2")
        .get("/data/internal/cases/${caseId}/event-triggers/invite-applicant2?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("invite-applicant2")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Finvite-applicant2%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Save and Continue
    ======================================================================================*/

    .group("XUI_NFD_190_JointSaveInviteApplicant2") {
      exec(http("XUI_NFD_190_005_JointSaveInviteApplicant2")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointSaveInviteApplicant2.json"))
        .check(jsonPath("$.state").is("AwaitingApplicant2Response")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_190_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AwaitingApplicant2Response")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val SubmitJointApplication =

    /*======================================================================================
    * View Case
    ======================================================================================*/

    group("XUI_NFD_195_ViewCase") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_195_005_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AwaitingApplicant2Response")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Select 'Submit Joint Application' from the dropdown
    ======================================================================================*/

    .group("XUI_NFD_200_JointStartSubmitApplication") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_200_005_JointStartSubmitApplication")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitor-submit-joint-application?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[?(@.id=='applicant1SolicitorAnswersLink')].value.document_filename").saveAs("app1AnswersFilename"))
        .check(jsonPath("$.case_fields[?(@.id=='applicant1SolicitorAnswersLink')].value.document_url").saveAs("app1AnswersDocumentURL"))
        .check(jsonPath("$.id").is("solicitor-submit-joint-application")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application%2Fsolicitor-submit-joint-applicationMarriageIrretrievablyBroken"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Marriage Broken Down
    ======================================================================================*/

    .group("XUI_NFD_210_JointMarriageBrokenDown") {
      exec(http("XUI_NFD_210_005_JointMarriageBrokenDown")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-joint-applicationMarriageIrretrievablyBroken")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointMarriageBrokenDown.json"))
        .check(substring("applicant2ScreenHasMarriageBroken")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application%2Fsolicitor-submit-joint-applicationFinancialOrdersForApplicant2"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Financial Orders
    ======================================================================================*/

    .group("XUI_NFD_220_JointFinancialOrder") {
      exec(http("XUI_NFD_220_005_JointFinancialOrder")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-joint-applicationFinancialOrdersForApplicant2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointFinancialOrder.json"))
        .check(substring("applicant2FinancialOrder")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application%2Fsolicitor-submit-joint-applicationHelpWithFeesPageForApplicant2"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)
/*
    /*======================================================================================
    * Joint Help with Fees
    ======================================================================================*/

    .group("XUI_NFD_230_JointHelpWithFees") {
      exec(http("XUI_NFD_230_005_JointHelpWithFees")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-joint-applicationHelpWithFeesPageForApplicant2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointHelpWithFees.json"))
        .check(substring("applicant2NeedsHelpWithFees")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application%2Fsolicitor-submit-joint-applicationcheckTheirAnswers"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

 */

    /*======================================================================================
    * Joint Any Corrections
    ======================================================================================*/

    .group("XUI_NFD_240_JointAnyCorrections") {
      exec(http("XUI_NFD_240_005_JointAnyCorrections")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-joint-applicationcheckTheirAnswers")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointAnyCorrections.json"))
        .check(substring("applicant2ConfirmApplicant1Information")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application%2Fsolicitor-submit-joint-applicationSolStatementOfTruthApplicant2"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Statement of Truth (Applicant 2)
    ======================================================================================*/

    .group("XUI_NFD_250_JointApp2StatementOfTruth") {
      exec(http("XUI_NFD_250_005_JointApp2StatementOfTruth")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-joint-applicationSolStatementOfTruthApplicant2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointApp2SOT.json"))
        .check(substring("applicant2SolSignStatementOfTruth")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-joint-application%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint App2 Submit Application
    ======================================================================================*/

    .group("XUI_NFD_260_JointApp2SubmitApplication") {
      exec(http("XUI_NFD_260_005_JointApp2SubmitApplication")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointSubmitApplication.json"))
        //TODO: Fix the below once NFD resolve this issue. The case state isn't updating in time, so sometimes the case state is stale
        .check(jsonPath("$.state").in("AwaitingApplicant2Response", "Applicant2Approved")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_260_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        //TODO: Fix the below once NFD resolve this issue. The case state isn't updating in time, so sometimes the case state is stale
        .check(jsonPath("$.state.id").in("AwaitingApplicant2Response", "Applicant2Approved")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val SignAndSubmitSole =

    /*======================================================================================
    * Select 'Sign and submit' from the dropdown
    ======================================================================================*/

    group("XUI_NFD_270_StartEventSignAndSubmit") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_270_005_StartEventSignAndSubmit")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitor-submit-application?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitor-submit-application"))
        .check(jsonPath("$.case_fields[?(@.id=='solApplicationFeeInPounds')].value").saveAs("feeInPounds"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeAmount").saveAs("feeAmount"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeCode").saveAs("feeCode"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeDescription").saveAs("feeDescription"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeVersion").saveAs("feeVersion"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.PaymentTotal").saveAs("paymentTotal")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolStatementOfTruth"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Statement Of Truth
    ======================================================================================*/

    .group("XUI_NFD_280_StatementOfTruth") {
      exec(http("XUI_NFD_280_005_StatementOfTruth")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolStatementOfTruth")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDStatementOfTruth.json"))
        .check(substring("solSignStatementOfTruth")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolPayment"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Payment Method
    ======================================================================================*/

    .group("XUI_NFD_290_PaymentMethod") {
      exec(http("XUI_NFD_290_005_PaymentMethod")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolPayment")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDPaymentMethod.json"))
        .check(substring("pbaNumbers")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolPayAccount"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select PBA Account
    ======================================================================================*/

    .group("XUI_NFD_300_PaymentDetails") {
      exec(http("XUI_NFD_300_005_PaymentDetails")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolPayAccount")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDPaymentDetails.json"))
        .check(substring("feeAccountReference")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolPaymentSummary"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Summary
    ======================================================================================*/

    .group("XUI_NFD_310_OrderSummary") {
      exec(http("XUI_NFD_310_005_OrderSummary")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolPaymentSummary")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDPaymentSummary.json"))
        .check(substring("applicationFeeOrderSummary")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Sign & Submit: Check Your Answers
    ======================================================================================*/

    .group("XUI_NFD_320_SignCheckYourAnswers") {
      exec(http("XUI_NFD_320_005_SignCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDSignCheckYourAnswers.json"))
        .check(jsonPath("$.state").is("Submitted")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_320_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("Submitted")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val SignAndSubmitJoint =

    /*======================================================================================
    * View Case
    ======================================================================================*/

    group("XUI_NFD_325_ViewCase") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_325_005_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("Applicant2Approved")))

      .exec(Common.userDetails)
    }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select 'Sign and submit' from the dropdown
    ======================================================================================*/

    .group("XUI_NFD_330_JointStartEventSignAndSubmit") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_330_005_StartEventSignAndSubmit")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitor-submit-application?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitor-submit-application"))
        .check(jsonPath("$.case_fields[?(@.id=='applicant2SolicitorAnswersLink')].value.document_filename").saveAs("app2AnswersFilename"))
        .check(jsonPath("$.case_fields[?(@.id=='applicant2SolicitorAnswersLink')].value.document_url").saveAs("app2AnswersUrl"))
        .check(jsonPath("$.case_fields[?(@.id=='solApplicationFeeInPounds')].value").saveAs("feeInPounds"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeAmount").saveAs("feeAmount"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeCode").saveAs("feeCode"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeDescription").saveAs("feeDescription"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.Fees[0].value.FeeVersion").saveAs("feeVersion"))
        .check(jsonPath("$.case_fields[?(@.id=='applicationFeeOrderSummary')].value.PaymentTotal").saveAs("paymentTotal")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationConfirmJointApplication"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Review Application
    ======================================================================================*/

    .group("XUI_NFD_340_JointReviewApplication") {
      exec(http("XUI_NFD_340_005_JointReviewApplication")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationConfirmJointApplication")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointReviewApplication.json"))
        .check(substring("applicant2SolicitorAnswersLink")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolStatementOfTruth"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint App1 Statement Of Truth
    ======================================================================================*/

    .group("XUI_NFD_350_JointApp1StatementOfTruth") {
      exec(http("XUI_NFD_350_005_JointApp1StatementOfTruth")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolStatementOfTruth")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointApp1SOT.json"))
        .check(substring("applicant1StatementOfTruth")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolPayment"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
     * Joint Payment Method
     ======================================================================================*/

    .group("XUI_NFD_360_JointPaymentMethod") {
      exec(http("XUI_NFD_360_005_JointPaymentMethod")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolPayment")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointPaymentMethod.json"))
        .check(substring("pbaNumbers")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolPayAccount"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Select PBA Account
    ======================================================================================*/

    .group("XUI_NFD_370_JointPaymentDetails") {
      exec(http("XUI_NFD_370_005_JointPaymentDetails")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolPayAccount")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointPaymentDetails.json"))
        .check(substring("feeAccountReference")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsolicitor-submit-applicationSolPaymentSummary"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Joint Order Summary
    ======================================================================================*/

    .group("XUI_NFD_380_JointOrderSummary") {
      exec(http("XUI_NFD_380_005_JointOrderSummary")
        .post("/data/case-types/NFD/validate?pageId=solicitor-submit-applicationSolPaymentSummary")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointPaymentSummary.json"))
        .check(substring("applicationFeeOrderSummary")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsolicitor-submit-application%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Sign & Submit: Check Your Answers
    ======================================================================================*/

    .group("XUI_NFD_390_JointSignCheckYourAnswers") {
      exec(http("XUI_NFD_390_005_JointSignCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDJointSignCheckYourAnswers.json"))
        .check(jsonPath("$.state").is("Submitted")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_390_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("Submitted")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val RespondToNFDCase =

    /*======================================================================================
    * View Case
    ======================================================================================*/

    group("XUI_NFD_400_ViewCase") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_400_005_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AwaitingAos")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Draft AoS from the dropdown
    ======================================================================================*/

    .group("XUI_NFD_410_StartDraftAOS") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_410_005_StartDraftAOS")
        .get("/data/internal/cases/${caseId}/event-triggers/draft-aos?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.case_fields[?(@.id=='miniApplicationLink')].value.document_filename").saveAs("filename"))
        .check(jsonPath("$.case_fields[?(@.id=='miniApplicationLink')].value.document_url").saveAs("documentURL"))
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("draft-aos")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos%2Fdraft-aosApplicant2SolConfirmContactDetails"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confirm Solicitor Details
    ======================================================================================*/

    .group("XUI_NFD_420_ConfirmSolicitorDetails") {
      exec(http("XUI_NFD_420_005_ConfirmSolicitorDetails")
        .post("/data/case-types/NFD/validate?pageId=draft-aosApplicant2SolConfirmContactDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespSolicitorDetails.json"))
        .check(substring("applicant2SolicitorName")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos%2Fdraft-aosApplicant2SolReviewApplicant1Application"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Review Application
    ======================================================================================*/

    .group("XUI_NFD_430_ReviewApplication") {
      exec(http("XUI_NFD_430_005_ReviewApplication")
        .post("/data/case-types/NFD/validate?pageId=draft-aosApplicant2SolReviewApplicant1Application")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespReviewApplication.json"))
        .check(substring("confirmReadPetition")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos%2Fdraft-aosapplicant2HowToResponseToApplication"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue Without Disputing
    ======================================================================================*/

    .group("XUI_NFD_440_ContinueWithoutDisputing") {
      exec(http("XUI_NFD_440_005_ContinueWithoutDisputing")
        .post("/data/case-types/NFD/validate?pageId=draft-aosapplicant2HowToResponseToApplication")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespContinueWithoutDisputing.json"))
        .check(substring("howToRespondApplication")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos%2Fdraft-aosApplicant2SolAosJurisdiction"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdictions
    ======================================================================================*/

    .group("XUI_NFD_450_Jurisdictions") {
      exec(http("XUI_NFD_450_005_Jurisdictions")
        .post("/data/case-types/NFD/validate?pageId=draft-aosApplicant2SolAosJurisdiction")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespJurisdictions.json"))
        .check(substring("jurisdictionAgree")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos%2Fdraft-aosApplicant2SolAosOtherProceedings"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Legal Proceedings
    ======================================================================================*/

    .group("XUI_NFD_460_LegalProceedings") {
      exec(http("XUI_NFD_460_005_LegalProceedings")
        .post("/data/case-types/NFD/validate?pageId=draft-aosApplicant2SolAosOtherProceedings")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespLegalProceedings.json"))
        .check(substring("applicant2LegalProceedings")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-aos%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Save AOS
    ======================================================================================*/

    .group("XUI_NFD_470_SaveAOS") {
      exec(http("XUI_NFD_470_005_SaveAOS")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespSaveAOS.json"))
        .check(jsonPath("$.state").is("AosDrafted")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_470_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AosDrafted")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Submit AoS from the dropdown menu
    ======================================================================================*/

    .group("XUI_NFD_480_StartSubmitAOS") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-aos"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_480_005_StartSubmitAOS")
        .get("/data/internal/cases/${caseId}/event-triggers/submit-aos?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("submit-aos")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-aos%2Fsubmit-aosApplicant2SolStatementOfTruth"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue to Statement Of Truth
    ======================================================================================*/

    .group("XUI_NFD_490_ContinueToStatementOfTruth") {

      exec(http("XUI_NFD_490_005_ContinueToStatementOfTruth")
        .post("/data/case-types/NFD/validate?pageId=submit-aosApplicant2SolStatementOfTruth")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespContinueToSOT.json"))
        .check(substring("labelContentMarriageOrCivilPartnership")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-aos%2Fsubmit-aosSubmitAos"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Statement Of Truth
    ======================================================================================*/

    .group("XUI_NFD_500_StatementOfTruth") {

      exec(http("XUI_NFD_500_005_StatementOfTruth")
        .post("/data/case-types/NFD/validate?pageId=submit-aosSubmitAos")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespSOT.json"))
        .check(substring("statementOfTruth")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-aos%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit AOS
    ======================================================================================*/

    .group("XUI_NFD_510_SubmitAOS") {

      exec(http("XUI_NFD_510_005_SubmitAOS")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDRespSubmitAOS.json"))
        .check(jsonPath("$.state").is("Holding")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_510_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("Holding")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ApplyForCO =

    /*======================================================================================
    * View Case
    ======================================================================================*/

    group("XUI_NFD_520_ViewCase") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_520_005_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AwaitingConditionalOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Draft Conditional Order from the dropdown
    ======================================================================================*/

    .group("XUI_NFD_530_StartDraftCO") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-conditional-order"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_530_005_StartDraftCO")
        .get("/data/internal/cases/${caseId}/event-triggers/draft-conditional-order?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.case_fields[?(@.id=='coRespondentAnswersLink')].value.document_filename").saveAs("respondentAnswers"))
        .check(jsonPath("$.case_fields[?(@.id=='coRespondentAnswersLink')].value.document_url").saveAs("respondentAnswersURL"))
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("draft-conditional-order")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-conditional-order%2Fdraft-conditional-orderConditionalOrderReviewAoS"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue with Conditional Order
    ======================================================================================*/

    .group("XUI_NFD_540_ContinueWithCO") {
      exec(http("XUI_NFD_540_005_ContinueWithCO")
        .post("/data/case-types/NFD/validate?pageId=draft-conditional-orderConditionalOrderReviewAoS")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDCOContinueWithCO.json"))
        .check(substring("coApplicant1ApplyForConditionalOrder")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-conditional-order%2Fdraft-conditional-orderConditionalOrderReviewApplicant1"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confirm Application Info
    ======================================================================================*/

      .group("XUI_NFD_550_ConfirmApplicationInfo") {
        exec(http("XUI_NFD_550_005_ConfirmApplicationInfo")
          .post("/data/case-types/NFD/validate?pageId=draft-conditional-orderConditionalOrderReviewApplicant1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/nfd/NFDCOConfirmAppInfo.json"))
          .check(substring("coApplicant1ConfirmInformationStillCorrect")))

        .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fdraft-conditional-order%2Fsubmit"))

        .exec(Common.userDetails)
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers - Save CO
    ======================================================================================*/

    .group("XUI_NFD_560_SaveCO") {
      exec(http("XUI_NFD_560_005_SaveCO")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDCOSaveCO.json"))
        .check(jsonPath("$.state").is("ConditionalOrderDrafted")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_560_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("ConditionalOrderDrafted")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Submit Conditional Order from the dropdown
    ======================================================================================*/

    .group("XUI_NFD_570_StartSubmitCO") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-conditional-order"))

      .exec(Common.profile)

      .exec(http("XUI_NFD_570_005_StartSubmitCO")
        .get("/data/internal/cases/${caseId}/event-triggers/submit-conditional-order?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("submit-conditional-order")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-conditional-order%2Fsubmit-conditional-orderConditionalOrderSoT"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue to Submit Conditional Order
    ======================================================================================*/

    .group("XUI_NFD_580_ContinueToSubmitCO") {
      exec(http("XUI_NFD_580_005_ContinueToSubmitCO")
        .post("/data/case-types/NFD/validate?pageId=submit-conditional-orderConditionalOrderSoT")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDCOContinueToSubmitCO.json"))
        .check(substring("coApplicant1StatementOfTruth")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2Fsubmit-conditional-order%2Fsubmit"))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit CO
    ======================================================================================*/

    .group("XUI_NFD_590_SubmitCO") {
      exec(http("XUI_NFD_590_005_SubmitCO")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/nfd/NFDCOSubmitCO.json"))
        .check(jsonPath("$.state").is("AwaitingLegalAdvisorReferral")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_NFD_590_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AwaitingLegalAdvisorReferral")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

}
