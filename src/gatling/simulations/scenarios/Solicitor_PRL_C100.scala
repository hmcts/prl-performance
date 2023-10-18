package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_C100 {
  
  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CreatePrivateLawCase =


    /*======================================================================================
    * Click the Create Case link
    ======================================================================================*/

    group("XUI_PRL_C100_030_CreateCase") {

      exec(_.setAll(
        "C100RandomString" -> ("App" + Common.randomString(5)),
        "C100ApplicantFirstName1" -> ("App" + Common.randomString(5)),
        "C100ApplicantLastName1" -> ("Test" + Common.randomString(5)),
        "C100ApplicantFirstName2" -> ("App" + Common.randomString(5)),
        "C100ApplicantLastName2" -> ("Test" + Common.randomString(5)),
        "C100RespondentFirstName" -> ("Resp" + Common.randomString(5)),
        "C100RespondentLastName" -> ("Test" + Common.randomString(5)),
        "C100RespondentEmail" -> (Common.randomString(5) + "@gmail.com"),
        "C100ApplicantEmail" -> (Common.randomString(5) + "@gmail.com"),
        "C100ChildFirstName" -> ("Child" + Common.randomString(5)),
        "C100ChildLastName" -> ("Test" + Common.randomString(5)),
        "C100RepresentativeFirstName" -> ("Rep" + Common.randomString(5)),
        "C100RepresentativeLastName" -> ("Test" + Common.randomString(5)),
        "C100SoleTraderName" -> ("Sole" + Common.randomString(5)),
        "C100SolicitorName" -> ("Soli" + Common.randomString(5)),
        "C100AppDobDay" -> Common.getDay(),
        "C100AppDobMonth" -> Common.getMonth(),
        "C100AppDobYear" -> Common.getDobYear(),
        "C100AppDobDay2" -> Common.getDay(),
        "C100AppDobMonth2" -> Common.getMonth(),
        "C100AppDobYear2" -> Common.getDobYear(),
        "C100ChildAppDobDay" -> Common.getDay(),
        "C100ChildAppDobMonth" -> Common.getMonth(),
        "C100ChildDobYear" -> Common.getDobYearChild(),
        "C100RespDobDay" -> Common.getDay(),
        "C100RespDobMonth" -> Common.getMonth(),
        "C100RandomPhone" -> ("07455753"+ (Common.randomNumber(3))),
        "C100RespDobYear" -> Common.getDobYear()))

      .exec(http("XUI_PRL_C100_030_CreateCase")
        .get(BaseURL + "/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("PRIVATELAW")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Jurisdiction = Family Private Law; Case Type = C100 & FL401 Applications; Event = Solicitor Application
    ======================================================================================*/

    .group("XUI_PRL_C100_040_SelectCaseType") {
      exec(http("XUI_FPL_040_005_StartApplication")
        .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/solicitorCreate?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("solicitorCreate")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Type of Application (C100 or FL401) - C100
    ======================================================================================*/

    .group("XUI_PRL_C100_050_SelectApplicationType") {
      exec(http("XUI_PRL_C100_050_005_SelectApplicationType")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=solicitorCreate2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSelectApplicationType.json"))
        .check(substring("caseTypeOfApplication")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confidentiality Statement
    ======================================================================================*/

    .group("XUI_PRL_C100_060_ConfidentialityStatement") {
      exec(http("XUI_PRL_C100_060_005_ConfidentialityStatement")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=solicitorCreate6")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLConfidentialityStatement.json"))
        .check(substring("c100ConfidentialityStatementDisclaimer")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Case Name
    ======================================================================================*/

    .group("XUI_PRL_C100_070_CaseName") {
      exec(http("XUI_PRL_C100_070_005_CaseName")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=solicitorCreate4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLCaseName.json"))
        .check(substring("applicantCaseName")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_080_CheckYourAnswers") {
      exec(http("XUI_PRL_C100_080_005_CheckYourAnswers")
        .post(BaseURL + "/data/case-types/PRLAPPS/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLCheckYourAnswers.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_080_010_WorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"solicitorCreate","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_080_015_ViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

  val TypeOfApplication =

    /*======================================================================================
    * Click on 'Type of Application' link
    ======================================================================================*/

    group("XUI_PRL_C100_090_CreateTypeOfApplicationEvent") {

      exec(http("XUI_PRL_C100_090_005_CreateTypeOfApplicationViewCase")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/selectApplicationType/selectApplicationType1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_090_010_CreateTypeOfApplicationEventLink")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_090_015_CreateTypeOfApplicationEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/selectApplicationType?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("selectApplicationType")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Type of Application Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_095_TypeOfApplicationProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * What order(s) are you applying for? - Child Arrangements, Spend Time with Order
    ======================================================================================*/

    .group("XUI_PRL_C100_100_SelectOrders") {
      exec(http("XUI_PRL_C100_100_005_SelectOrders")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=selectApplicationType1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSelectOrders.json"))
        .check(substring("typeOfChildArrangementsOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Draft Consent Order Upload
    ======================================================================================*/

    .group("XUI_PRL_C100_110_ConsentOrderUpload") {
      exec(http("XUI_PRL_C100_110_005_ConsentOrderUpload")
        .post(BaseURL + "/documentsv2")
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
    * Do you have a draft consent order? - Yes
    ======================================================================================*/

    .group("XUI_PRL_C100_120_ConsentOrder") {
      exec(http("XUI_PRL_C100_120_005_ConsentOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=selectApplicationType2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLConsentOrders.json"))
        .check(substring("consentOrder")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Have you applied to the court for permission to make this application? - Yes
    ======================================================================================*/

    .group("XUI_PRL_C100_130_PermissionForApplication") {
      exec(http("XUI_PRL_C100_130_005_PermissionForApplication")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=selectApplicationType3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLPermissionRequired.json"))
        .check(substring("applicationPermissionRequired")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Provide Brief Details of Application
    ======================================================================================*/

    .group("XUI_PRL_C100_140_ProvideBriefDetails") {
      exec(http("XUI_PRL_C100_140_005_ProvideBriefDetails")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=selectApplicationType4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLProvideBriefDetails.json"))
        .check(substring("applicationDetails")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_150_CheckYourAnswers") {
      exec(http("XUI_PRL_C100_150_005_CheckYourAnswers")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLCheckYourAnswersTypeOfApplication.json"))
        .check(substring("applicationPermissionRequired"))
        .check(jsonPath("$.state").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_150_010_WorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"selectApplicationType","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_150_015_ViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='selectApplicationType')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)


  val HearingUrgency =

    /*======================================================================================
    * Click on 'Hearing Urgency'
    ======================================================================================*/

    group("XUI_PRL_C100_160_HearingUrgency") {
      exec(http("XUI_PRL_C100_160_005_HearingUrgencyRedirect")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/hearingUrgency/hearingUrgency1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(http("XUI_PRL_C100_160_010_HearingUrgencyViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.caseActivityGet)


      .exec(http("XUI_PRL_C100_160_015_HearingUrgencyEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/hearingUrgency?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("hearingUrgency")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

      .exec(Common.userDetails)

      .exec(Common.userDetails)

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Urgency Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_165_HearingUrgencyProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Hearing Urgency Questions
    ======================================================================================*/

    .group("XUI_PRL_C100_170_HearingUrgencyQuestions") {
      exec(http("XUI_PRL_C100_170_005_HearingUrgencyQuestions")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=hearingUrgency1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLHearingUrgency.json"))
        .check(substring("areRespondentsAwareOfProceedings")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Hearing Urgency Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_180_HearingUrgencyCheckYourAnswers") {
      exec(http("XUI_PRL_C100_180_005_HearingUrgencyCheckYourAnswers")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLHearingUrgencyAnswers.json")))

      .exec(http("XUI_PRL_C100_180_010_HearingUrgencyWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"hearingUrgency","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_180_015_HearingUrgencyViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='hearingUrgency')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)


  val ApplicantDetails =

    /*======================================================================================
    * Click on 'Applicant Details'
    ======================================================================================*/

    group("XUI_PRL_C100_190_ApplicantDetails") {
      exec(http("XUI_PRL_C100_190_005_ApplicantDetailsRedirect")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/applicantsDetails/applicantsDetails1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_190_005_ApplicantDetailsViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))


      .exec(http("XUI_PRL_C100_190_010_ApplicantDetailsEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/applicantsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("applicantsDetails")))

      .exec(Common.userDetails)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Details Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_200_ApplicantDetailsProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Applicant Add New - 2 applicants to be added
    ======================================================================================*/

    .group("XUI_PRL_C100_210_ApplicantDetails") {

      exec(Common.caseShareOrgs)

      .exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_210_015_ApplicantDetailValidate")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=applicantsDetails1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLApplicantDetails.json"))
        .check(substring("dxNumber")))

      .exec(Common.userDetails)

      .exec(Common.caseShareOrgs)

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Applicant Details Check Your Answers
    ======================================================================================*/

    .group("XUI_PRL_C100_220_ApplicantDetailsCheckYourAnswers") {

      exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_220_005_ApplicantDetailsCheckYourAnswers")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLApplicantDetailsAnswers.json")))


      .exec(http("XUI_PRL_C100_220_010_ApplicantDetailsWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"applicantsDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_220_015_ApplicantDetailsViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='applicantsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

  val ChildDetails =

    /*======================================================================================
    * Click on 'Child Details'
    ======================================================================================*/

    group("XUI_PRL_C100_230_ChildDetailsRedirect") {
      exec(http("XUI_PRL_C100_230_005_ChildDetailsRedirect")
        .get(BaseURL + "/case-details/${caseId}/trigger/childDetails/childDetails1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_230_010_ChildDetailsCaseView")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(http("XUI_PRL_C100_230_015_ChildDetailsEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/childDetailsRevised?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("childDetailsRevised")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_235_ChildDetailsProfile") {
      exec(Common.profile)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Add New Child
    ======================================================================================*/

    .group("XUI_PRL_C100_240_ChildDetailsAddNew") {
      exec(http("XUI_PRL_C100_240_005_ChildDetailsAddNew")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=childDetailsRevised1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildDetails.json")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Additional Details
    ======================================================================================*/

    .group("XUI_PRL_C100_250_ChildDetailsAdditionalDetails") {

      exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_250_005_ChildDetailsAdditionalDetails")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=childDetailsRevised2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildAdditionalDetails.json"))
        .check(substring("childrenKnownToLocalAuthority")))

      .exec(Common.userDetails)
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Details Answer Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_260_ChildDetailsAdditionalDetails") {
      exec(http("XUI_PRL_C100_260_005_ChildDetailsAdditionalDetails")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildDetailsSubmit.json")))

      .exec(http("XUI_PRL_C100_260_010_ChildDetailsAdditionalDetailsWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"childDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_260_010_ChildDetailsAdditionalDetailsViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(substring("newChildDetails")))

      .exec(Common.userDetails)

    }

    .pause(MinThinkTime, MaxThinkTime)

  val RespondentDetails =

    /*======================================================================================
    * Click on 'Respondent Details'
    ======================================================================================*/

    group("XUI_PRL_C100_270_RespondentDetailsRedirect") {
      exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_270_005_RespondentDetailsRedirect")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/respondentsDetails/respondentsDetails1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_270_010_RespondentDetailsCaseView")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(http("XUI_PRL_C100_270_015_RespondentDetailsCaseEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/respondentsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token")))

      .exec(Common.userDetails)

    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_275_RespondentDetailsProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Add Respondent Details
    ======================================================================================*/

    .group("XUI_PRL_C100_280_RespondentDetailsAddNew") {

      exec(Common.caseShareOrgs)

      .exec(Common.postcodeLookup)

      .exec(http("XUI_PRL_C100_280_005_RespondentDetailsAddNew")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=respondentsDetails1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLRespondentDetails.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Details Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_290_RespondentDetailsSubmit") {
      exec(http("XUI_PRL_C100_290_005_RespondentDetailsSubmit")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLChildAdditionalDetailsSubmit.json")))

      .exec(http("XUI_PRL_C100_290_010_RespondentDetailsSubmitWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"respondentsDetails","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_290_015_RespondentDetailsSubmitViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='respondentsDetails')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)
  
    }

    .pause(MinThinkTime, MaxThinkTime)

  val MIAM =

    /*======================================================================================
    * Click on 'Miam'
    ======================================================================================*/

    group("XUI_PRL_C100_300_MIAMRedirect") {
      exec(http("XUI_PRL_C100_300_005_MIAMRedirect")
        .get(BaseURL + "/cases/case-details//trigger/miam/miam1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_300_010_MIAMCaseView")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(http("XUI_PRL_C100_300_015_MIAMCaseEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/respondentsDetails?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_305_MIAMProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Has the applicant attended a Mediation information & Assessment Meeting (MIAM)?
    ======================================================================================*/

    .group("XUI_PRL_C100_310_AttendedMIAM") {

      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_310_005_AttendedMIAM")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=miam1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAttendedMIAM.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    *MIAM certificate Upload
    ======================================================================================*/

    .group("XUI_PRL_C100_320_MIAMUpload") {
      exec(http("XUI_PRL_C100_320_005_MIAMUpload")
        .post(BaseURL + "/documentsv2")
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
    * MIAM Details
    ======================================================================================*/

    .group("XUI_PRL_C100_320_MIAMdetails") {

      exec(http("XUI_PRL_C100_320_005_MIAMdetails")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=miam1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLMIAMDetails.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_330_MIAMSubmit") {
      exec(http("XUI_PRL_C100_330_005_MIAMSubmit")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLMIAMDetailsSubmit.json")))

      .exec(http("XUI_PRL_C100_330_005_MIAMWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"miam","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_330_005_MIAMViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='miam')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

  val AllegationsOfHarm =

    /*======================================================================================
    * Click on 'Allegations Of Harm'
    ======================================================================================*/

    group("XUI_PRL_C100_340_AllegationsOfHarmRedirect") {
      exec(http("XUI_PRL_C100_340_005_AllegationsOfHarmRedirect")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/allegationsOfHarm/allegationsOfHarmRevised1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_340_010_AllegationsOfHarmRedirectCaseView")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(http("XUI_PRL_C100_340_015_AllegationsOfHarmEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/allegationsOfHarmRevised?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_345_AllegationsOfHarmProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are there Allegations of Harm?
    ======================================================================================*/

    .group("XUI_PRL_C100_350_AllegationsOfHarm") {

      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_350_005_AllegationsOfHarm")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarmRevised1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAreThereAllegationsOfHarm.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm details
    ======================================================================================*/

    .group("XUI_PRL_C100_360_AllegationsOfHarmDetails") {

      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_360_005_AllegationsOfHarmDetails")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarmRevised2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAllegationsOfHarmDetails.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Behaviour
    ======================================================================================*/

    .group("XUI_PRL_C100_370_AllegationsOfHarmBehaviour") {

      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_370_005_AllegationsOfHarmBehaviour")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarmRevised3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAllegationsOfHarmBehaviour.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Other Concerns
    ======================================================================================*/

    .group("XUI_PRL_C100_380_AllegationsOfHarmOther") {

      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_380_005_AllegationsOfHarmOther")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=allegationsOfHarm4")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAllegationsOfHarmOther.json")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Allegations of Harm Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_390_AllegationsOfHarmSubmit") {
      exec(http("XUI_PRL_C100_390_005_AllegationsOfHarmSubmit")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLAreThereAllegationsOfHarmSubmit.json")))

      .exec(http("XUI_PRL_C100_390_010_AllegationsOfHarmWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"allegationsOfHarm","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_390_015_AllegationsOfHarmViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(substring("allegationOfHarm")))
        //might need to relook at the final checks for each submit

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

  val ViewPdfApplication =

    /*======================================================================================
    * Click on 'View PDF Application'
    ======================================================================================*/

    group("XUI_PRL_C100_400_ViewPdfApplicationRedirect") {
      exec(http("XUI_PRL_C100_400_005_ViewPdfApplicationRedirect")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/viewPdfDocument/viewPdfDocument1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_400_010_ViewPdfApplicationRedirectCaseView")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(http("XUI_PRL_C100_400_015_ViewPdfApplicationRedirectEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/viewPdfDocument?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.case_fields[?(@.id=='draftOrderDoc')].value.document_url").saveAs("DocumentUrl"))
        .check(jsonPath("$.case_fields[?(@.id=='draftOrderDoc')].value.document_filename").saveAs("DocumentFileName"))
        .check(jsonPath("$.case_fields[?(@.id=='draftOrderDoc')].value.document_hash").saveAs("DocumentHash"))
        .check(jsonPath("$.event_token").saveAs("event_token")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * View PDF Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_405_ViewPdfProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * View PDF Continue
    ======================================================================================*/

    .group("XUI_PRL_C100_410_ViewPdfContinue") {

      exec(Common.caseShareOrgs)

      .exec(http("XUI_PRL_C100_410_005_ViewPdfContinue")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=viewPdfDocument1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLViewPdfContinue.json"))
        .check(substring("isEngDocGen")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * View PDF Submit
    ======================================================================================*/

    .group("XUI_PRL_C100_420_ViewPdfSubmit") {
      exec(http("XUI_PRL_C100_420_005_ViewPdfSubmitViewCase")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLViewPdfContinueSubmit.json")))

      .exec(http("XUI_PRL_C100_420_010_ViewPdfSubmitWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"viewPdfDocument","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_420_015_ViewPdfSubmit")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)


  val OtherPeopleInTheCase =

  /*======================================================================================
  * Click on 'Other People In The Case'
  ======================================================================================*/

    group("XUI_PRL_C100_430_OtherPeopleRedirect") {
      exec(http("XUI_PRL_C100_430_005_OtherPeopleRedirect")
        .get(BaseURL + "/cases/case-details/#{caseId}/trigger/otherPeopleInTheCaseRevised/otherPeopleInTheCaseRevised1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

        .exec(Common.caseActivityGet)

        .exec(http("XUI_PRL_C100_430_010_OtherPeopleRedirect")
          .get(BaseURL + "/data/internal/cases/${caseId}")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
          .header("x-xsrf-token", "${XSRFToken}")
         // .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
          .check(substring("otherPeopleInTheCaseRevised")))

        .exec(http("XUI_PRL_C100_430_015_OtherPeopleRedirect")
          .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/otherPeopleInTheCaseRevised?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(substring("Other people in the case"))
          .check(jsonPath("$.event_token").saveAs("event_token")))


        .exec(Common.userDetails)
        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
    * Other people in the case Details
    ======================================================================================*/

      .group("XUI_PRL_C100_440_Other_People_Details") {

        exec(http("XUI_PRL_C100_440_005_Other_People_Details")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=otherPeopleInTheCaseRevised1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLOtherPeopleDetails.json"))
          .check(substring("otherPartyInTheCaseRevised")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
  * Other people in the case Submit
  ======================================================================================*/

      .group("XUI_PRL_C100_450_Other_People_Submit") {

        exec(http("XUI_PRL_C100_450_005_Other_People_Submit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLOtherPeopleSubmit.json")))


          .exec(http("XUI_PRL_C100_450_015_ViewPdfSubmit")
            .get(BaseURL + "/data/internal/cases/${caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
            .header("x-xsrf-token", "${XSRFToken}")
            .check(substring("otherPeopleInTheCaseRevised"))
            .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)



  val OtherChildrenNotInTheCase =

  /*======================================================================================
  * Click on 'OtherChildrenNotInTheCase'
  ======================================================================================*/

    group("XUI_PRL_C100_460_Other_Children") {
      exec(http("XUI_PRL_C100_460_005_Other_Children")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/otherChildNotInTheCase/otherChildNotInTheCase1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

        .exec(Common.caseActivityGet)

        .exec(http("XUI_PRL_C100_460_010_Other_Children")
          .get(BaseURL + "/data/internal/cases/${caseId}")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
          .header("x-xsrf-token", "${XSRFToken}")
          // .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
          .check(substring("otherChildNotInTheCase")))

        .exec(http("XUI_PRL_C100_460_015_Other_Children")
          .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/otherChildNotInTheCase?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("event_token")))

        .exec(Common.userDetails)
        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
    * Do you or respondents have other children who are not part of this application? - no
    ======================================================================================*/

      .group("XUI_PRL_C100_470_Have_Other_Children") {

        exec(http("XUI_PRL_C100_470_005_Have_Other_Children")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=otherChildNotInTheCase1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLHave_Other_Children.json"))
          .check(substring("childrenNotPartInTheCaseYesNo")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
  * Other children not in the case Submit
  ======================================================================================*/

      .group("XUI_PRL_C100_480_Other_Children_Submit") {

        exec(http("XUI_PRL_C100_480_005_Other_Children_Submit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLOther_Children_Submit.json")))


          .exec(http("XUI_PRL_C100_480_010_Other_Children_Submit")
            .get(BaseURL + "/data/internal/cases/${caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
            .header("x-xsrf-token", "${XSRFToken}")
            .check(substring("otherPeopleInTheCaseRevised"))
            .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)


  val ChildrenAndApplicants =

  /*======================================================================================
  * Click on 'Children And Applicants'
  ======================================================================================*/

    group("XUI_PRL_C100_490_Children_And_Applicants") {
      exec(http("XUI_PRL_C100_490_005_Children_And_Applicants")
        .get(BaseURL + "/cases/case-details/#{caseId}/trigger/childrenAndApplicants/childrenAndApplicants1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.data.buffChildAndApplicantRelations[0].id").saveAs("buffChildAndApplicantRelationsId"))
        .check(jsonPath("$.event_token").saveAs("event_token")))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

        .exec(Common.caseActivityGet)

        .exec(http("XUI_PRL_C100_490_010_Children_And_Applicants")
          .get(BaseURL + "/data/internal/cases/${caseId}")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
          .header("x-xsrf-token", "${XSRFToken}")
          // .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
          .check(jsonPath("$.tabs[5].fields[6].formatted_value[0].id").saveAs("appId"))
          .check(jsonPath("$.tabs[5].fields[57].formatted_value[0].id").saveAs("childId"))
          .check(substring("childrenAndApplicants")))

   /*     .exec(http("XUI_PRL_C100_490_015_Children_And_Applicants")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/childrenAndApplicants?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.data.buffChildAndApplicantRelations[0].id").saveAs("buffChildAndApplicantRelationsId"))
          .check(jsonPath("$.event_token").saveAs("event_token")))

    */

        .exec(Common.userDetails)
        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
    * What is the applicant's relationship to the child?
    ======================================================================================*/

      .group("XUI_PRL_C100_500_Children_And_App_Relationship") {

        exec(http("XUI_PRL_C100_470_005_Have_Other_Children")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=childrenAndApplicants1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLChildrenAndAppRelationship.json"))
          .check(substring("buffChildAndApplicantRelations")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
  * Children and applicants submit
  ======================================================================================*/

      .group("XUI_PRL_C100_510_Children_And_App_Submit") {

        exec(http("XUI_PRL_C100_510_005_Children_And_App_Submit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLChildrenAndAppSubmit.json")))


          .exec(http("XUI_PRL_C100_510_010_Other_Children_Submit")
            .get(BaseURL + "/data/internal/cases/${caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
            .header("x-xsrf-token", "${XSRFToken}")
            .check(substring("otherPeopleInTheCaseRevised"))
            .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)


  val ChildrenAndRespondents =

  /*======================================================================================
  * Click on 'Children And Respondents '
  ======================================================================================*/

    group("XUI_PRL_C100_520_Children_And_Respondents") {
      exec(http("XUI_PRL_C100_520_005_Children_And_Respondents")
        .get(BaseURL + "/cases/case-details/#{caseId}/trigger/childrenAndRespondents/childrenAndRespondents1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

        .exec(Common.caseActivityGet)

        .exec(http("XUI_PRL_C100_520_010_Children_And_Respondents")
          .get(BaseURL + "/data/internal/cases/${caseId}")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
          .header("x-xsrf-token", "${XSRFToken}")
          // .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
          .check(jsonPath("$.tabs[5].fields[66].formatted_value[0].value.respondentId").saveAs("respondId"))
       //   .check(jsonPath("$.tabs[5].fields[57].formatted_value[0].id").saveAs("childId"))
          .check(substring("childrenAndApplicants")))

        .exec(http("XUI_PRL_C100_520_015_Children_And_Respondents")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/childrenAndApplicants?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.case_fields[0].formatted_value[0].id").saveAs("childrenAndRespondentsId"))
          .check(jsonPath("$.event_token").saveAs("event_token")))

        .exec(Common.userDetails)

        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
    * What is the respondent's relationship to the child?
    ======================================================================================*/

      .group("XUI_PRL_C100_530_Children_And_Respond_Relationship") {

        exec(http("XUI_PRL_C100_530_005_Children_And_Respond_Relationship")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=childrenAndRespondents1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLChildrenAndRespondRelationship.json"))
          .check(substring("buffChildAndRespondentRelations")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
  * Children and applicants submit
  ======================================================================================*/

      .group("XUI_PRL_C100_540_Children_And_Respond_Submit") {

        exec(http("XUI_PRL_C100_540_005_Children_And_Respond_Submit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLChildrenAndRespondSubmit.json")))


          .exec(http("XUI_PRL_C100_540_010_Children_And_Respond_Submit")
            .get(BaseURL + "/data/internal/cases/${caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
            .header("x-xsrf-token", "${XSRFToken}")
            .check(substring("childrenAndRespondents"))
            .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)


  val ChildrenAndOtherPeople =

  /*======================================================================================
  * Click on 'Children And Other People'
  ======================================================================================*/

    group("XUI_PRL_C100_550_Children_And_Other_People") {
      exec(http("XUI_PRL_C100_550_005_Children_And_Other_People")
        .get(BaseURL + "/cases/case-details/#{caseId}/trigger/childrenAndOtherPeople/childrenAndOtherPeople1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

        .exec(Common.configurationui)

        .exec(Common.configJson)

        .exec(Common.TsAndCs)

        .exec(Common.configUI)

        .exec(Common.userDetails)

        .exec(Common.isAuthenticated)

        .exec(Common.monitoringTools)

        .exec(Common.caseActivityGet)

        .exec(http("XUI_PRL_C100_550_010_Children_And_Other_People")
          .get(BaseURL + "/data/internal/cases/${caseId}")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
          .header("x-xsrf-token", "${XSRFToken}")
          // .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
          .check(jsonPath("$.tabs[5].fields[59].formatted_value[0].value.otherPeopleId").saveAs("otherPeopleId"))
          //   .check(jsonPath("$.tabs[5].fields[57].formatted_value[0].id").saveAs("childId"))
          .check(substring("childrenAndOtherPeople")))

        .exec(http("XUI_PRL_C100_550_015_Children_And_Other_People")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/childrenAndOtherPeople?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.case_fields[0].formatted_value[0].id").saveAs("childrenAndOtherPeopleId"))
          .check(jsonPath("$.event_token").saveAs("event_token")))

        .exec(Common.userDetails)

        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
    * What is their relationship to the child? - Guardian
    ======================================================================================*/

      .group("XUI_PRL_C100_560_Children_And_Other_Relationship") {

        exec(http("XUI_PRL_C100_560_005_Children_And_Other_Relationship")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=childrenAndOtherPeople1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLChildrenAndOtherRelationship.json"))
          .check(substring("buffChildAndRespondentRelations")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
  * Children and applicants submit
  ======================================================================================*/

      .group("XUI_PRL_C100_570_Children_And_Other_Submit") {

        exec(http("XUI_PRL_C100_570_005_Children_And_Other_Submit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100/PRLChildrenAndOtherSubmit.json")))


          .exec(http("XUI_PRL_C100_540_010_Children_And_Respond_Submit")
            .get(BaseURL + "/data/internal/cases/${caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
            .header("x-xsrf-token", "${XSRFToken}")
            .check(substring("childrenAndRespondents"))
            .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

          .exec(Common.userDetails)

      }
      .pause(MinThinkTime, MaxThinkTime)

  val SubmitAndPay =

    /*======================================================================================
    * Click on 'SubmitAndPay'
    ======================================================================================*/

    group("XUI_PRL_C100_430_SubmitAndPayRedirect") {
      exec(http("XUI_PRL_C100_430_005_SubmitAndPayRedirect")
        .get(BaseURL + "/cases/case-details/${caseId}/trigger/submitAndPay/submitAndPay1")
        .headers(Headers.navigationHeader)
        .header("x-xsrf-token", "${XSRFToken}"))

      .exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.isAuthenticated)

      .exec(Common.monitoringTools)

      .exec(Common.caseActivityGet)

      .exec(http("XUI_PRL_C100_430_010_SubmitAndPayRedirectCaseView")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='viewPdfDocument')]"))
        .check(jsonPath("$.state.id").is("AWAITING_SUBMISSION_TO_HMCTS")))

      .exec(http("XUI_PRL_C100_430_015_SubmitAndPayRedirectEvent")
        .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/submitAndPay?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("submitAndPay")))

      .exec(Common.userDetails)
      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Profile
    ======================================================================================*/

    .group("XUI_PRL_C100_435_SubmitAndPayProfile") {
      exec(Common.profile)
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Confidentiality Statement
    ======================================================================================*/

    .group("XUI_PRL_C100_440_SubmitAndPayConfidentialityStatement") {

      exec(http("XUI_PRL_C100_440_005_SubmitAndPayConfidentialityStatement")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=submitAndPay1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayConfidentialityStatement.json"))
        .check(substring("applicantSolicitorEmailAddress")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Declaration
    ======================================================================================*/

    .group("XUI_PRL_C100_445_SubmitAndPayDeclaration") {

      exec(http("XUI_PRL_C100_445_005_SubmitAndPayDeclaration")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=submitAndPay2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayDeclaration.json"))
        .check(substring("feeAmount")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Continue
    ======================================================================================*/

    .group("XUI_PRL_C100_450_SubmitAndPayContinue") {

      exec(http("XUI_PRL_C100_450_005_SubmitAndPayContinue")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=submitAndPay3")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayContinue.json"))
        .check(substring("paymentServiceRequestReferenceNumber")))

      .exec(Common.userDetails)

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit and Pay Now
    ======================================================================================*/

    .group("XUI_PRL_C100_460_SubmitAndPayNow") {
      exec(http("XUI_PRL_C100_460_005_SubmitAndPayNow")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100/PRLSubmitAndPayNow.json"))
        .check(substring("created_on")))

      .exec(http("XUI_PRL_C100_460_010_SubmitAndPayNowWorkAllocation")
        .post(BaseURL + "/workallocation/searchForCompletable")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(StringBody("""{"searchRequest":{"ccdId":"${caseId}","eventId":"submitAndPay","jurisdiction":"PRIVATELAW","caseTypeId":"PRLAPPS"}}"""))
        .check(substring("tasks")))

      .exec(http("XUI_PRL_C100_460_015_SubmitAndPayNowViewCase")
        .get(BaseURL + "/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .header("x-xsrf-token", "${XSRFToken}")
        .check(jsonPath("$.events[?(@.event_id=='submitAndPay')]"))
        .check(jsonPath("$.state.id").is("SUBMITTED_NOT_PAID")))

      .exec(Common.userDetails)

      .exec { session =>
        val fw = new BufferedWriter(new FileWriter("cases.csv", true))
        try {
          fw.write(session("caseId").as[String] + "\r\n")
        } finally fw.close()
        session
      }

    }
    .pause(MinThinkTime, MaxThinkTime)


  //
  //
  //
  //
  //





}