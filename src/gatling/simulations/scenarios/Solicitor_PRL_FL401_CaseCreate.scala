package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.{BufferedWriter, FileWriter}
import utils._

object Solicitor_PRL_FL401_CaseCreate {

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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401CheckYourAnswers.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_080_010_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .get("/cases/case-details/#{caseId}/trigger/fl401TypeOfApplication/fl401TypeOfApplication1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_FL401_090_010_CreateTypeOfApplicationEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/fl401TypeOfApplication?ignore-warning=false")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401TypeOfApplicationCheckYourAnswers.json"))
        .check(substring("typeOfApplicationLinkToCA"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_120_010_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .get("/cases/case-details/#{caseId}/trigger/withoutNoticeOrderDetails/withoutNoticeOrderDetails1")
        .headers(Headers.navigationHeader)
        .check(substring("Manage cases")))

      .exec(Common.configurationui)
      .exec(Common.configJson)
      .exec(Common.TsAndCs)
      .exec(Common.configUI)
      .exec(Common.userDetails)
      .exec(Common.isAuthenticated)
      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_FL401_130_010_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='fl401TypeOfApplication')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_130_015_CreateWithoutNoticeOrderEvent")
        .get("/data/internal/cases/#{caseId}/event-triggers/withoutNoticeOrderDetails?ignore-warning=false")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .header("x-xsrf-token", "#{XSRFToken}")
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
        .post("/data/cases/#{caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/fl401/PRLFL401WithoutNoticeCheckYourAnswers.json"))
        .check(substring("anyOtherDtailsForWithoutNoticeOrder"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_FL401_180_010_ViewCase")
        .get("/data/internal/cases/#{caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='withoutNoticeOrderDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)





}