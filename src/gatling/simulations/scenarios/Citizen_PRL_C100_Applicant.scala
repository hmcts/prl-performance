package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Citizen_PRL_C100_Applicant {
  
  val BaseURL = Environment.baseURL
  val PayURL = Environment.payURL
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
        .check(substring("Before you start your application"))
        .check(CsrfCheck.save))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What you’ll need to complete your application
    ======================================================================================*/

    .group("PRL_CitizenC100_040_WhereDoChildrenLive") {
      exec(http("PRL_CitizenC100_040_005_WhatYouWillNeed")
        .post(prlURL + "/c100-rebuild/start")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .formParam("_csrf", "#{csrf}")
        .formParam("onlyContinue", "true")
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
        .formParam("c100RebuildChildPostCode", "KT25BU")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Where do the children live?")))
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
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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
        .formParam("sq_doNotHaveParentalResponsibility_subfield", "No Parental Responsibility")
        .formParam("sq_courtOrderPrevent_subfield", "")
        .formParam("sq_anotherReason_subfield", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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
        .formParam("sq_permissionsRequest", "Perf test details")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
        .check(substring("Your MIAM certificate has been uploaded")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * MIAM certificate Upload Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_200_MIAMUploadContinue") {
      exec(http("PRL_CitizenC100_200_005_MIAMUploadContinue")
        .post(prlURL + "/c100-rebuild/miam/upload-confirmation?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("What are you asking the court to do?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What are you asking the court to do? - Stop the other people in the application doing something
    ======================================================================================*/

    .group("PRL_CitizenC100_210_AskingCourtToDo") {
      exec(http("PRL_CitizenC100_210_005_AskingCourtToDo")
        .post(prlURL + "/c100-rebuild/typeoforder/select-courtorder?_csrf=#{csrf}")
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
        .check(CsrfCheck.save)
        .check(substring("You would like the court to stop the other people in the application:")))
    }
    
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * You would like the court to stop the other people in the application:
    ======================================================================================*/

    .group("PRL_CitizenC100_220_WouldLikeCourtTo") {
      exec(http("PRL_CitizenC100_220_005_WouldLikeCourtTo")
        .post(prlURL + "/c100-rebuild/typeoforder/caorder?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Describe what you want the court to do regarding the children in this application")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Describe what you want the court to do regarding the children in this application
    ======================================================================================*/

    .group("PRL_CitizenC100_230_DescribeWhatCourt") {
      exec(http("PRL_CitizenC100_230_005_DescribeWhatCourt")
        .post(prlURL + "/c100-rebuild/typeoforder/shortstatement?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("too_shortStatement", "I want some perf testing")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Does your situation qualify for an urgent first hearing?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Does your situation qualify for an urgent first hearing? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_240_UrgentFirstHearing") {
      exec(http("PRL_CitizenC100_240_005_UrgentFirstHearing")
        .post(prlURL + "/c100-rebuild/hearing-urgency/urgent?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hu_urgentHearingReasons", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Tell us about your situation")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Tell us about your situation - Risk to my safety or the children's safety
    ======================================================================================*/

    .group("PRL_CitizenC100_250_UrgentHearingDetails") {
      exec(http("PRL_CitizenC100_250_005_UrgentHearingDetails")
        .post(prlURL + "/c100-rebuild/hearing-urgency/urgent-details?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "")
        .formParam("hu_reasonOfUrgentHearing", "riskOfSafety")
        .formParam("hu_otherRiskDetails", "PerfRiskDetails")
        .formParam("hu_timeOfHearingDetails", "10 Days")
        .formParam("hu_hearingWithNext48HrsMsg", "")
        .formParam("hu_hearingWithNext48HrsDetails", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Are you asking for a without notice hearing?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are you asking for a without notice hearing? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_260_AskingForWithoutNotice") {
      exec(http("PRL_CitizenC100_260_005_AskingForWithoutNotice")
        .post(prlURL + "/c100-rebuild/hearing-without-notice/hearing-part1?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwn_hearingPart1", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Details of without notice hearing")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Details of without notice hearing
    ======================================================================================*/

    .group("PRL_CitizenC100_270_WithoutNoticeDetails") {
      exec(http("PRL_CitizenC100_270_005_WithoutNoticeDetails")
        .post(prlURL + "/c100-rebuild/hearing-without-notice/hearing-part2?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwn_reasonsForApplicationWithoutNotice", "Some perf test details here")
        .formParam("hwn_doYouNeedAWithoutNoticeHearingDetails", "")
        .formParam("hwn_doYouNeedAWithoutNoticeHearing", "No")
        .formParam("hwn_doYouRequireAHearingWithReducedNoticeDetails", "")
        .formParam("hwn_doYouRequireAHearingWithReducedNotice", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Enter the names of the children")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Child Name
    ======================================================================================*/

    .group("PRL_CitizenC100_280_ChildrenName"){
      exec(http("PRL_CitizenC100_280_005_ChildrenName")
        .post(prlURL + "/c100-rebuild/child-details/add-children?_csrf=#{csrf}")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c100TempFirstName", "#{PRLRandomString}" + "First")
        .formParam("c100TempLastName", "#{PRLRandomString}" + "Last")
        .formParam("_ctx", "cd")
        .formParam("onlycontinue", "true")
        // .check(CsrfCheck.save)
        .check(headerRegex("location", """/c100-rebuild\/child-details\/(.{8}-.{4}-.{4}-.{4}-.{12})\/personal-details""").ofType[(String)].saveAs("childId"))
        .check(status.is(302)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide details for First Name
    ======================================================================================*/

    .group("PRL_CitizenC100_290_ChildDetails") {
      exec(http("PRL_CitizenC100_290_005_ChildDetails")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/personal-details?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("dateOfBirth-day", "#{PRLAppDobDay}") 
        .formParam("dateOfBirth-month", "#{PRLAppDobMonth}") 
        .formParam("dateOfBirth-year", "2018") // Temp hard coded to ensure a child of the correct year is created
        .formParam("isDateOfBirthUnknown", "")
        .formParam("approxDateOfBirth-day", "")
        .formParam("approxDateOfBirth-month", "")
        .formParam("approxDateOfBirth-year:", "")
        .formParam("gender", "Male")
        .formParam("otherGenderDetails", "")
        .formParam("_ctx", "pd")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Which of the decisions you’re asking the court to resolve relate to")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Which of the decisions you’re asking the court to resolve relate to First Name
    ======================================================================================*/

    .group("PRL_CitizenC100_300_WhichDecision") {

      exec(http("PRL_CitizenC100_300_005_WhichDecision")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/child-matters?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("needsResolution", "")
        .formParam("needsResolution", "changeChildrenNameSurname")
        .formParam("_ctx", "cm")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Parental responsibility for")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Parental responsibility for the First Child
    ======================================================================================*/

    .group("PRL_CitizenC100_310_ParentalResponsibility") {
      exec(http("PRL_CitizenC100_310_005_ParentalResponsibility")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/parental-responsibility?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("statement", "Perf ParentalResponsibility")
        .formParam("_ctx", "pr")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Further Information")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Further Information, yes and then no
    ======================================================================================*/

    .group("PRL_CitizenC100_320_FurtherInformation") {
      exec(http("PRL_CitizenC100_320_005_FurtherInformation")
        .post(prlURL + "/c100-rebuild/child-details/further-information?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("cd_childrenKnownToSocialServices", "Yes")
        .formParam("cd_childrenKnownToSocialServicesDetails", "Perf test SocialServicesDetails")
        .formParam("cd_childrenSubjectOfProtectionPlan", "No")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you or any respondents have other children who are not part of this application?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you or any respondents have other children who are not part of this application? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_330_OtherChildren") {
      exec(http("PRL_CitizenC100_330_005_OtherChildren")
        .post(prlURL + "/c100-rebuild/child-details/has-other-children?_csrf=#{csrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ocd_hasOtherChildren", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Enter your name")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter your name
    ======================================================================================*/

    .group("PRL_CitizenC100_340_EnterYourName") {
      exec(http("PRL_CitizenC100_340_005_EnterYourName")
        .post(prlURL + "/c100-rebuild/applicant/add-applicants?_csrf=#{csrf}")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("applicantFirstName", "#{PRLRandomString}" + "Applicant")
        .formParam("applicantLastName", "#{PRLRandomString}" + "Name")
        .formParam("saveAndContinue", "true")
        // .check(CsrfCheck.save)
        .check(headerRegex("location", """c100-rebuild\/applicant\/(.{8}-.{4}-.{4}-.{4}-.{12})\/confidentiality\/details-know""").ofType[(String)].saveAs("applicantId"))
        .check(status.is(302))
        )
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
        .check(CsrfCheck.save)
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
        .formParam("startAlternative", "No")
        .formParam("_ctx", "appl_start_alternative")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .formParam("dateOfBirth-year", "1980")
        .formParam("applicantPlaceOfBirth", "#{PRLRandomString}" + "PlaceOfBirth")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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
        .formParam("relationshipType", "Father")
        .formParam("otherRelationshipTypeDetails", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Current postcode")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant Postcode
    ======================================================================================*/

    .group("PRL_CitizenC100_400_ApplicantPostcode") {
      exec(http("PRL_CitizenC100_400_005_ApplicantPostcode")
        .post(prlURL + "/c100-rebuild/applicant/#{applicantId}/address/lookup")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("addressPostcode", "KT25BU")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .formParam("addressPostcode", "KT25BU")
        .formParam("country", "United Kingdom")
        .formParam("addressHistory", "Yes")
        .formParam("provideDetailsOfPreviousAddresses", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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
        .check(CsrfCheck.save)
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
        .formParam("applicantContactPreferences", "email")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
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

  val C100Case2 =

    /*======================================================================================
    * Enter the respondent's Details
    ======================================================================================*/

    exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth(),
      "PRLAppDobYear" -> Common.getDobYear(),
      "PRLChildDobYear" -> Common.getDobYearChild()))

    .group("PRL_CitizenC100_460_RespondentDetails") {
      exec(http("PRL_CitizenC100_460_005_RespondentDetails")
        .post(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/personal-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("previousFullName", "")
        .formParam("hasNameChanged", "no")
        .formParam("gender", "Female")
        .formParam("otherGenderDetails", "")
        .formParam("dateOfBirth-day", "#{PRLAppDobDay}")
        .formParam("dateOfBirth-month", "#{PRLAppDobMonth}")
        .formParam("dateOfBirth-year", "1980")
        .formParam("isDateOfBirthUnknown", "")
        .formParam("approxDateOfBirth-day", "")
        .formParam("approxDateOfBirth-month", "")
        .formParam("approxDateOfBirth-year", "")
        .formParam("respondentPlaceOfBirth", "#{PRLRandomString}" + "PlaceOfBirth")
        .formParam("respondentPlaceOfBirthUnknown", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("relationship to")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent's Relationship to child
    ======================================================================================*/

    .group("PRL_CitizenC100_470_RespondentRelationship") {
      exec(http("PRL_CitizenC100_470_005_RespondentRelationship")
        .post(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/relationship-to-child/#{childId}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("relationshipType", "Mother")
        .formParam("otherRelationshipTypeDetails", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Address of")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Respondent Postcode
    ======================================================================================*/

    .group("PRL_CitizenC100_480_RespondentPostcode") {
      exec(http("PRL_CitizenC100_480_005_RespondentPostcode")
        .post(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/address/lookup")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("PostCode", "KT25BU")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(regex("""<option value="([0-9]+)">""").findRandom.saveAs("addressIndex")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Address Respondent
    ======================================================================================*/

    .group("PRL_CitizenC100_490_RespondentSelectAddress") {
      exec(http("PRL_CitizenC100_490_005_RespondentSelectAddress")
        .post(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/address/select")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("selectAddress", "#{addressIndex}")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(regex("""name="AddressLine1" type="text" value="(.+)""").saveAs("address"))
        .check(regex("""name="PostTown" type="text" value="(.+)""").saveAs("town"))
        .check(substring("Building and street")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant address input for Respondent
    ======================================================================================*/

    .group("PRL_CitizenC100_500_RespondentAddress") {
      exec(http("PRL_CitizenC100_500_005_RespondentAddress")
        .post(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/address/manual")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("AddressLine1", "#{address}")
        .formParam("AddressLine2", "")
        .formParam("PostTown", "#{town}")
        .formParam("County", "#{PRLRandomString}" + "County")
        .formParam("PostCode", "KT25BU")
        .formParam("Country", "United Kingdom")
        .formParam("addressUnknown", "")
        .formParam("addressHistory", "yes")
        .formParam("provideDetailsOfPreviousAddresses", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Contact details of")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Contact details of Respondent
    ======================================================================================*/

    .group("PRL_CitizenC100_510_RespondentContact") {
      exec(http("PRL_CitizenC100_510_005_RespondentContact")
        .post(prlURL + "/c100-rebuild/respondent-details/#{respondentId}/contact-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("emailAddress", "#{PRLRandomString}" + "@gmail.com")
        .formParam("donKnowEmailAddress", "")
        .formParam("telephoneNumber", "07000000000")
        .formParam("donKnowTelephoneNumber", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Is there anyone else who should know about your application?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Is there anyone else who should know about your application?
    ======================================================================================*/

    .group("PRL_CitizenC100_520_AnyoneElse") {
      exec(http("PRL_CitizenC100_520_005_AnyoneElse")
        .post(prlURL + "/c100-rebuild/other-person-details/other-person-check")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("oprs_otherPersonCheck", "No")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("currently live with?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Who does the first child live with?
    ======================================================================================*/

    .group("PRL_CitizenC100_530_ChildLiveWith") {
      exec(http("PRL_CitizenC100_530_005_ChildLiveWith")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/live-with")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("liveWith", "")
        .formParam("liveWith", "")
        .formParam("liveWith", "#{applicantId}")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Have you or the children ever been involved in court proceedings?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Have you or the children ever been involved in court proceedings? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_540_InvolvedInCourt") {
      exec(http("PRL_CitizenC100_540_005_InvolvedInCourt")
        .post(prlURL + "/c100-rebuild/other-proceedings/current-previous-proceedings")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("op_childrenInvolvedCourtCase", "Yes")
        .formParam("op_courtOrderProtection", "Yes")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Provide details of court cases you or the children have been involved in")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide details of court cases you or the children have been involved in - A Child Arrangements Order
    ======================================================================================*/

    .group("PRL_CitizenC100_550_CourtCasesInfo") {
      exec(http("PRL_CitizenC100_550_005_CourtCasesInfo")
        .post(prlURL + "/c100-rebuild/other-proceedings/proceeding-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "")
        .formParam("op_courtProceedingsOrders", "childArrangementOrder")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Provide details of court cases you or the children have been involved in")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide details of court cases you or the children have been involved in
    ======================================================================================*/

    .group("PRL_CitizenC100_560_DetailsOfCourtCases") {
      exec(http("PRL_CitizenC100_560_005_DetailsOfCourtCases")
        .post(prlURL + "/c100-rebuild/other-proceedings/childArrangementOrder/order-details")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("orderDetail-1", "#{PRLRandomString}" + "OrderDetails")
        .formParam("caseNo-1", "123456")
        .formParam("orderDate-1-day", "#{PRLAppDobDay}")
        .formParam("orderDate-1-month", "#{PRLAppDobMonth}")
        .formParam("orderDate-1-year", "2023")
        .formParam("currentOrder-1", "")
        .formParam("orderEndDate-1-day", "")
        .formParam("orderEndDate-1-month", "")
        .formParam("orderEndDate-1-year", "")
        .formParam("orderCopy-1", "No")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Safety concerns")))
    }
    
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Safety concerns - Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_570_SafetyInfo") {
      exec(http("PRL_CitizenC100_570_005_SafetyInfo")
        .post(prlURL + "/c100-rebuild/safety-concerns/concern-guidance")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you have any concerns for your safety or the safety of the children?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you have any concerns for your safety or the safety of the children? - yes
    ======================================================================================*/

    .group("PRL_CitizenC100_580_SafetyConcerns") {
      exec(http("PRL_CitizenC100_580_005_SafetyConcerns")
        .post(prlURL + "/c100-rebuild/safety-concerns/concerns-for-safety")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_haveSafetyConcerns", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Who are you concerned about?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Who are you concerned about? - The children
    ======================================================================================*/

    .group("PRL_CitizenC100_590_ConcernedAbout") {
      exec(http("PRL_CitizenC100_590_005_ConcernedAbout")
        .post(prlURL + "/c100-rebuild/safety-concerns/concern-about")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_safetyConernAbout", "")
        .formParam("c1A_safetyConernAbout", "")
        .formParam("c1A_safetyConernAbout", "children")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("What type of behaviour have the children experienced or are at risk of experiencing?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What type of behaviour have the children experienced or are at risk of experiencing? - Physical abuse
    ======================================================================================*/

    .group("PRL_CitizenC100_600_TypeOfBehaviour") {
      exec(http("PRL_CitizenC100_600_005_TypeOfBehaviour")
        .post(prlURL + "/c100-rebuild/safety-concerns/child/concerns-about")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "")
        .formParam("c1A_concernAboutChild", "physicalAbuse")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Briefly describe the physical abuse against the children if you feel able to")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Briefly describe the physical abuse against the children if you feel able to
    ======================================================================================*/

    .group("PRL_CitizenC100_610_DescribePhysicalAbuse") {
      exec(http("PRL_CitizenC100_610_005_DescribePhysicalAbuse")
        .post(prlURL + "/c100-rebuild/safety-concerns/child/report-abuse/physicalAbuse")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("childrenConcernedAbout", "")
        .formParam("childrenConcernedAbout", "#{childId}")
        .formParam("behaviourDetails", "#{PRLRandomString}" + "behaviourDetails")
        .formParam("behaviourStartDate", "01/01/2024")
        .formParam("isOngoingBehaviour", "No")
        .formParam("seekHelpDetails", "")
        .formParam("seekHelpFromPersonOrAgency", "No")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Have the children been impacted by drug, alcohol or substance abuse?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Have the children been impacted by drug, alcohol or substance abuse? - yes
    ======================================================================================*/

    .group("PRL_CitizenC100_620_ImpactedByDrug") {
      exec(http("PRL_CitizenC100_620_005_ImpactedByDrug")
        .post(prlURL + "/c100-rebuild/safety-concerns/other-concerns/drugs")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_otherConcernsDrugs", "Yes")
        .formParam("c1A_otherConcernsDrugsDetails", "Perf ConcernsDrugsDetails")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you have any other concerns about the children’s safety and wellbeing?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you have any other concerns about the children’s safety and wellbeing? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_630_OtherConcerns") {
      exec(http("PRL_CitizenC100_630_005_OtherConcerns")
        .post(prlURL + "/c100-rebuild/safety-concerns/other-concerns/other-issues")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_childSafetyConcernsDetails", "")
        .formParam("c1A_childSafetyConcerns", "No")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("What do you want the court to do to keep you and the children safe?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What do you want the court to do to keep you and the children safe?
    ======================================================================================*/

    .group("PRL_CitizenC100_640_KeepChildrenSafe") {
      exec(http("PRL_CitizenC100_640_005_KeepChildrenSafe")
        .post(prlURL + "/c100-rebuild/safety-concerns/orders-required/court-action")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_keepingSafeStatement", "Perf testkeepingSafe")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Contact between the children and the other people in this application")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Contact between the children and the other people in this application
    ======================================================================================*/

    .group("PRL_CitizenC100_650_ContactBetweenChildren") {
      exec(http("PRL_CitizenC100_650_005_ContactBetweenChildren")
        .post(prlURL + "/c100-rebuild/safety-concerns/orders-required/unsupervised")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c1A_supervisionAgreementDetails", "Yes, but I prefer that it is supervised")
        .formParam("c1A_agreementOtherWaysDetails", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Are the children&#39;s lives mainly based outside of England and Wales?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are the children's lives mainly based outside of England and Wales? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_660_ChildrenBasedOutsideEngland") {
      exec(http("PRL_CitizenC100_660_005_ChildrenBasedOutsideEngland")
        .post(prlURL + "/c100-rebuild/international-elements/start")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ie_provideDetailsStart", "")
        .formParam("ie_internationalStart", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Are the children&#39;s parents (or anyone significant to the children) mainly based outside of England and Wales?")))
    }
    
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Are the children's parents (or anyone significant to the children) mainly based outside of England and Wales? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_670_ParentsBasedOutsideEngland") {
      exec(http("PRL_CitizenC100_670_005_ParentsBasedOutsideEngland")
        .post(prlURL + "/c100-rebuild/international-elements/parents")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ie_provideDetailsParents", "")
        .formParam("ie_internationalParents", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Could another person in the application apply for a similar order in a country outside England or Wales?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Could another person in the application apply for a similar order in a country outside England or Wales? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_680_AnotherPersonApply") {
      exec(http("PRL_CitizenC100_680_005_AnotherPersonApply")
        .post(prlURL + "/c100-rebuild/international-elements/jurisdiction")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ie_provideDetailsJurisdiction", "")
        .formParam("ie_internationalJurisdiction", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Has another country asked (or been asked) for information or help for the children?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Has another country asked (or been asked) for information or help for the children? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_690_AnotherCountryAsked") {
      exec(http("PRL_CitizenC100_690_005_AnotherCountryAsked")
        .post(prlURL + "/c100-rebuild/international-elements/request")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ie_provideDetailsRequest", "")
        .formParam("ie_internationalRequest", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("part in hearings by video and phone?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Would you be able to take part in hearings by video and phone?
    ======================================================================================*/

    .group("PRL_CitizenC100_700_TakePartInHearings") {
      exec(http("PRL_CitizenC100_700_005_TakePartInHearings")
        .post(prlURL + "/c100-rebuild/reasonable-adjustments/attending-court")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ra_typeOfHearing", "")
        .formParam("ra_typeOfHearing", "")
        .formParam("ra_typeOfHearing", "")
        .formParam("ra_typeOfHearing", "videoHearing")
        .formParam("ra_noVideoAndPhoneHearing_subfield", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Give details of the language you require")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you have any language requirements?
    ======================================================================================*/

    .group("PRL_CitizenC100_710_LanguageRequirements") {
      exec(http("PRL_CitizenC100_710_005_LanguageRequirements")
        .post(prlURL + "/c100-rebuild/reasonable-adjustments/language-requirements")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ra_languageNeeds", "")
        .formParam("ra_languageNeeds", "")
        .formParam("ra_languageNeeds", "")
        .formParam("ra_languageNeeds", "")
        .formParam("ra_needInterpreterInCertainLanguage_subfield", "")
        .formParam("ra_languageNeeds", "noLanguageRequirements")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you or the children need special arrangements at court?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you or the children need special arrangements at court?
    ======================================================================================*/

    .group("PRL_CitizenC100_720_SpecialArrangements") {
      exec(http("PRL_CitizenC100_720_005_SpecialArrangements")
        .post(prlURL + "/c100-rebuild/reasonable-adjustments/special-arrangements")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "")
        .formParam("ra_specialArrangements", "separateWaitingRoom")
        .formParam("ra_specialArrangementsOther_subfield", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you have a physical, mental or learning disability or health condition that means you need support during your case?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * DG Do you have a physical, mental or learning disability or health condition that means you need support during your case?
    ======================================================================================*/

    .group("PRL_CitizenC100_730_010_SpecialArrangements") {
      exec(http("PRL_CitizenC100_730_010_support-during-your-case")
        .post(prlURL + "/c100-rebuild/reasonable-adjustments/support-during-your-case")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ra_disabilityRequirements", "")
        .formParam("ra_disabilityRequirements", "")
        .formParam("ra_disabilityRequirements", "")
        .formParam("ra_disabilityRequirements", "")
        .formParam("ra_disabilityRequirements", "")
        .formParam("ra_disabilityRequirements", "")
        .formParam("ra_disabilityRequirements", "documentsHelp")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("I need documents in an alternative format")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * DG I need documents in an alternative format
    ======================================================================================*/

    .group("PRL_CitizenC100_740_015_SpecialArrangements") {
      exec(http("PRL_CitizenC100_740_015_documents-support")
        .post(prlURL + "/c100-rebuild/reasonable-adjustments/documents-support")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "")
        .formParam("ra_documentInformation", "specifiedColorDocuments")
        .formParam("ra_specifiedColorDocuments_subfield", "In Colour Blue")
        .formParam("ra_largePrintDocuments_subfield", "")
        .formParam("ra_documentHelpOther_subfield", "")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you need help with paying the fee for this application?"))
        )
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you need help with paying the fee for this application? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_750_HelpWithPaying") {
      exec(http("PRL_CitizenC100_750_005_HelpWithPaying")
        .post(prlURL + "/c100-rebuild/help-with-fees/need-help-with-fees")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwf_needHelpWithFees", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Check your Answers"))
      )
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check your Answers
    ======================================================================================*/

    .group("PRL_CitizenC100_760_CheckYourAnswers") {
      exec(http("PRL_CitizenC100_760_005_CheckYourAnswers")
        .post(prlURL + "/c100-rebuild/check-your-answers")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("statementOfTruth", "")
        .formParam("statementOfTruth", "Yes")
        .formParam("saveAndContinue", "true")
        .check(headerRegex("Location", """https:\/\/card.payments.service.gov.uk\/secure\/(.{8}-.{4}-.{4}-.{4}-.{12})""").ofType[(String)].saveAs("paymentId"))
        .check(status.in(302, 403, 200)))

      .exec(http("PRL_CitizenC100_760_010_GetPaymentLink")
        .get(PayURL + "/secure/#{paymentId}")
        .headers(Headers.navigationHeader)
        .header("accept", "*/*")
        .header("content-type", "application/x-www-form-urlencoded")
        .header("origin", "https://card.payments.service.gov.uk")
        .check(bodyString.saveAs("responseBody"))
        .check(regex("""charge-id" name="chargeId" type="hidden" value="(.*)"/>""").saveAs("chargeId"))
        .check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"/>""").saveAs("csrf")))

      .exec(http("PRL_CitizenC100_760_015_CheckYourAnswersFinal")
        .get(PayURL + "/card_details/#{chargeId}")
        .headers(Headers.navigationHeader)
        .check(CsrfCheck2.save)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .check(substring("Enter card details")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter card details
    ======================================================================================*/

    .group("PRL_CitizenC100_770_EnterCardDetails") {
      exec(http("PRL_CitizenC100_770_005_EnterCardDetails")
        .post(PayURL + "/card_details/#{chargeId}")
        .headers(Headers.commonHeader)
        .check(CsrfCheck2.save)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .formParam("chargeId", "#{chargeId}")
        .formParam("csrfToken", "#{csrf}")
        .formParam("cardNo", "4444333322221111")
        .formParam("expiryMonth", "04")
        .formParam("expiryYear", "27")
        .formParam("cardholderName", "#{ApplicantFirstName} #{ApplicantLastName}")
        .formParam("cvc", "123")
        .formParam("addressCountry", "GB")
        .formParam("addressLine1", "12 Test Street")
        .formParam("addressLine2", "")
        .formParam("addressCity", "London")
        .formParam("addressPostcode", "KT25BU")
        .formParam("email", "#{PRLRandomString}" + "@gmail.com")
        .check(substring("Confirm your payment")))

      .exec(http("PRL_CitizenC100_770_010_FinalSubmit")
        .post(PayURL + "/card_details/#{chargeId}/confirm")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("accept-language", "en-US,en;q=0.9")
        .header("origin", "https://card.payments.service.gov.uk")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("csrfToken", "#{csrf}")
        .formParam("chargeId", "#{chargeId}")
        .check(regex("""<strong>(.{16})<\/strong>""").saveAs("caseNumber"))
        .check(status.is(200)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("C100Cases.csv", true))
      try {
        fw.write(session("caseNumber").as[String] + "\r\n")
      } finally fw.close()
      session
    }

    /*======================================================================================
    * Logout
    ======================================================================================*/

    .group("PRL_CitizenC100_780_Logout") {
      exec(http("PRL_CitizenC100_780_005_Logout")
        .get(prlURL + "/logout")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(substring("Sign in or create an account")))
    }

}