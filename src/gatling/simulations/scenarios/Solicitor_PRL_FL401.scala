package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

/*======================================================================================
* Create a new Private Law FL401 application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_FL401 {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CreatePrivateLawCase =

    //set session variables
    exec(_.setAll(
      "ApplicantFirstName" -> ("App" + Common.randomString(5)),
      "ApplicantLastName" -> ("Test" + Common.randomString(5)),
      "RespondentFirstName" -> ("Resp" + Common.randomString(5)),
      "RespondentLastName" -> ("Test" + Common.randomString(5)),
      "AppDobDay" -> Common.getDay(),
      "AppDobMonth" -> Common.getMonth(),
      "AppDobYear" -> Common.getDobYear(),
      "RespDobDay" -> Common.getDay(),
      "RespDobMonth" -> Common.getMonth(),
      "RespDobYear" -> Common.getDobYear()))

    /*======================================================================================
    * Click the Create Case link
    ======================================================================================*/

    .group("XUI_PRL_FL401_030_CreateCase") {
      exec(http("XUI_PRL_FL401_030_CreateCase")
        .get("/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("PRIVATELAW")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction = Family Private Law; Case Type = C100 & FL401 Applications; Event = Solicitor Application
    ======================================================================================*/

    .group("XUI_PRL_FL401_040_SelectCaseType") {
      exec(http("XUI_PRL_FL401_040_005_SelectCaseType")
        .get("/data/internal/case-types/PRLAPPS/event-triggers/solicitorCreate?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitorCreate")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Type of Application (C100 or FL401) - FL401
    ======================================================================================*/

    .group("XUI_PRL_FL401_050_SelectApplicationType") {
      exec(http("XUI_PRL_FL401_050_005_SelectApplicationType")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SelectApplicationType.json"))
        .check(substring("caseTypeOfApplication")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confidentiality Statement
    ======================================================================================*/

    .group("XUI_PRL_FL401_060_ConfidentialityStatement") {
      exec(http("XUI_PRL_FL401_060_005_ConfidentialityStatement")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ConfidentialityStatement.json"))
        .check(substring("confidentialityStatementDisclaimer")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Case Name
    ======================================================================================*/

    .group("XUI_PRL_FL401_070_CaseName") {
      exec(http("XUI_PRL_FL401_070_005_CaseName")
        .post("/data/case-types/PRLAPPS/validate?pageId=solicitorCreate5")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401CaseName.json"))
        .check(substring("applicantOrRespondentCaseName")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_080_CheckYourAnswers") {
      exec(http("XUI_PRL_FL401_080_005_CheckYourAnswers")
        .post("/data/case-types/PRLAPPS/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401CheckYourAnswers.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_080_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"solicitorCreate","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_080_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val TypeOfApplication =

    /*======================================================================================
    * Click the "Type of Application" link
    ======================================================================================*/

    group("XUI_PRL_FL401_090_CreateTypeOfApplicationEvent") {
      exec(http("XUI_PRL_FL401_090_005_TypeOfApplicationTrigger")
        .get("/cases/case-details/${caseId}/trigger/fl401TypeOfApplication/fl401TypeOfApplication1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_090_010_CreateTypeOfApplicationEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/fl401TypeOfApplication?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("fl401TypeOfApplication")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Type Of Application Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_095_TypeOfApplicationProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What order(s) are you applying for? - Tick both Non-molestation order and Occupation order
    ======================================================================================*/

    .group("XUI_PRL_FL401_100_SelectOrders") {
      exec(http("XUI_PRL_FL401_100_005_SelectOrders")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401TypeOfApplication1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SelectOrders.json"))
        .check(substring("typeOfApplicationOrders")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Is this linked to Child Arrangements application? Select Yes
    ======================================================================================*/

    .group("XUI_PRL_FL401_110_LinkedToCase") {
      exec(http("XUI_PRL_FL401_110_005_LinkedToCase")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401TypeOfApplication2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401LinkedToCase.json"))
        .check(substring("typeOfApplicationLinkToCA")))

        .exec(Common.userDetails)
    }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_120_TypeOfApplicationCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_120_005_TypeOfApplicationCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401TypeOfApplicationCheckYourAnswers.json"))
        .check(substring("typeOfApplicationLinkToCA"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_120_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"fl401TypeOfApplication","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_120_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401TypeOfApplication')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


  val WithoutNoticeOrder =

    /*======================================================================================
    * Click the Without Notice Order link
    ======================================================================================*/

    group("XUI_PRL_FL401_130_CreateWithoutNoticeOrderEvent") {
      exec(http("XUI_PRL_FL401_130_005_WithoutNoticeOrderTrigger")
        .get("/cases/case-details/${caseId}/trigger/withoutNoticeOrderDetails/withoutNoticeOrderDetails1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_130_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401TypeOfApplication')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_130_015_CreateWithoutNoticeOrderEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/withoutNoticeOrderDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("withoutNoticeOrderDetails")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Without Notice Order Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_135_ApplyWithoutNoticeProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you want to apply for the order without giving notice to the respondent? Select Yes
    ======================================================================================*/

    .group("XUI_PRL_FL401_140_ApplyWithoutNotice") {
      exec(http("XUI_PRL_FL401_140_005_ApplyWithoutNotice")
        .post("/data/case-types/PRLAPPS/validate?pageId=withoutNoticeOrderDetails1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ApplyWithoutNotice.json"))
        .check(substring("orderWithoutGivingNoticeToRespondent")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Reason for applying without notice - Select all options
    ======================================================================================*/

    .group("XUI_PRL_FL401_150_ReasonForApplyWithoutNotice") {
      exec(http("XUI_PRL_FL401_150_005_ReasonForApplyWithoutNotice")
        .post("/data/case-types/PRLAPPS/validate?pageId=withoutNoticeOrderDetails2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ReasonForApplyWithoutNotice.json"))
        .check(substring("reasonForOrderWithoutGivingNotice")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Bail Conditions - No
    ======================================================================================*/

    .group("XUI_PRL_FL401_160_BailConditions") {
      exec(http("XUI_PRL_FL401_160_005_BailConditions")
        .post("/data/case-types/PRLAPPS/validate?pageId=withoutNoticeOrderDetails3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401BailConditions.json"))
        .check(substring("bailDetails")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Other Details
    ======================================================================================*/

    .group("XUI_PRL_FL401_170_OtherDetails") {
      exec(http("XUI_PRL_FL401_170_005_OtherDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=withoutNoticeOrderDetails4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401OtherDetails.json"))
        .check(substring("anyOtherDtailsForWithoutNoticeOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_180_WithoutNoticeCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_180_005_WithoutNoticeCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401WithoutNoticeCheckYourAnswers.json"))
        .check(substring("anyOtherDtailsForWithoutNoticeOrder"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_180_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"withoutNoticeOrderDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_180_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='withoutNoticeOrderDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ApplicantDetails =

    /*======================================================================================
    * Click the Applicant Details link
    ======================================================================================*/

    group("XUI_PRL_FL401_190_CreateApplicantDetailsEvent") {
      exec(http("XUI_PRL_FL401_190_005_ApplicantDetailsTrigger")
        .get("/cases/case-details/${caseId}/trigger/applicantsDetails/applicantsDetails1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_190_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='withoutNoticeOrderDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_190_015_CreateApplicantDetailsEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/applicantsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("applicantsDetails")))

      .exec(Common.userDetails)

      .exec(http("XUI_PRL_FL401_190_020_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Details Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_195_ApplicantDetailsProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Applicant Details
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .group("XUI_PRL_FL401_200_ApplicantDetails") {
      exec(http("XUI_PRL_FL401_200_005_ApplicantDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=applicantsDetails2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ApplicantDetails.json"))
        .check(substring("doTheyHaveLegalRepresentation")))

      .exec(Common.caseShareOrgs)

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_210_ApplicantDetailsCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_210_005_ApplicantDetailsCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ApplicantDetailsCheckYourAnswers.json"))
        .check(substring("doTheyHaveLegalRepresentation"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_210_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"applicantsDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_210_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='applicantsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val RespondentDetails =

    /*======================================================================================
    * Click the Respondent Details link
    ======================================================================================*/

    group("XUI_PRL_FL401_220_CreateRespondentDetailsEvent") {
      exec(http("XUI_PRL_FL401_220_005_RespondentDetailsTrigger")
        .get("/cases/case-details/${caseId}/trigger/respondentsDetails/respondentsDetails1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_220_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='applicantsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_220_015_CreateRespondentDetailsEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/respondentsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("respondentsDetails")))

      .exec(Common.userDetails)
      
      .exec(http("XUI_PRL_FL401_220_020_GetOrgs")
        .get("/api/caseshare/orgs")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*"))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_225_RespondentDetailsProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .group("XUI_PRL_FL401_230_RespondentDetails") {
      exec(http("XUI_PRL_FL401_230_005_RespondentDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=respondentsDetails2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401RespondentDetails.json"))
        .check(substring("respondentsFL401")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_240_RespondentDetailsCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_240_005_RespondentDetailsCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401RespondentDetailsCheckYourAnswers.json"))
        .check(substring("respondentsFL401"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_240_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"respondentsDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_240_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ApplicantsFamily =

    /*======================================================================================
    * Click the Applicant's family link
    ======================================================================================*/

    group("XUI_PRL_FL401_250_CreateApplicantsFamilyEvent") {
      exec(http("XUI_PRL_FL401_250_005_CreateApplicantsFamilyTrigger")
        .get("/cases/case-details/${caseId}/trigger/fl401ApplicantFamilyDetails/fl401ApplicantFamilyDetails1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_FL401_250_010_WorkAllocation")
        .get("/workallocation2/case/tasks/${caseId}/event/fl401ApplicantFamilyDetails/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_250_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_250_020_CreateRespondentDetailsEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/fl401ApplicantFamilyDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("fl401ApplicantFamilyDetails")))

      .exec(Common.userDetails)
      .exec(Common.userDetails)
      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicants Family Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_255_ApplicantsFamilyDetailsProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Does the applicant have children - No
    ======================================================================================*/

    .group("XUI_PRL_FL401_260_ApplicantsFamilyDetails") {
      exec(http("XUI_PRL_FL401_260_005_ApplicantsFamilyDetails")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401ApplicantFamilyDetails1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ApplicantsFamilyDetails.json"))
        .check(substring("applicantFamilyDetails")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_270_ApplicantsFamilyDetailsCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_270_005_ApplicantsFamilyDetailsCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ApplicantsFamilyDetailsCheckYourAnswers.json"))
        .check(substring("applicantFamilyDetails"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_270_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"fl401ApplicantFamilyDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_270_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401ApplicantFamilyDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val Relationship =

    /*======================================================================================
    * Click the Relationship to Respondent link
    ======================================================================================*/

    group("XUI_PRL_FL401_280_CreateRelationshipEvent") {
      exec(http("XUI_PRL_FL401_280_005_CreateRelationshipTrigger")
        .get("/cases/case-details/${caseId}/trigger/respondentRelationship/respondentRelationship1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_280_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401ApplicantFamilyDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_280_015_CreateRelationshipEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/respondentRelationship?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("respondentRelationship")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select relationship Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_285_RelationshipProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select relationship - Married or Civil
    ======================================================================================*/

    .group("XUI_PRL_FL401_290_Relationship") {
      exec(http("XUI_PRL_FL401_290_005_Relationship")
        .post("/data/case-types/PRLAPPS/validate?pageId=respondentRelationship1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401Relationship.json"))
        .check(substring("respondentRelationObject")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Relationship Dates
    ======================================================================================*/

    .group("XUI_PRL_FL401_300_RelationshipDates") {
      exec(http("XUI_PRL_FL401_300_005_RelationshipDates")
        .post("/data/case-types/PRLAPPS/validate?pageId=respondentRelationship2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401RelationshipDates.json"))
        .check(substring("respondentRelationDateInfoObject")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_310_RelationshipCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_310_005_RelationshipCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401RelationshipCheckYourAnswers.json"))
        .check(substring("respondentRelationObject"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_310_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"respondentRelationship","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_310_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentRelationship')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val Behaviour =

    /*======================================================================================
    * Click the Respondent's behaviour link
    ======================================================================================*/

    group("XUI_PRL_FL401_320_CreateBehaviourEvent") {
      exec(http("XUI_PRL_FL401_320_005_CreateBehaviourTrigger")
        .get("/cases/case-details/${caseId}/trigger/respondentBehaviour/respondentBehaviour1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_320_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentRelationship')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_320_015_CreateRelationshipEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/respondentBehaviour?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("respondentBehaviour")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Behaviour Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_325_BehaviourProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Behaviour - Being violent
    ======================================================================================*/

    .group("XUI_PRL_FL401_330_Behaviour") {
      exec(http("XUI_PRL_FL401_330_005_Behaviour")
        .post("/data/case-types/PRLAPPS/validate?pageId=respondentBehaviour1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401Behaviour.json"))
        .check(substring("respondentBehaviourData")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_340_BehaviourCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_340_005_BehaviourCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401BehaviourCheckYourAnswers.json"))
        .check(substring("respondentBehaviourData"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_340_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"respondentBehaviour","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_340_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentBehaviour')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val TheHome =

    /*======================================================================================
    * Click the The home link
    ======================================================================================*/

    group("XUI_PRL_FL401_350_CreateTheHomeEvent") {
      exec(http("XUI_PRL_FL401_350_005_CreateTheHomeTrigger")
        .get("/cases/case-details/${caseId}/trigger/fl401Home/fl401Home1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_350_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentBehaviour')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_350_015_CreateTheHomeEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/fl401Home?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("fl401Home")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Complete The Home Details Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_355_TheHomeProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Complete The Home Details
    ======================================================================================*/

    .exec(Common.postcodeLookup)

    .group("XUI_PRL_FL401_360_TheHome") {
      exec(http("XUI_PRL_FL401_360_005_TheHome")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401Home1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401TheHome.json"))
        .check(substring("doesApplicantHaveHomeRights")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_370_TheHomeCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_370_005_TheHomeCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401TheHomeCheckYourAnswers.json"))
        .check(substring("doesApplicantHaveHomeRights"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_370_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"fl401Home","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_370_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401Home')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val UploadDocuments =

    /*======================================================================================
    * Click the Upload Documents link
    ======================================================================================*/

    group("XUI_PRL_FL401_380_CreateUploadDocumentsEvent") {
      exec(http("XUI_PRL_FL401_380_005_CreateUploadDocumentsTrigger")
        .get("/cases/case-details/${caseId}/trigger/fl401UploadDocuments/fl401UploadDocuments1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_380_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401Home')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_380_015_CreateUploadDocumentsEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/fl401UploadDocuments?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("fl401UploadDocuments")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Documents Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_385_UploadDocumentProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Documents
    ======================================================================================*/

    .group("XUI_PRL_FL401_390_UploadDocument") {
      exec(http("XUI_PRL_FL401_390_005_UploadDocument")
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
        .formParam("caseTypeId", "PRLAPPS")
        .formParam("jurisdictionId", "PRIVATELAW")
        .check(substring("originalDocumentName"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("documentHash"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit Documents
    ======================================================================================*/

    .group("XUI_PRL_FL401_400_SubmitDocuments") {
      exec(http("XUI_PRL_FL401_400_005_SubmitDocuments")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401UploadDocuments1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SubmitDocuments.json"))
        .check(substring("fl401UploadWitnessDocuments")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_FL401_410_UploadDocumentsCheckYourAnswers") {
      exec(http("XUI_PRL_FL401_410_005_UploadDocumentsCheckYourAnswers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401UploadDocumentsCheckYourAnswers.json"))
        .check(substring("fl401UploadWitnessDocuments"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_410_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"fl401UploadDocuments","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_410_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401UploadDocuments')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val ViewPDF =

    /*======================================================================================
    * Click the View PDF application link
    ======================================================================================*/

    group("XUI_PRL_FL401_420_CreateViewPDFEvent") {
      exec(http("XUI_PRL_FL401_420_005_CreateViewPDFTrigger")
        .get("/cases/case-details/${caseId}/trigger/viewPdfDocument/viewPdfDocument1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_420_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401UploadDocuments')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_420_015_CreateViewPDFEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/viewPdfDocument?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[?(@.id=='draftOrderDoc')].value.document_url").saveAs("DocumentURL"))
        .check(jsonPath("$.case_fields[?(@.id=='draftOrderDoc')].value.document_filename").saveAs("DocumentFilename"))
        .check(jsonPath("$.case_fields[?(@.id=='draftOrderDoc')].value.document_hash").saveAs("DocumentHash"))
        .check(jsonPath("$.id").is("viewPdfDocument")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_425_ViewPDFContinueProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue
    ======================================================================================*/

    .group("XUI_PRL_FL401_430_ViewPDFContinue") {
      exec(http("XUI_PRL_FL401_430_005_ViewPDFContinue")
        .post("/data/case-types/PRLAPPS/validate?pageId=viewPdfDocument1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ViewPDF.json"))
        .check(substring("draftOrderDoc")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Save and Continue
    ======================================================================================*/

    .group("XUI_PRL_FL401_440_ViewPDFSaveAndContinue") {
      exec(http("XUI_PRL_FL401_440_005_ViewPDFSaveAndContinue")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401ViewPDFSaveAndContinue.json"))
        .check(substring("draftOrderDoc"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_440_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"viewPdfDocument","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_440_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


  val StatementOfTruth =

    /*======================================================================================
    * Click the Statement of truth and submit link
    ======================================================================================*/

    group("XUI_PRL_FL401_450_CreateSOTEvent") {
      exec(http("XUI_PRL_FL401_450_005_CreateSOTTrigger")
        .get("/cases/case-details/${caseId}/trigger/fl401StatementOfTruthAndSubmit/fl401StatementOfTruthAndSubmit1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_FL401_450_010_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_450_015_CreateSOTEvent")
        .get("/data/internal/cases/${caseId}/event-triggers/fl401StatementOfTruthAndSubmit?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("fl401StatementOfTruthAndSubmit")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Statement of Truth (SOT) Profile
    ======================================================================================*/

    .group("XUI_PRL_FL401_455_SOTProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Statement of Truth (SOT)
    ======================================================================================*/

    .group("XUI_PRL_FL401_460_SOT") {
      exec(http("XUI_PRL_FL401_460_005_SOT")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401StatementOfTruthAndSubmit1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SOT.json"))
        .check(substring("fl401StmtOfTruth")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Continue
    ======================================================================================*/

    .group("XUI_PRL_FL401_470_SOTContinue") {
      exec(http("XUI_PRL_FL401_470_005_SOTContinue")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401StatementOfTruthAndSubmit2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SOTContinue.json"))
        .check(substring("fl401ConfidentialityCheck")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Select the family court
    ======================================================================================*/

    .group("XUI_PRL_FL401_480_SelectFamilyCourt") {
      exec(http("XUI_PRL_FL401_480_005_SelectFamilyCourt")
        .post("/data/case-types/PRLAPPS/validate?pageId=fl401StatementOfTruthAndSubmit3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SelectFamilyCourt.json"))
        .check(substring("submitCountyCourtSelection")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit
    ======================================================================================*/

    .group("XUI_PRL_FL401_490_SOTSubmit") {
      exec(http("XUI_PRL_FL401_490_005_SOTSubmit")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401SOTSubmit.json"))
        .check(substring("fl401StmtOfTruth"))
        .check(jsonPath("$.state").is("SUBMITTED_PAID")))

      .exec(http("XUI_PRL_FL401_490_010_WorkAllocation")
        .post("/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"fl401StatementOfTruthAndSubmit","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_FL401_490_015_ViewCase")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401StatementOfTruthAndSubmit')]"))
        .check(jsonPath("$.state.id").is("SUBMITTED_PAID")))

      .exec(Common.userDetails)
    }

  .pause(MinThinkTime, MaxThinkTime)

}
