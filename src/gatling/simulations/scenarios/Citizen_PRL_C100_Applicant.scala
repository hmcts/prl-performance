package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Citizen_PRL_C100_Applicant {
  
  val BaseURL = Environment.baseURL
  val prlURL = Environment.prlURL
  val IdamUrl = Environment.idamURL
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular
  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val C100Case =

    /*======================================================================================
    * Citizen Home
    ======================================================================================*/

    exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth(),
      "PRLAppDobYear" -> Common.getDobYear(),
      "PRLChildDobYear" -> Common.getDobYearChild()))

    // .feed(PRLCitizens)

    .group("PRL_CitizenC100_010_PRLHome") {
      exec(http("PRL_CitizenC100_010_005_PRLHome")
        .get(prlURL)
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))
      }

    .pause(MinThinkTime, MaxThinkTime)

    /*===============================================================================================
    * Login
    ===============================================================================================*/

    .group("PRL_CitizenC100_020_Login") {
      exec(http("PRL_CitizenC100_020_005_Login")
        .post(IdamUrl + "/login?client_id=prl-citizen-frontend&response_type=code&redirect_uri=" + prlURL + "/receiver")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("username", "#{user}")
        .formParam("password", "#{password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "true")
        .formParam("_csrf", "#{csrf}")
        .check(substring("Child arrangements and family injunction cases")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select 'New child arrangements application (C100)'
    ======================================================================================*/

    .group("PRL_CitizenC100_025_ChildArrangementsApplication") {
      exec(http("PRL_CitizenC100_025_005_ChildArrangementsApplication")
        .get(prlURL + "/task-list/applicant")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(substring("Your application")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select 'Start new C100 application'
    ======================================================================================*/

    .group("PRL_CitizenC100_030_StartApplication") {
      exec(http("PRL_CitizenC100_030_005_StartApplication")
        .get(prlURL + "/c100-rebuild/start")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(substring("Before you start your application")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What you’ll need to complete your application
    ======================================================================================*/

    .group("PRL_CitizenC100_040_WhereDoChildrenLive") {
      exec(http("PRL_CitizenC100_040_005_WhatYouWillNeed")
        .get(prlURL + "/c100-rebuild/childaddress")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(CsrfCheck.save)
        .check(substring("Where do the children live?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Where do the children live? - Enter PostCode
    ======================================================================================*/

    .feed(postcodeFeeder)

    .group("PRL_CitizenC100_060_ChildrenPostCode") {
      exec(http("PRL_CitizenC100_060_005_ChildrenPostCode")
        .post(prlURL + "/c100-rebuild/childaddress")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c100RebuildChildPostCode", "#{postcode}")
        .formParam("saveAndContinue", "true")
        .check(substring("Do you have a written agreement with the other people in the case that you want the court to review?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you have a written agreement with the other people in the case that you want the court to review? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_070_WrittenAgreement") {
      exec(http("PRL_CitizenC100_070_005_WrittenAgreement")
        .post(prlURL + "/c100-rebuild/screening-questions/consent-agreement")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sq_writtenAgreement", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("Before you go to court")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Before you go to court
    ======================================================================================*/

    .group("PRL_CitizenC100_080_BeforeCourt") {
      exec(http("PRL_CitizenC100_080_005_BeforeCourt")
        .post(prlURL + "/c100-rebuild/screening-questions/alternative-resolution")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("saveAndContinue", "true")
        .check(substring("This is known as ‘non-court dispute resolution’")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Other ways to reach an agreement - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_090_ReachAgreement") {
      exec(http("PRL_CitizenC100_090_005_ReachAgreement")
        .post(prlURL + "/c100-rebuild/screening-questions/alternative-routes")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        // .formParam("sq_alternativeRoutes", "Yes")
        // .formParam("sq_agreementReason", "#{PRLRandomString}" + "agreementReason")
        .formParam("saveAndContinue", "true")
        .check(substring("Will you be using a legal representative in these proceedings?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Will you be using a legal representative in these proceedings? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_100_LegalRep") {
      exec(http("PRL_CitizenC100_100_005_LegalRep")
        .post(prlURL + "/c100-rebuild/screening-questions/legal-representation")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sq_legalRepresentation", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("Is there any reason that you would need permission")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Is there any reason that you would need permission from the court to make this application? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_110_PermissionFromCourt") {
      exec(http("PRL_CitizenC100_110_005_PermissionFromCourt")
        .post(prlURL + "/c100-rebuild/screening-questions/permission")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sq_courtPermissionRequired", "Yes")
        .formParam("saveAndContinue", "true")
        .check(substring("Why do you need a permission from the court to make this application?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Why do you need a permission from the court to make this application? - "I do not have parental responsibility for the children"
    ======================================================================================*/

    .group("PRL_CitizenC100_120_WhyPermissionFromCourt") {
      exec(http("PRL_CitizenC100_120_005_WhyPermissionFromCourt")
        .post(prlURL + "/c100-rebuild/screening-questions/permissions-why")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sq_permissionsWhy", "")
        .formParam("sq_permissionsWhy", "")
        .formParam("sq_permissionsWhy", "")
        .formParam("sq_permissionsWhy", "doNotHaveParentalResponsibility")
        .formParam("sq_doNotHaveParentalResponsibility_subfield", "#{PRLRandomString}" + "ParentalResponsibility")
        .formParam("sq_courtOrderPrevent_subfield", "")
        .formParam("sq_anotherReason_subfield", "")
        .formParam("saveAndContinue", "true")
        .check(substring("Explain why the court should grant you permission to submit this application")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Explain why the court should grant you permission to submit this application
    ======================================================================================*/

    .group("PRL_CitizenC100_130_WhyCourtShouldGrant") {
      exec(http("PRL_CitizenC100_130_005_WhyCourtShouldGrant")
        .post(prlURL + "/c100-rebuild/screening-questions/permissions-request")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sq_permissionsRequest", "#{PRLRandomString}" + "ParentalResponsibility")
        .formParam("saveAndContinue", "true")
        .check(substring("Are the children involved in any emergency protection, care or supervision proceedings (or have they been)?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are the children involved in any emergency protection, care or supervision proceedings? - no
    ======================================================================================*/

    .group("PRL_CitizenC100_140_InvolvedInEmergencyProtection") {
      exec(http("PRL_CitizenC100_140_005_InvolvedInEmergencyProtection")
        .post(prlURL + "/c100-rebuild/miam/other-proceedings")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("miam_otherProceedings", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("Attending a Mediation Information and Assessment Meeting")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Attending a Mediation Information and Assessment Meeting MIAM
    ======================================================================================*/

    .group("PRL_CitizenC100_150_AttendingMIAM") {
      exec(http("PRL_CitizenC100_150_005_AttendingMIAM")
        .post(prlURL + "/c100-rebuild/miam/miam-info")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("miam_consent", "")
        .formParam("miam_consent", "Yes")
        .formParam("saveAndContinue", "true")
        .check(substring("Have you attended")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Have you attended a Mediation Information and Assessment Meeting (MIAM)? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_160_AttendedMIAM") {
      exec(http("PRL_CitizenC100_160_005_AttendedMIAM")
        .post(prlURL + "/c100-rebuild/miam/attendance")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("miam_attendance", "Yes")
        .formParam("saveAndContinue", "true")
        .check(substring("Do you have a document signed by the mediator?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you have a document signed by the mediator? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_170_DocumentSigned") {
      exec(http("PRL_CitizenC100_170_005_DocumentSigned")
        .post(prlURL + "/c100-rebuild/miam/mediator-document")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("miam_haveDocSigned", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Upload your MIAM certificate")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM certificate Upload
    ======================================================================================*/

    .group("PRL_CitizenC100_180_MIAMUpload") {
      exec(http("PRL_CitizenC100_180_005_MIAMUpload")
        .post(prlURL+ "/c100-rebuild/miam/upload?_csrf=#{csrf}")
        .headers(Headers.uploadHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        .formParam("_csrf", "#{csrf}")
        .bodyPart(RawFileBodyPart("documents", "3MB.pdf")
        .contentType("application/pdf")
        .fileName("3MB.pdf")
        .transferEncoding("binary"))
        .check(substring("applicant__miam_certificate")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM certificate Upload Submit
    ======================================================================================*/

    .group("PRL_CitizenC100_190_MIAMUploadSubmit") {
      exec(http("PRL_CitizenC100_190_005_MIAMUploadSubmit")
        .post(prlURL + "/c100-rebuild/miam/upload?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("miamUpload", "true")
        .formParam("saveAndContinue", "true")
        .check(substring("Your MIAM certificate has been uploaded")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM certificate Upload Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_200_MIAMUploadContinue") {
      exec(http("PRL_CitizenC100_200_005_MIAMUploadContinue")
        .post(prlURL + "/c100-rebuild/miam/upload-confirmation")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("saveAndContinue", "true")
        .check(substring("What are you asking the court to do?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What are you asking the court to do? - Stop the other people in the application doing something
    ======================================================================================*/

    .group("PRL_CitizenC100_210_AskingCourtToDo") {
      exec(http("PRL_CitizenC100_210_005_AskingCourtToDo")
        .post(prlURL + "/c100-rebuild/typeoforder/select-courtorder")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("too_courtOrder", "")
        .formParam("too_courtOrder", "")
        .formParam("too_courtOrder", "")
        .formParam("too_courtOrder", "")
        .formParam("too_courtOrder", "stopOtherPeopleDoingSomething")
        .formParam("too_stopOtherPeopleDoingSomethingSubField", "")
        .formParam("too_stopOtherPeopleDoingSomethingSubField", "")
        .formParam("too_stopOtherPeopleDoingSomethingSubField", "")
        .formParam("too_stopOtherPeopleDoingSomethingSubField", "")
        .formParam("too_stopOtherPeopleDoingSomethingSubField", "")
        .formParam("too_stopOtherPeopleDoingSomethingSubField", "changeChildrenNameSurname")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("too_resolveSpecificIssueSubField", "")
        .formParam("onlycontinue", "true")
        .check(substring("You would like the court to stop the other people in the application:")))
    }
    
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * You would like the court to stop the other people in the application:
    ======================================================================================*/

    .group("PRL_CitizenC100_220_WouldLikeCourtTo") {
      exec(http("PRL_CitizenC100_220_005_WouldLikeCourtTo")
        .post(prlURL + "/c100-rebuild/typeoforder/caorder")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("onlycontinue", "true")
        .check(substring("Describe what you want the court to do regarding the children in this application")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Describe what you want the court to do regarding the children in this application
    ======================================================================================*/

    .group("PRL_CitizenC100_230_DescribeWhatCourt") {
      exec(http("PRL_CitizenC100_230_005_DescribeWhatCourt")
        .post(prlURL + "/c100-rebuild/typeoforder/shortstatement")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("too_shortStatement", "#{PRLRandomString}" + "WantCourtToDo")
        .formParam("onlycontinue", "true")
        .check(substring("Does your situation qualify for an urgent first hearing?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Does your situation qualify for an urgent first hearing? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_240_UrgentFirstHearing") {
      exec(http("PRL_CitizenC100_240_005_UrgentFirstHearing")
        .post(prlURL + "/c100-rebuild/hearing-urgency/urgent")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hu_urgentHearingReasons", "Yes")
        .formParam("saveAndContinue", "true")
        .check(substring("Tell us about your situation")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Tell us about your situation - Risk to my safety or the children's safety
    ======================================================================================*/

    .group("PRL_CitizenC100_250_UrgentHearingDetails") {
      exec(http("PRL_CitizenC100_250_005_UrgentHearingDetails")
        .post(prlURL + "/c100-rebuild/hearing-urgency/urgent-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "riskOfSafety")
        .formParam("hu_otherRiskDetails", "#{PRLRandomString}" + "RiskDetails")
        .formParam("hu_timeOfHearingDetails", "#{PRLAppDobDay}" + "Days")
        .formParam("hu_hearingWithNext48HrsMsg", "")
        .formParam("hu_hearingWithNext48HrsDetails", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("Are you asking for a without notice hearing?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are you asking for a without notice hearing? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_260_AskingForWithoutNotice") {
      exec(http("PRL_CitizenC100_260_005_AskingForWithoutNotice")
        .post(prlURL + "/c100-rebuild/hearing-without-notice/hearing-part1")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwn_hearingPart1", "Yes")
        .formParam("saveAndContinue", "true")
        .check(substring("Details of without notice hearing")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Details of without notice hearing
    ======================================================================================*/

    .group("PRL_CitizenC100_270_WithoutNoticeDetails") {
      exec(http("PRL_CitizenC100_270_005_WithoutNoticeDetails")
        .post(prlURL + "/c100-rebuild/hearing-without-notice/hearing-part2")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwn_reasonsForApplicationWithoutNotice", "#{PRLRandomString}" + "WithoutNotice")
        .formParam("hwn_doYouNeedAWithoutNoticeHearingDetails", "")
        .formParam("hwn_doYouNeedAWithoutNoticeHearing", "No")
        .formParam("hwn_doYouRequireAHearingWithReducedNoticeDetails", "")
        .formParam("hwn_doYouRequireAHearingWithReducedNotice", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("Enter the names of the children")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Name
    ======================================================================================*/

    .group("PRL_CitizenC100_280_ChildrenName"){
      exec(http("PRL_CitizenC100_280_005_ChildrenName")
        .post(prlURL + "/c100-rebuild/child-details/add-children")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c100TempFirstName", "#{PRLRandomString}" + "First")
        .formParam("c100TempLastName", "#{PRLRandomString}" + "Last")
        .formParam("_ctx", "cd")
        .formParam("onlycontinue", "true")
        .check(headerRegex("location", """/c100-rebuild\/child-details\/(.{8}-.{4}-.{4}-.{4}-.{12})\/personal-details""").ofType[(String)].saveAs("childId"))
        .check(status.is(302)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Name Redirect
    ======================================================================================*/

    .group("PRL_CitizenC100_285_ChildrenNameRedirect"){
      exec(http("PRL_CitizenC100_280_005_ChildrenName")
        .get(prlURL + "/c100-rebuild/child-details/#{childId}/personal-details")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .check(substring("Provide details for")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide details for First Name
    ======================================================================================*/

    .group("PRL_CitizenC100_290_ChildDetails") {
      exec(http("PRL_CitizenC100_290_005_ChildDetails")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/personal-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("dateOfBirth-day", "#{PRLAppDobDay}")
        .formParam("dateOfBirth-month", "#{PRLAppDobMonth}")
        .formParam("dateOfBirth-year", "#{PRLChildDobYear}")
        .formParam("isDateOfBirthUnknown", "")
        .formParam("approxDateOfBirth-day", "")
        .formParam("approxDateOfBirth-month", "")
        .formParam("approxDateOfBirth-year:", "")
        .formParam("gender", "Male")
        .formParam("otherGenderDetails", "")
        .formParam("_ctx", "pd")
        .formParam("onlycontinue", "true")
        .check(substring("Which of the decisions you’re asking the court to resolve relate to")))
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Which of the decisions you’re asking the court to resolve relate to First Name
    ======================================================================================*/

    .group("PRL_CitizenC100_300_WhichDecision") {

      exec(http("PRL_CitizenC100_300_005_WhichDecision")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/child-matters")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("needsResolution", "")
        .formParam("needsResolution", "changeChildrenNameSurname")
        .formParam("_ctx", "cm")
        .formParam("onlycontinue", "true")
        .check(substring("Parental responsibility for")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Parental responsibility for the First Child
    ======================================================================================*/

    .group("PRL_CitizenC100_310_ParentalResponsibility") {
      exec(http("PRL_CitizenC100_310_005_ParentalResponsibility")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/parental-responsibility")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("statement", "#{PRLRandomString}" + "ParentalResponsibility")
        .formParam("_ctx", "pr")
        .formParam("onlycontinue", "true")
        .check(substring("Further Information")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Further Information, yes and then no
    ======================================================================================*/

    .group("PRL_CitizenC100_320_FurtherInformation") {
      exec(http("PRL_CitizenC100_320_005_FurtherInformation")
        .post(prlURL + "/c100-rebuild/child-details/further-information")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("cd_childrenKnownToSocialServices", "Yes")
        .formParam("cd_childrenKnownToSocialServicesDetails", "#{PRLRandomString}" + "SocialServicesDetails")
        .formParam("cd_childrenSubjectOfProtectionPlan", "No")
        .formParam("onlycontinue", "true")
        .check(substring("Do you or any respondents have other children who are not part of this application?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you or any respondents have other children who are not part of this application? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_330_OtherChildren") {
      exec(http("PRL_CitizenC100_330_005_OtherChildren")
        .post(prlURL + "/c100-rebuild/child-details/has-other-children")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ocd_hasOtherChildren", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("Enter your name")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter your name
    ======================================================================================*/

    .group("PRL_CitizenC100_340_EnterYourName") {
      exec(http("PRL_CitizenC100_340_005_EnterYourName")
        .post(prlURL + "/c100-rebuild/applicant/add-applicants")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("applicantFirstName", "#{PRLRandomString}" + "Applicant")
        .formParam("applicantLastName", "#{PRLRandomString}" + "Name")
        .formParam("saveAndContinue", "true")
        .check(headerRegex("location", """c100-rebuild\/applicant\/(.{8}-.{4}-.{4}-.{4}-.{12})\/confidentiality\/details-know""").ofType[(String)].saveAs("applicantId"))
        .check(status.is(302)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter your name Redirect
    ======================================================================================*/

    .group("PRL_CitizenC100_345_EnterYourNameRedirect") {
      exec(http("PRL_CitizenC100_340_005_EnterYourName")
        .get(prlURL + "/c100-rebuild/applicant/#{applicantId}/confidentiality/details-know")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .check(substring("Do the other people named in this application (the respondents) know any of your contact details?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do the other people named in this application (the respondents) know any of your contact details? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_350_KnowContactDetails") {
      exec(http("PRL_CitizenC100_350_005_KnowContactDetails")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/confidentiality/details-know")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("detailsKnown", "No")
        .formParam("_ctx", "appl_detailsknow")
        .formParam("saveAndContinue", "true")
        .check(substring("Do you want to keep your contact details private from ")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you want to keep your contact details private from the other people named in the application (the respondents)? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_360_KeepDetailsPrivate") {
      exec(http("PRL_CitizenC100_360_005_KeepDetailsPrivate")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/confidentiality/start-alternative")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("contactDetailsPrivateAlternative", "")
        .formParam("contactDetailsPrivateAlternative", "")
        .formParam("contactDetailsPrivateAlternative", "")
        .formParam("contactDetailsPrivateAlternative", "email")
        .formParam("startAlternative", "No")
        .formParam("_ctx", "appl_start_alternative")
        .formParam("saveAndContinue", "true")
        .check(substring("The court will not keep your contact details private")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * The court will not keep your contact details private
    ======================================================================================*/

    .group("PRL_CitizenC100_370_KeepDetailsPrivateContinue") {
      exec(http("PRL_CitizenC100_370_005_KeepDetailsPrivateContinue")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/confidentiality/feedbackno")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("onlycontinue", "true")
        .check(substring("Provide details for")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide details for Applicant
    ======================================================================================*/

    .group("PRL_CitizenC100_380_ApplicantDetails") {
      exec(http("PRL_CitizenC100_380_005_ApplicantDetails")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/personal-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("applPreviousName", "")
        .formParam("haveYouChangeName", "No")
        .formParam("gender", "Male")
        .formParam("otherGenderDetails", "")
        .formParam("dateOfBirth-day", "#{PRLAppDobDay}")
        .formParam("dateOfBirth-month", "#{PRLAppDobMonth}")
        .formParam("dateOfBirth-year", "#{PRLChildDobYear}")
        .formParam("applicantPlaceOfBirth", "#{PRLRandomString}" + "PlaceOfBirth")
        .formParam("onlycontinue", "true")
        .check(substring("relationship to")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant relationship to first child
    ======================================================================================*/

    .group("PRL_CitizenC100_390_ApplicantRelationship") {
      exec(http("PRL_CitizenC100_390_005_ApplicantRelationship")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/relationship-to-child/#{childId}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("relationshipType", "Mother")
        .formParam("otherRelationshipTypeDetails", "")
        .formParam("onlycontinue", "true")
        .check(substring("Current postcode")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Postcode
    ======================================================================================*/

    .feed(postcodeFeeder)

    .group("PRL_CitizenC100_400_ApplicantPostcode") {
      exec(http("PRL_CitizenC100_400_005_ApplicantPostcode")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/address/lookup")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("addressPostcode", "#{postcode}")
        .formParam("onlycontinue", "true")
        .check(regex("""<option value="([0-9]+)">""").findRandom.saveAs("addressIndex")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Address
    ======================================================================================*/

    .group("PRL_CitizenC100_410_ApplicantSelectAddress") {
      exec(http("PRL_CitizenC100_410_005_ApplicantSelectAddress")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/address/select")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("selectAddress", "#{addressIndex}")
        .formParam("onlycontinue", "true")
        .check(regex("""name="address1" type="text" value="(.+)""").saveAs("address"))
        .check(regex("""name="addressTown" type="text" value="(.+)""").saveAs("town"))
        .check(substring("Building and street")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant address input
    ======================================================================================*/

    .group("PRL_CitizenC100_420_ApplicantAddress") {
      exec(http("PRL_CitizenC100_420_005_ApplicantAddress")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/address/manual")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("address1", "#{address}")
        .formParam("address2", "")
        .formParam("addressTown", "#{town}")
        .formParam("addressCounty", "#{PRLRandomString}" + "County")
        .formParam("addressPostcode", "#{postcode}")
        .formParam("country", "United Kingdom")
        .formParam("addressHistory", "Yes")
        .formParam("provideDetailsOfPreviousAddresses", "")
        .formParam("onlycontinue", "true")
        .check(substring("Contact details of")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Contact Details of Applicant
    ======================================================================================*/

    .group("PRL_CitizenC100_430_ApplicantContact") {
      exec(http("PRL_CitizenC100_430_005_ApplicantContact")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/contact-detail")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("canProvideEmail", "Yes")
        .formParam("emailAddress", "#{PRLRandomString}" + "@gmail.com")
        .formParam("canProvideTelephoneNumber", "Yes")
        .formParam("telephoneNumber", "07000000000")
        .formParam("canNotProvideTelephoneNumberReason", "")
        .formParam("canLeaveVoiceMail", "No")
        .formParam("onlycontinue", "true")
        .check(substring("Contact Preferences")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Contact Preference of Applicant
    ======================================================================================*/

    .group("PRL_CitizenC100_440_ApplicantContactPreference") {
      exec(http("PRL_CitizenC100_440_005_ApplicantContactPreference")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/contact-preference")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("applicantContactPreferences", "")
        .formParam("applicantContactPreferences", "")
        .formParam("applicantContactPreferences", "Digital")
        .formParam("onlycontinue", "true")
        .check(substring("Enter the respondent&#39;s name")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter the respondent's name
    ======================================================================================*/

    .group("PRL_CitizenC100_450_RespondentName") {
      exec(http("PRL_CitizenC100_450_005_RespondentName")
        .post(prlURL + "/c100-rebuild/respondent-details/add-respondents")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c100TempFirstName", "#{PRLRandomString}" + "Respondent")
        .formParam("c100TempLastName", "#{PRLRandomString}" + "Name")
        .formParam("_ctx", "resp")
        .formParam("onlycontinue", "true")
        .check(headerRegex("location", """c100-rebuild\/respondent-details\/(.{8}-.{4}-.{4}-.{4}-.{12})\/personal-details""").ofType[(String)].saveAs("respondentId"))
        .check(status.is(302)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter the respondent's name - redirect
    ======================================================================================*/

    .group("PRL_CitizenC100_460_RespondentNameRedirect") {
      exec(http("PRL_CitizenC100_460_005_RespondentNameRedirect")
        .get(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/personal-details")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .check(substring("Provide details for")))
    }
    
    .pause(MinThinkTime, MaxThinkTime)



}