package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

/*======================================================================================
* Create a new Divorce application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_Divorce {

  val IdamUrl = Environment.idamURL
  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CreateDivorceCase =

    //set session variables
    exec(_.setAll(
      "petitionerFirstName" -> ("Pet" + Common.randomString(5)),
      "petitionerLastName" -> ("Test" + Common.randomString(5)),
      "respondentFirstName" -> ("Resp" + Common.randomString(5)),
      "respondentLastName" -> ("Test" + Common.randomString(5)),
      "marriageDay" -> Common.getDay(),
      "marriageMonth" -> Common.getMonth(),
      "marriageYear" -> Common.getMarriageYear(),
      "separationDay" -> Common.getDay(),
      "separationMonth" -> Common.getMonth(),
      "separationYear" -> Common.getSeparationYear(),
      "referenceDate" -> Common.getReferenceDate()))

    /*======================================================================================
    * Click the Create Case link
    ======================================================================================*/

    .group("XUI_Divorce_030_CreateCase") {
      exec(Common.healthcheck("%2Fcases%2Fcase-filter"))

      .exec(http("XUI_Divorce_030_CreateCase")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("DIVORCE")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction = Family Divorce; Case Type = Divorce case - v115.00; Event = Apply for a divorce
    ======================================================================================*/

    .group("XUI_Divorce_040_SelectCaseType") {
      exec(Common.healthcheck("%2Fcases%2Fcase-create%2FDIVORCE%2FDIVORCE%2FsolicitorCreate"))

      .exec(http("XUI_Divorce_040_005_StartApplication")
        .get("/data/internal/case-types/DIVORCE/event-triggers/solicitorCreate?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitorCreate")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-create%2FDIVORCE%2FDIVORCE%2FsolicitorCreate%2FsolicitorCreateSolAboutTheSolicitor"))

      //select respondent solicitor org now, as this call will be retrieved from cache in future
      //requests, where checks aren't performed by Gatling https://gatling.io/docs/gatling/reference/current/http/protocol/#caching
      .exec(http("XUI_Divorce_040_010_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .check(regex(""""name":"(.+?)","organisationIdentifier":"([0-9A-Z]+?)"""").ofType[(String, String)].findRandom.saveAs("solicitorRespondentOrgs")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Complete Solicitor Details and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_050_AddSolicitorDetails") {
      exec(http("XUI_Divorce_050_005_AddSolicitorDetails")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolAboutTheSolicitor")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceAddSolicitorDetails.json"))
        .check(substring("PetitionerSolicitorName")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Complete Petitioner Details and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_060_AddPetitionerDetails") {
      exec(http("XUI_Divorce_060_005_AddPetitionerDetails")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolAboutThePetitioner")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceAddPetitionerDetails.json"))
        .check(substring("D8PetitionerFirstName")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Complete Respondent Details and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_070_AddRespondentDetails") {
      exec(http("XUI_Divorce_070_005_AddRespondentDetails")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolAboutTheRespondent")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceAddRespondentDetails.json"))
        .check(substring("D8RespondentFirstName")))

      .exec(http("XUI_Divorce_070_010_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Add respondent's solicitor details and click Continue
    Represented: Yes
    Digital case: Yes
    ======================================================================================*/

    .group("XUI_Divorce_080_AddRespondentSolicitorDetails") {
      exec(http("XUI_Divorce_080_005_AddRespondentSolicitorDetails")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateRespondentServiceDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceAddRespondentSolicitorDetails.json"))
        .check(substring("D8RespondentSolicitorName")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Add marriage certificate details and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_090_AddMarraigeCertDetails") {
      exec(http("XUI_Divorce_090_005_AddMarraigeCertDetails")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolMarriageCertificate")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceAddMarriageCertificateDetails.json"))
        .check(substring("D8MarriagePetitionerName")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Add jurisdiction details and click Continue
    The Petitioner and Respondent are both domiciled in England and Wales
    ======================================================================================*/

    .group("XUI_Divorce_100_Jurisdiction") {
      exec(http("XUI_Divorce_100_005_Jurisdiction")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolJurisdiction")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceJurisdiction.json"))
        .check(substring("D8JurisdictionConnectionNewPolicy")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Add reason for divorce and click Continue
    5 year separation
    ======================================================================================*/

    .group("XUI_Divorce_110_ReasonForDivorce") {
      exec(http("XUI_Divorce_110_005_ReasonForDivorce")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolReasonForDivorce")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceReasonForDivorce.json"))
        .check(substring("D8ReasonForDivorce")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Add dates for separation and click Continue
    > 5 years
    ======================================================================================*/

    .group("XUI_Divorce_120_DateOfSeparation") {
      exec(http("XUI_Divorce_120_005_DateOfSeparation")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolSOCSeparation")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceDateOfSeparation.json"))
        .check(substring("D8ReasonForDivorceSeperationDate")))

        .exec(Common.userDetails)
        .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Lived Apart - Select Yes and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_130_LivedApart") {
      exec(http("XUI_Divorce_130_005_LivedApart")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolSOCLivedApart")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceLivedApart.json"))
        .check(substring("D8LivedApartSinceSeparation")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Other legal proceedings - Select No and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_140_OtherLegalProceedings") {
      exec(http("XUI_Divorce_140_005_OtherLegalProceedings")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolExistingCourtCases")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceOtherLegalProceedings.json"))
        .check(substring("D8LegalProceedings")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Financial Orders - Select No and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_150_FinancialOrders") {
      exec(http("XUI_Divorce_150_005_FinancialOrders")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolDividingMoneyAndProperty")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceFinancialOrders.json"))
        .check(substring("D8FinancialOrder")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Claim for Costs - Select No and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_160_ClaimForCosts") {
      exec(http("XUI_Divorce_160_005_ClaimForCosts")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolApplyToClaimCosts")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceClaimForCosts.json"))
        .check(substring("D8DivorceCostsClaim")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Marriage Certificate
    ======================================================================================*/

    .group("XUI_Divorce_170_UploadMarriageCertificate") {
      exec(http("XUI_Divorce_170_005_UploadMarriageCertificate")
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

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Click Continue
    ======================================================================================*/

    .group("XUI_Divorce_180_ConfirmDocumentUploads") {
      exec(http("XUI_Divorce_180_005_ConfirmDocumentUploads")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreateSolUploadDocs")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceUploadDocuments.json"))
        .check(substring("D8DocumentsUploaded")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Language - No to Welsh andClick Continue
    ======================================================================================*/

    .group("XUI_Divorce_190_SelectLanguage") {
      exec(http("XUI_Divorce_190_005_SelectLanguage")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorCreatelangPref")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceSelectLanguage.json"))
        .check(substring("LanguagePreferenceWelsh")))

      .exec(http("XUI_Divorce_190_010_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers - Save Petition
    ======================================================================================*/

    .group("XUI_Divorce_200_CheckYourAnswers") {
      exec(http("XUI_Divorce_200_005_CheckYourAnswers")
        .post("/data/case-types/DIVORCE/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceCheckYourAnswers.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(jsonPath("$.state").is("SOTAgreementPayAndSubmitRequired")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_Divorce_200_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("SOTAgreementPayAndSubmitRequired")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Case submission from the dropdown and click Go
    ======================================================================================*/

    .group("XUI_Divorce_210_CaseSubmission") {
      exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit"))

      .exec(Common.profile)

      .exec(http("XUI_Divorce_210_005_CaseSubmission")
        .get("/data/internal/cases/${caseId}/event-triggers/solicitorStatementOfTruthPaySubmit?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit%2FsolicitorStatementOfTruthPaySubmitSolStatementOfTruth"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Statement of Truth - enter details and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_220_StatementOfTruth") {
      exec(http("XUI_Divorce_220_005_StatementOfTruth")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorStatementOfTruthPaySubmitSolStatementOfTruth")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceStatementOfTruth.json"))
        .check(substring("D8StatementOfTruth")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit%2FsolicitorStatementOfTruthPaySubmitSolPayment"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Payment Method - Select Fee Account and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_230_PaymentMethod") {
      exec(http("XUI_Divorce_230_005_PaymentMethod")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorStatementOfTruthPaySubmitSolPayment")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorcePaymentMethod.json"))
        .check(substring("SolPaymentHowToPay")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit%2FsolicitorStatementOfTruthPaySubmitSolPayAccount"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select a PBA account from the dropdown enter a reference and click Continue
    ======================================================================================*/

    .group("XUI_Divorce_240_PBAAccount") {
      exec(http("XUI_Divorce_240_005_PBAAccount")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorStatementOfTruthPaySubmitSolPayAccount")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorcePBAAccount.json"))
        .check(substring("PbaNumbers")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit%2FsolicitorStatementOfTruthPaySubmitSolPaymentSummary"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Summary - Click Continue
    ======================================================================================*/

    .group("XUI_Divorce_250_OrderSummary") {
      exec(http("XUI_Divorce_250_005_OrderSummary")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorStatementOfTruthPaySubmitSolPaymentSummary")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceOrderSummary.json"))
        .check(substring("solApplicationFeeOrderSummary")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit%2FsolicitorStatementOfTruthPaySubmitSolSummary"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Case Submission - Click Continue
    ======================================================================================*/

    .group("XUI_Divorce_260_CaseSubmission") {
      exec(http("XUI_Divorce_260_005_CaseSubmission")
        .post("/data/case-types/DIVORCE/validate?pageId=solicitorStatementOfTruthPaySubmitSolSummary")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceCaseSubmission.json"))
        .check(substring("""data":{}""")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}%2Ftrigger%2FsolicitorStatementOfTruthPaySubmit%2Fsubmit"))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers - Click Submit Petition
    ======================================================================================*/

    .group("XUI_Divorce_270_SubmitPetition") {
      exec(http("XUI_Divorce_270_005_SubmitPetition")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/divorce/DivorceSubmitPetition.json"))
        .check(jsonPath("$.state").is("Submitted")))

      .exec(Common.healthcheck("%2Fcases%2Fcase-details%2F${caseId}"))

      .exec(http("XUI_Divorce_270_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("Submitted")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

}
