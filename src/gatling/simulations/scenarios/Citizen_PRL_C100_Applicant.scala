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
  val pcqURL = Environment.pcqURL
  val IdamUrl = Environment.idamURL
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular
  val postcodeFeeder = csv("postcodes.csv").circular

  // Variables for user flow control
  val hwfScreens = 1; // Controls whether or not to select help with fees (internal no redirect to gov.uk)
  val pcqScreens = 1; // Controls whether or not to go to PCQ questions

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  //========================================
  //Set VuserID to session Variable
  //=========================================
  //  exec(session => {
  //     val userId = session.userId // Get the VUser ID
  //     println(s"Assigned VUser ID: $userId") // Print to debug
  //     session.set("userId", userId)
  //   })

  val C100Case =

     exec(session => {
      val userId = session.userId // Get the VUser ID
      println(s"Assigned VUser ID: $userId") // Print to debug
      session.set("userId", userId)
    })

    /*======================================================================================
    * Citizen Home
    ======================================================================================*/

    .exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth()))

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
    * Before you start your application
    ======================================================================================*/

    .group("PRL_CitizenC100_040_WhereDoChildrenLive") {
      exec(http("PRL_CitizenC100_040_005_WhatYouWillNeed")
        .post(prlURL + "/c100-rebuild/start")
        .headers(Headers.navigationHeader)
        .header("content-type", "application/x-www-form-urlencoded")
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
        .check(substring("Do you have a written agreement with the other people")))
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
    * Before you go to court --> Continue
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
    * Other ways to reach an agreement - Continue
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
    * Attending a Mediation Information and Assessment Meeting MIAM --> Checkbox --> Continue
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

    //================================================================
    // Save the captured csrf token for use in subsequent requests URL (remains static)
    //================================================================
    .exec { session =>
    // Retrieve the captured value of "csrf" from the session
    val csrfValue = session("csrf").as[String]

    // Set a new session variable "savedCsrf" with the value of "csrf"
    session.set("staticCsrf", csrfValue)
    }

    /*======================================================================================
    * MIAM certificate Upload
    ======================================================================================*/

    .group("PRL_CitizenC100_180_MIAMUpload") {
      exec(http("PRL_CitizenC100_180_005_MIAMUpload")
        .post(prlURL+ "/c100-rebuild/miam/upload?_csrf=#{staticCsrf}")
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
    * MIAM certificate Upload Submit --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_190_MIAMUploadSubmit") {
      exec(http("PRL_CitizenC100_190_005_MIAMUploadSubmit")
        .post(prlURL + "/c100-rebuild/miam/upload?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/miam/upload-confirmation?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/typeoforder/select-courtorder?_csrf=#{staticCsrf}")
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
    * You would like the court to stop the other people in the application: --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_220_WouldLikeCourtTo") {
      exec(http("PRL_CitizenC100_220_005_WouldLikeCourtTo")
        .post(prlURL + "/c100-rebuild/typeoforder/caorder?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/typeoforder/shortstatement?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/hearing-urgency/urgent?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/hearing-urgency/urgent-details?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/hearing-without-notice/hearing-part1?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/hearing-without-notice/hearing-part2?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/child-details/add-children?_csrf=#{staticCsrf}")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("c100TempFirstName", "#{PRLRandomString}" + "First")
        .formParam("c100TempLastName", "#{PRLRandomString}" + "Last")
        .formParam("_ctx", "cd")
        .formParam("onlycontinue", "true")
        //.check(CsrfCheck.save)
        .check(headerRegex("location", """/c100-rebuild\/child-details\/(.{8}-.{4}-.{4}-.{4}-.{12})\/personal-details""").ofType[(String)].saveAs("childId"))
        .check(status.is(302)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Provide details for First Name
    ======================================================================================*/

    .group("PRL_CitizenC100_290_ChildDetails") {
      exec(http("PRL_CitizenC100_290_005_ChildDetails")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/personal-details?_csrf=#{staticCsrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("dateOfBirth-day", "#{PRLAppDobDay}") 
        .formParam("dateOfBirth-month", "#{PRLAppDobMonth}") 
        .formParam("dateOfBirth-year", "2018") // Temp hard coded to ensure a child of the correct age is created
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
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/child-matters?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/parental-responsibility?_csrf=#{staticCsrf}")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("statement", "Childs mother")
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
        .post(prlURL + "/c100-rebuild/child-details/further-information?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/child-details/has-other-children?_csrf=#{staticCsrf}")
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
        .post(prlURL + "/c100-rebuild/applicant/add-applicants?_csrf=#{staticCsrf}")
        .disableFollowRedirect
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("applicantFirstName", "#{PRLRandomString}" + "Applicant")
        .formParam("applicantLastName", "#{PRLRandomString}" + "Name")
        .formParam("saveAndContinue", "true")
        //.check(CsrfCheck.save)
        //.check(headerRegex("location", """c100-rebuild\/applicant\/(.{8}-.{4}-.{4}-.{4}-.{12})\/confidentiality\/details-know""").ofType[(String)].saveAs("applicantId"))
        .check(headerRegex("Location", """/c100-rebuild/refuge/staying-in-refuge/([0-9a-fA-F\-]{36})""").ofType[String].saveAs("applicantId"))
        //.check(headerRegex("Location", """\/c100-rebuild\/refuge/staying-in-refuge/\/(.{8}-.{4}-.{4}-.{4}-.{12})?""").ofType[(String)].saveAs("applicantId"))
        .check(status.is(302))
        )
    }

    .pause(MinThinkTime, MaxThinkTime)

   /*======================================================================================
    * Staying in a refuge
    ======================================================================================*/

    .group("PRL_CitizenC100_341_StayingInARefuge") {
      exec(http("PRL_CitizenC100_341_005_StayingInARefuge")
        .post(prlURL + "/c100-rebuild/refuge/staying-in-refuge/#{applicantId}")
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("isCitizenLivingInRefuge", "No")
        .formParam("onlyContinue", "true")
        .check(CsrfCheck.save)
        .check(status.in(302, 403, 200))
        .check(substring("Do the other people named")))
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
    * Provide details for Applicant - No, Male, Rand DOB
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
        //.check(css("input[name='_csrf']", "value").findAll.saveAs("csrfTokens")) // Capture all tokens here to select corect one later
        .check(substring("Current postcode")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Staying in a refuge
    ======================================================================================*/

    // .group("PRL_CitizenC100_400_StayingInARefuge") {
    //   exec(http("PRL_CitizenC100_400_005_StayingInARefuge")
    //     .post(prlURL + "/c100-rebuild/refuge/staying-in-refuge/#{applicantId}")
    //     .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    //     .header("content-type", "application/x-www-form-urlencoded")
    //     .formParam("_csrf", "#{csrf}")
    //     .formParam("isCitizenLivingInRefuge", "No")
    //     .formParam("onlyContinue", "true")
    //     .check(CsrfCheck.save)
    //     .check(status.in(302, 403, 200))
    //     .check(substring("Current postcode")))
    // }

    //  .pause(MinThinkTime, MaxThinkTime)

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
        .check(regex("""name="addressTown" type="text" value="(.+)"""").saveAs("town"))
        .check(substring("Address details of")))
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
        .formParam("addressHistory", "No")
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
      "PRLAppDobMonth" -> Common.getMonth()))

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
        .check(regex("""name="AddressLine1" type="text" value="(.+)" aria-describedby""").saveAs("address"))
        .check(regex("""name="PostTown" type="text" value="(.+)">""").saveAs("town"))
        .check(substring("Address details of")))
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
        .formParam("addressHistory", "no")
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
    // Do only for every 3rd user (Volume for these parts of the journey is lower)
     ======================================================================================*/
    .doIfOrElse(session => session("userId").as[Long] % 3 == 0) {
   
    /*======================================================================================
    ** Added steps for the new Other C8 Functionality & Docmosis changes **

    * Is there anyone else who should know about your application? --> Yes
    ======================================================================================*/

      group("PRL_CitizenC100_520_AnyoneElseYes") {
        exec(http("PRL_CitizenC100_520_005_AnyoneElseYes")
          .post(prlURL + "/c100-rebuild/other-person-details/other-person-check")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("oprs_otherPersonCheck", "Yes")
          .formParam("saveAndContinue", "true")
          .check(CsrfCheck.save)
          .check(substring("Enter the other person&#39;s name")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    ** Enter the other person's name
    ======================================================================================*/

     .group("PRL_CitizenC100_521_AddOtherPersons") {
        exec(http("PRL_CitizenC100_521_005_AddOtherPersons")
          .post(prlURL + "/c100-rebuild/other-person-details/add-other-persons")
          .disableFollowRedirect
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("c100TempFirstName", "#{PRLRandomString}" + "Other")
          .formParam("c100TempLastName", "#{PRLRandomString}" + "Last")
          .formParam("_ctx", "op")
          .formParam("onlycontinue", "true")
          .check(headerRegex("location", """/c100-rebuild\/other-person-details\/(.{8}-.{4}-.{4}-.{4}-.{12})\/personal-details""").ofType[(String)].saveAs("otherPersonId"))
          .check(status.is(302)))

        .exec(http("PRL_CitizenC100_521_010_AddOtherPersons")
          .get(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/personal-details")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .check(CsrfCheck.save)
          .check(substring("Provide details for")))
    
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    ** Provide details for
    ======================================================================================*/

    .exec(_.setAll(
      "PRLOtherDobDay" -> Common.getDay(),
      "PRLOtherDobMonth" -> Common.getMonth()))

    .group("PRL_CitizenC100_522_PersonalDetails") {
        exec(http("PRL_CitizenC100_522_005_PersonalDetails")
          .post(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/personal-details")
          .disableFollowRedirect
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("previousFullName", "")
          .formParam("hasNameChanged", "no")
          .formParam("gender", "Female")
          .formParam("dateOfBirth-day", "#{PRLOtherDobDay}")
          .formParam("dateOfBirth-month", "#{PRLOtherDobMonth}")
          .formParam("dateOfBirth-year", "1989")
          .formParam("isDateOfBirthUnknown", "")
          .formParam("approxDateOfBirth-day", "")
          .formParam("approxDateOfBirth-month", "")
          .formParam("approxDateOfBirth-year", "")
          .formParam("onlycontinue", "true")
          .check(headerRegex("location", """/c100-rebuild\/other-person-details\/#{otherPersonId}\/relationship-to-child/(.{8}-.{4}-.{4}-.{4}-.{12})""").ofType[(String)].saveAs("childId"))
          .check(status.is(302)))

        .exec(http("PRL_CitizenC100_522_010_PersonalDetails")
          .get(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/relationship-to-child/#{childId}")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .check(CsrfCheck.save)
          .check(substring("Someone who represents the rights of a child")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    ** Other person relatonship to child --> Guardian
    ======================================================================================*/

    .group("PRL_CitizenC100_523_RelationshipToChild") {
        exec(http("PRL_CitizenC100_523_005_RelationshipToChild")
          .post(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/relationship-to-child/#{childId}")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("relationshipType", "Guardian")
          .formParam("otherRelationshipTypeDetails", "")
          .formParam("onlycontinue", "true")
          .check(CsrfCheck.save)
          .check(substring("Staying in a refuge")))
      }

      .pause(MinThinkTime, MaxThinkTime)
    
    /*======================================================================================
    ** Staying in a refuge --> No --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_524_StayingInARefuge") {
        exec(http("PRL_CitizenC100_524_005_StayingInARefuge")
          .post(prlURL + "/c100-rebuild/refuge/staying-in-refuge/#{otherPersonId}")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("isCitizenLivingInRefuge", "No")
          .formParam("onlyContinue", "true")
          .check(CsrfCheck.save)
          .check(substring("Address of")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    ** Address of Other Person --> Enter postcode --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_525_AddressLookup") {
        exec(http("PRL_CitizenC100_525_005_AddressLookup")
          .post(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/address/lookup")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("PostCode", "KT11AN")
          .formParam("_ctx", "opAddressLookup")
          .formParam("onlycontinue", "true")
          .check(CsrfCheck.save)
          .check(regex("""<option value="([0-62]+)">""").findRandom.saveAs("addressIndex"))
          .check(substring("Select Address of")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    ** Select Address of Other Person --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_526_AddressSelect") {
        exec(http("PRL_CitizenC100_526_005_AddressSelect")
          .post(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/address/select")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("selectAddress", "#{addressIndex}")
          .formParam("onlycontinue", "true")
          .check(CsrfCheck.save)
          .check(regex("""name="AddressLine1" type="text" value="(.+)" aria-describedby""").saveAs("address"))
          .check(regex("""name="PostTown" type="text" value="(.+)">""").saveAs("town"))
          .check(substring("Address details of")))
      }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Applicant address input for Respondent
    ======================================================================================*/

    .group("PRL_CitizenC100_527_AddressManualContinue") {
      exec(http("PRL_CitizenC100_527_005_AddressManualContinue")
        .post(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/address/manual")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("AddressLine1", "#{address}")
        .formParam("AddressLine2", "")
        .formParam("PostTown", "#{town}")
        .formParam("County", "#{PRLRandomString}" + "County")
        .formParam("PostCode", "KT1 1AN")
        .formParam("Country", "United Kingdom")
        .formParam("addressUnknown", "")
        .formParam("_ctx", "opAddressManual")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Select the person that the child lives with most of the time")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Who does child mainly live with ? --> Other Person --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_528_MainlyLiveWith") {
      exec(http("PRL_CitizenC100_528_005_MainlyLiveWith")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/live-with/mainly-live-with")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("mainlyLiveWith", "#{otherPersonId}")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("living arrangements")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Living arrangements ? --> Other Person --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_529_LivingArrangements") {
      exec(http("PRL_CitizenC100_529_005_LivingArrangements")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/live-with/living-arrangements")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("liveWith", "")
        .formParam("liveWith", "")
        .formParam("liveWith", "")
        .formParam("liveWith", "#{otherPersonId}")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("identity private")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Keeping Identity Private  --> Yes --> Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_530_Confidentiality") {
      exec(http("PRL_CitizenC100_530_005_Confidentiality")
        .post(prlURL + "/c100-rebuild/other-person-details/#{otherPersonId}/confidentiality")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("confidentiality", "Yes")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Have you or the children ever been involved in court")))
    }

    .pause(MinThinkTime, MaxThinkTime)

        //=============================================================================== 
    } { // end of doif, start of else  // ** Usual journey for the rest of the vusers **
        //===============================================================================

    /*======================================================================================
    * Is there anyone else who should know about your application? --> No
    ======================================================================================*/

    group("PRL_CitizenC100_520_AnyoneElseNo") {
      exec(http("PRL_CitizenC100_520_005_AnyoneElseNo")
        .post(prlURL + "/c100-rebuild/other-person-details/other-person-check")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("oprs_otherPersonCheck", "No")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Select the person that the child lives with most of the time")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Who does the child mainly live with?
    ======================================================================================*/

    .group("PRL_CitizenC100_530_ChildMainlyLiveWith") {
      exec(http("PRL_CitizenC100_530_005_ChildMainlyLiveWith")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/live-with/mainly-live-with")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("mainlyLiveWith", "#{applicantId}")
        .formParam("onlycontinue", "true")
        .check(CsrfCheck.save)
        .check(substring("living arrangements")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Childs living arrangements - Select all of the people the child lives with - select top & Continue
    ======================================================================================*/

    .group("PRL_CitizenC100_535_LivingArrangements") {
      exec(http("PRL_CitizenC100_535_005_LivingArrangements")
        .post(prlURL + "/c100-rebuild/child-details/#{childId}/live-with/living-arrangements")
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
  
    } //** End of else - Return to normal journey all vusers **

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
        .check(substring("Which type of order")))
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
        .check(substring("Provide details of the order that you")))
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
    * Do I need documents in an alternative format
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
        .check(substring("Do you need help with paying the fee for this application?")))
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    // Do only for every 3rd user (Volume for these parts of the journey is lower)
     ======================================================================================*/
    .doIfOrElse(session => session("userId").as[Long] % 3 == 0) {

    /*======================================================================================
    * Do you need help with paying the fee for this application? - Yes & No 
    ======================================================================================*/
   
    group("PRL_CitizenC100_750_HelpWithPayingYes") {
      exec(http("PRL_CitizenC100_750_005_HelpWithPayingYes")
        .post(prlURL + "/c100-rebuild/help-with-fees/need-help-with-fees")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwf_needHelpWithFees", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Have you already applied")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Have you already applied for help with your application fee? - No 
    ======================================================================================*/
   
    .group("PRL_CitizenC100_751_FeesApplied") {
      exec(http("PRL_CitizenC100_751_005_FeesApplied")
        .post(prlURL + "/c100-rebuild/help-with-fees/fees-applied")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("helpWithFeesReferenceNumber", "")
        .formParam("hwf_feesAppliedDetails", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("You need to apply for help with your child")))
    }

    .pause(MinThinkTime, MaxThinkTime)

   /*======================================================================================
    * You need to apply for help with your child arrangements application fee - Enter ref number
    ======================================================================================*/
   
    .group("PRL_CitizenC100_752_EnterHWFRefNumber") {
      exec(http("PRL_CitizenC100_752_005_EnterHWFRefNumber")
        .post(prlURL + "/c100-rebuild/help-with-fees/hwf-guidance")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("helpWithFeesReferenceNumber", "HWF-A2B-12C")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    } { // End of HWF screens

    /*======================================================================================
    * Do you need help with paying the fee for this application? - No
    ======================================================================================*/

    group("PRL_CitizenC100_750_HelpWithPayingNo") {
      exec(http("PRL_CitizenC100_750_005_HelpWithPayingNo")
        .post(prlURL + "/c100-rebuild/help-with-fees/need-help-with-fees")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("hwf_needHelpWithFees", "No")
        .formParam("saveAndContinue", "true")
        //.formParam("saveAndComeLater", "true")
        .check(CsrfCheck.save)
        .check(substring("Check your answers")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    } //End of HWF steps

    /*======================================================================================
    * Check your Answers - New
    ======================================================================================*/

    .group("PRL_CitizenC100_760_CheckYourAnswers") {
      exec(http("PRL_CitizenC100_760_005_CheckYourAnswers")
        .post(prlURL + "/c100-rebuild/check-your-answers")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("statementOfTruth", "")
        .formParam("statementOfTruth", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Equality and diversity questions"))
        .check(status.in(302, 200)))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  // Do only for every 3rd user (Volume for these parts of the journey is lower)
    ======================================================================================*/
  .doIfOrElse(session => session("userId").as[Long] % 3 == 0) {
    /*======================================================================================
    * Equality and diversity questions - I don't want to answer these questions 
    ======================================================================================*/

    group("PRL_CitizenC100_761_PCQStartNo") {
      exec(http("PRL_CitizenC100_761_005_PCQStartNo")
        .post(pcqURL + "/opt-out")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("opt-out-button", "")
        .check(regex("""card-details" name="cardDetails" method="POST" action="/card_details/(.*)"""").optional.saveAs("chargeId"))
        .check(regex("""<strong>(.{16})<\/strong>""").optional.saveAs("caseId"))
        .check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(regex("""csrf2" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf2"))
        .check(status.in(302, 200)))
    } 

     .pause(MinThinkTime, MaxThinkTime)

    } { // End of doIf, start Else
    
  //.doIf(session => session("pcqScreens").as[Int] == 1) { // PCQ Steps if flag is set to 1
    /*======================================================================================
    * Equality and diversity questions - Continue to questions
    ======================================================================================*/

    group("PRL_CitizenC100_761_PCQStartYes") {
      exec(http("PRL_CitizenC100_761_005_PCQStartYes")
        .post(pcqURL + "/start-page")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .check(CsrfCheck.save)
        //.check(regex("""card-details" name="cardDetails" method="POST" action="/card_details/(.*)"""").optional.saveAs("chargeId"))
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        //.check(regex("""csrf2" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf2"))
        .check(substring("What is your main language"))
        .check(status.in(302, 200)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your Language? - English
    ======================================================================================*/

    .group("PRL_CitizenC100_762_SelectLanguage") {
      exec(http("PRL_CitizenC100_762_005_SelectLanguage")
        .post(pcqURL + "/language")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("language_main", "4")
        .formParam("language_other", "")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("What is your sex?"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your Sex? - Prefer not to say
    ======================================================================================*/

    .group("PRL_CitizenC100_763_SelectSex") {
      exec(http("PRL_CitizenC100_763_005_SelectSex")
        .post(pcqURL + "/sex")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sex", "0")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("Which of the following best describes how you think of yourself?"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - Which of the following best describes how you think of yourself? - Hetero or straight
    ======================================================================================*/

    .group("PRL_CitizenC100_764_SelectSexualOrientation") {
      exec(http("PRL_CitizenC100_764_005_SelectSexualOrientation")
        .post(pcqURL + "/sexual-orientation")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sexuality", "1")
        .formParam("sexuality_other", "")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("Are you married or in a legally registered civil partnership?"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

   /*======================================================================================
    * Equality and diversity questions - Are you married or in a legally registered civil partnership? - Yes
    ======================================================================================*/

    .group("PRL_CitizenC100_765_SelectMaritialStatus") {
      exec(http("PRL_CitizenC100_765_005_SelectMaritialStatus")
        .post(pcqURL + "/marital-status")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("marriage", "1")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("What is your ethnic group?"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your ethnic group? - Prefer not to say
    ======================================================================================*/

    .group("PRL_CitizenC100_766_SelectEthnicGroup") {
      exec(http("PRL_CitizenC100_766_005_SelectEthnicGroup")
        .post(pcqURL + "/ethnic-group")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ethnic_group", "0")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("What is your religion?"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)
  
    /*======================================================================================
    * Equality and diversity questions - What is your religion? - Prefer not to say
    ======================================================================================*/

    .group("PRL_CitizenC100_767_SelectReligion") {
      exec(http("PRL_CitizenC100_767_005_SelectReligion")
        .post(pcqURL + "/religion")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("religion_other", "")
        .formParam("religion", "0")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("Do you have any physical or mental health conditions"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - Do you have any physical or mental health conditions? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_768_HealthConditions") {
      exec(http("PRL_CitizenC100_768_005_HealthConditions")
        .post(pcqURL + "/disability")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("disability_conditions", "2")
        .check(CsrfCheck.save)
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("Are you pregnant or have you been pregnant in the last year?"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - Are you pregnant or have you been pregnant in the last year? - No
    ======================================================================================*/

    .group("PRL_CitizenC100_769_SelectPregnancy") {
      exec(http("PRL_CitizenC100_769_005_SelectPregnancy")
        .post(pcqURL + "/pregnant")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("pregnancy", "2")
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("You have answered the equality questions"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - You have answered the equality questions - Continue to next steps
    ======================================================================================*/

    .group("PRL_CitizenC100_7691_PCQReturnToService") {
      exec(http("PRL_CitizenC100_7691_005_PCQReturnToService")
        .get(pcqURL + "/return-to-service")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .check(regex("""card-details" name="cardDetails" method="POST" action="/card_details/(.*)"""").optional.saveAs("chargeId"))
        .check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(regex("""csrf2" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf2"))
        .check(regex("""<strong>(.{16})<\/strong>""").optional.saveAs("caseId"))
        .check(status.in(302, 200)))
    } 

    .pause(MinThinkTime, MaxThinkTime)

    } // End of PCQ steps

  .doIf(session => session("userId").as[Long] % 3 != 0) {
  // Only enter card details if HWF's was not selected.
  //.doIf(session => session("hwfScreens").as[Int] != 1) {
    /*======================================================================================
    * Enter card details
    ======================================================================================*/

     group("PRL_CitizenC100_770_EnterCardDetails") {
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
        .formParam("cardholderName", "#{PRLRandomString}Applicant #{PRLRandomString}Name")
        .formParam("cvc", "123")
        .formParam("addressCountry", "GB")
        .formParam("addressLine1", "12 Test Street")
        .formParam("addressLine2", "")
        .formParam("addressCity", "London")
        .formParam("addressPostcode", "KT25BU")
        .formParam("email", "#{PRLRandomString}" + "@gmail.com")
        .check(substring("Confirm your payment")))
     }

     .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Enter card details
    ======================================================================================*/
    .group("PRL_CitizenC100_780_FinalSubmit") {
       exec(http("PRL_CitizenC100_780_005_FinalSubmit")
        .post(PayURL + "/card_details/#{chargeId}/confirm")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("accept-language", "en-US,en;q=0.9")
        .header("origin", "https://card.payments.service.gov.uk")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("csrfToken", "#{csrf}")
        .formParam("chargeId", "#{chargeId}")
        .check(regex("""<strong>(.{16})<\/strong>""").saveAs("caseId"))
        .check(status.is(200)))
    }

    .pause(MinThinkTime, MaxThinkTime)

  } // End of If

    //Write case to file
    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("C100Cases.csv", true))
      try {
        fw.write(session("caseId").as[String] + "\r\n")
      } finally fw.close()
      session
    }

    /*======================================================================================
    * Logout
    ======================================================================================*/

  .group("PRL_CitizenC100_790_Logout") {
      exec(http("PRL_CitizenC100_790_005_Logout")
        .get(prlURL + "/logout")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(substring("Sign in or create an account")))
    }
    .pause(MinThinkTime, MaxThinkTime)

    }
