package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

/*======================================================================================
* Create a new Bail application as a professional user (e.g. Legal Rep)
======================================================================================*/

object Solicitor_Bails {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CreateBailApplication =

    exec(_.setAll(
      "BailsRandomString" -> (Common.randomString(7)),
      "BailsRandomNumber" -> (Common.randomNumber(7)),
      "BailsDobDay" -> Common.getDay(),
      "BailsDobMonth" -> Common.getMonth(),
      "BailsDobYear" -> Common.getDobYear(),
      "BailsArrivedYear" -> Common.getDobYearChild(),
      "BailsPhoneNumber" -> ("07" + Common.randomNumber(9)),
      "BailsSupporterEmail" -> (Common.randomString(7) + "@gmail.com")))


    /*======================================================================================
    * Click the Create Case link
    ======================================================================================*/

    .group("XUI_Bails_030_CreateCase") {
      exec(http("XUI_Bails_030_005_CreateCase")
        .get(BaseURL + "/aggregated/caseworkers/:uid/jurisdictions?access=create")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(substring("Bail")))

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*=============================================================================================
    * Jurisdiction = Immigration & Asylum; Case Type = Bail* master; Event = Start The Application
    ===============================================================================================*/

    .group("XUI_Bails_040_SelectCaseType") {

      exec(http("XUI_Bails_040_005_SelectCaseType")
        .get(BaseURL + "/data/internal/case-types/Bail/event-triggers/startApplication?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("startApplication")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Previous applications? - No
    ======================================================================================*/

    .group("XUI_Bails_050_Previous_Applications") {
      exec(http("XUI_Bails_050_005_Previous_Applications")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationhasPreviousBailApplication")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsPreviousApplications.json"))
        .check(substring("hasPreviousBailApplication")))

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Create new application
    ======================================================================================*/

    .group("XUI_Bails_060_CreateNewApplication") {
      exec(http("XUI_Bails_060_005_CreateNewApplication")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationnoAccessToPreviousApplications")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsCreateNewApplication.json"))
        .check(substring("startApplication")))

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Before you start
    ======================================================================================*/

    .group("XUI_Bails_070_Before_You_Start") {
      exec(http("XUI_Bails_070_005_Before_You_Start")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationbeforeYouStart")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsBeforeYouStart.json"))
        .check(substring("startApplicationbeforeYouStart")))

    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What is the applicant’s name?
    ======================================================================================*/

    .group("XUI_Bails_080_Applicant_Name") {
      exec(http("XUI_Bails_080_005_Applicant_Name")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationapplicantNameDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsApplicantName.json"))
        .check(substring("applicantFamilyName")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * What is the applicant’s date of birth?
    ======================================================================================*/

    .group("XUI_Bails_090_Applicant_DoB") {
      exec(http("XUI_Bails_090_005_DoB")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationapplicantDateOfBirthDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsApplicantDoB.json"))
        .check(substring("applicantDateOfBirth")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * What is the applicant’s gender?
    ======================================================================================*/

    .group("XUI_Bails_100_Applicant_Gender") {
      exec(http("XUI_Bails_100_005_Applicant_Gender")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationapplicantGenderDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsApplicantGender.json"))
        .check(substring("applicantGender")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * What is the applicant’s nationality?
    ======================================================================================*/

    .group("XUI_Bails_110_Applicant_Nationality") {
      exec(http("XUI_Bails_110_005_Applicant_Nationality")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationapplicantNationalityDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsApplicantNationality.json"))
        .check(substring("applicantNationality")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * What is the applicant’s Home Office reference number?
    ======================================================================================*/

    .group("XUI_Bails_120_Home_Office_Ref_Number") {
      exec(http("XUI_Bails_120_005_Home_Office_Ref_Number")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationhomeOfficeReferenceNumber")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsHomeOfficeRefNumber.json"))
        .check(substring("homeOfficeReferenceNumber")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Where is the applicant detained? - Prison
    ======================================================================================*/

    .group("XUI_Bails_130_Where_Is_Applicant_Detained") {
      exec(http("XUI_Bails_130_005_Where_Is_Applicant_Detained")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationhomeOfficeReferenceNumber")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailPrisonDetails.json"))
        .check(substring("applicantDetainedLoc")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * In which prison is the applicant detained?
    ======================================================================================*/

    .group("XUI_Bails_140_Which_Prison") {
      exec(http("XUI_Bails_140_005_Which_Prison")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationhomeOfficeReferenceNumber")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailWhichPrison.json"))
        .check(substring("prisonName")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * What date did the applicant arrive in the UK?
    ======================================================================================*/

    .group("XUI_Bails_150_Date_Arrived_In_UK?") {
      exec(http("XUI_Bails_150_005_Date_Arrived_In_UK")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationapplicantArrivalInUK")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailDateArrivedInUK.json"))
        .check(jsonPath("$.data.applicantNationalities[0].id").saveAs("applicantNationalitiesId"))
        .check(substring("applicantArrivalInUKDate")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Does the applicant have access to a mobile phone? - yes
    ======================================================================================*/

    .group("XUI_Bails_160_Mobile_Phone") {
      exec(http("XUI_Bails_160_005_Mobile_Phone")
        .post(BaseURL + "/data/case-types/Bail/validate?pageId=startApplicationapplicantMobileDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailMobilePhone.json"))
        .check(substring("applicantHasMobile")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*========================================================================================================================================
    * Does the applicant have an appeal hearing pending in the First-tier Tribunal (IAC)? - Yes, but I don't know the appeal reference number
    ========================================================================================================================================*/

    .group("XUI_Bails_170_IAC") {
      exec(http("XUI_Bails_170_005_IAC")
        .post("/data/case-types/Bail/validate?pageId=startApplicationhasAppealHearingPending")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsIAC.json"))
        .check(substring("hasAppealHearingPending")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Does the applicant have somewhere to live if this bail application is granted? - Yes
    ========================================================================================*/

    .group("XUI_Bails_180_Place_To_Live") {
      exec(http("XUI_Bails_180_005_Place_To_Live")
        .post("/data/case-types/Bail/validate?pageId=startApplicationapplicantHasAddress")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsHasAddress.json"))
        .check(substring("applicantHasAddress")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * At what address will the applicant live if this bail application is granted?
    ========================================================================================*/

    .group("XUI_Bails_190_Applicant_Address") {

      exec(Common.postcodeLookup)

      .exec(http("XUI_Bails_190_005_Applicant_Address")
        .post("/data/case-types/Bail/validate?pageId=startApplicationapplicantAddressDetail")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsApplicantAddress.json"))
        .check(substring("applicantAddress")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Does the applicant agree to be bound by a financial condition? - yes
    ========================================================================================*/

    .group("XUI_Bails_200_Financial_Condition") {

      exec(http("XUI_Bails_200_005_Financial_Condition")
        .post("/data/case-types/Bail/validate?pageId=startApplicationfinancialConditionAgree")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialCondition.json"))
        .check(substring("agreesToBoundByFinancialCond")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Does the applicant have a financial condition supporter? - yes
    ========================================================================================*/

    .group("XUI_Bails_210_Financial_Supporter") {

      exec(http("XUI_Bails_210_005_Financial_Supporter")
        .post("/data/case-types/Bail/validate?pageId=startApplicationfinancialConditionSupporter")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialConditionSupporter.json"))
        .check(substring("hasFinancialCondSupporter")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s name?
    ========================================================================================*/

    .group("XUI_Bails_220_Financial_Supporter_Name") {

      exec(http("XUI_Bails_220_005_Financial_Supporter_Name")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterNameDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterName.json"))
        .check(substring("supporterFamilyNames")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s address?
    ========================================================================================*/

    .group("XUI_Bails_230_Financial_Supporter_Address") {

      exec(http("XUI_Bails_230_005_Financial_Supporter_Address")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterAddressDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterAddress.json"))
        .check(substring("supporterAddressDetails")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What are the financial condition supporter’s contact details? - Mobile and Email
    ========================================================================================*/

    .group("XUI_Bails_240_Financial_Supporter_Contact") {

      exec(http("XUI_Bails_240_005_Financial_Supporter_Contact")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterContactDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterContact.json"))
        .check(substring("supporterContactDetails")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s date of birth?
    ========================================================================================*/

    .group("XUI_Bails_250_Financial_Supporter_Contact_DoB") {

      exec(http("XUI_Bails_250_005_Financial_Supporter_Contact_DoB")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterDOB")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterDoB.json"))
        .check(substring("agreesToBoundByFinancialCond")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s relationship to the applicant?
    ========================================================================================*/

    .group("XUI_Bails_260_Financial_Supporter_Relationship") {

      exec(http("XUI_Bails_260_005_Financial_Supporter_Relationship")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterRelation")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterRelationship.json"))
        .check(substring("supporterRelation")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s occupation?
    ========================================================================================*/

    .group("XUI_Bails_270_Financial_Supporter_Occupation") {

      exec(http("XUI_Bails_270_005_Financial_Supporter_Occupation")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterOccupation")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterOccupation.json"))
        .check(substring("supporterOccupation")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s immigration status?
    ========================================================================================*/

    .group("XUI_Bails_280_Financial_Immigration_Status") {

      exec(http("XUI_Bails_280_005_Financial_Immigration_Status")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterImmigration")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialImmigrationStatus.json"))
        .check(substring("supporterImmigration")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s nationality?
    ========================================================================================*/

    .group("XUI_Bails_290_Financial_Nationality") {

      exec(http("XUI_Bails_290_005_Financial_Nationality")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterNationality")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialNationality.json"))
        .check(substring("supporterNationality")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Does the financial condition supporter have a passport? - Yes
    ========================================================================================*/

    .group("XUI_Bails_300_Financial_Passport") {

      exec(http("XUI_Bails_300_005_Financial_Passport")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterHasPassport")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialPassport.json"))
        .check(substring("supporterHasPassport")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What is the financial condition supporter’s passport number?
    ========================================================================================*/

    .group("XUI_Bails_310_Financial_Passport_Number") {

      exec(http("XUI_Bails_310_005_Financial_Passport_Number")
        .post("/data/case-types/Bail/validate?pageId=startApplicationsupporterPassport")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialPassportNumber.json"))
        .check(substring("supporterPassport")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What amount does the financial condition supporter undertake to pay?
    ========================================================================================*/

    .group("XUI_Bails_320_Financial_Condition_Amount") {

      exec(http("XUI_Bails_320_005_Financial_Condition_Amount")
        .post("/data/case-types/Bail/validate?pageId=startApplicationfinancialAmountSupporterUndertakes1")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialConditionAmount.json"))
        .check(substring("financialAmountSupporterUndertakes1")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * What amount does the financial condition supporter undertake to pay? - No
    ========================================================================================*/

    .group("XUI_Bails_330_Another_Supporter") {

      exec(http("XUI_Bails_330_005_Another_Supporter")
        .post("/data/case-types/Bail/validate?pageId=startApplicationfinancialConditionSupporter2")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsAnotherSupporter.json"))
        .check(substring("hasFinancialCondSupporter2")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Grounds for bail
    ========================================================================================*/

    .group("XUI_Bails_340_Grounds_For_Bail") {

      exec(http("XUI_Bails_340_005_Grounds_For_Bail")
        .post("/data/case-types/Bail/validate?pageId=startApplicationgroundsForBailInfo")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsGroundsForBail.json"))
        .check(substring("startApplicationgroundsForBailInfo")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * On what grounds is the applicant applying for bail?
    ========================================================================================*/

    .group("XUI_Bails_350_Bail_Grounds") {

      exec(http("XUI_Bails_350_005_Bail_Grounds")
        .post("/data/case-types/Bail/validate?pageId=startApplicationgroundsForBailReasons")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsGrounds.json"))
        .check(substring("groundsForBailReasons")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Do you want to provide supporting evidence? - yes
    ========================================================================================*/

    .group("XUI_Bails_360_Provide_Supporting_Evidence") {

      exec(http("XUI_Bails_360_005_Provide_Supporting_Evidence")
        .post("/data/case-types/Bail/validate?pageId=startApplicationgroundsForBailProvideEvidence")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsProvideSupportingEvidence.json"))
        .check(substring("groundsForBailProvideEvidenceOption")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=======================================================================================
    * Upload evidence
    ========================================================================================*/

    .group("XUI_Bails_370_Upload_Evidence") {

      exec(http("XUI_Bails_370_005_Upload_Evidence")
        .post("/documentsv2")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("X-XSRF-TOKEN", "${XSRFToken}")
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "null")
        .formParam("jurisdictionId", "null")
        .bodyPart(RawFileBodyPart("files", "120KB.pdf")
          .fileName("120KB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(jsonPath("$.documents[0].hashToken").saveAs("DocumentHash"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL")))

    }
      .pause(MinThinkTime, MaxThinkTime)


      /*=======================================================================================
      * Submit evidence
      ========================================================================================*/

      .group("XUI_Bails_375_Submit_Evidence") {

    exec(http("XUI_Bails_375_005_Upload_Evidence")
      .post("/data/case-types/Bail/validate?pageId=startApplicationgroundsForBailEvidenceDocumentUpload")
      .headers(Headers.commonHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "${XSRFToken}")
      .body(ElFileBody("bodies/bails/BailsUploadEvidence.json"))
      .check(substring("document_url")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*=====================================================================================================
    * Does the applicant consent to future management of bail being transferred to the Home Office? - yes
    ======================================================================================================*/

    .group("XUI_Bails_380_Transfer_Bail_Management") {

      exec(http("XUI_Bails_380_Transfer_Bail_Management")
        .post("/data/case-types/Bail/validate?pageId=startApplicationtransferBailManagement")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsTransferBailManagement.json"))
        .check(substring("transferBailManagementYesOrNo")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*==================================================================================================
    * Does the applicant or any financial condition supporters need an interpreter at the hearing? - no
    ====================================================================================================*/

    .group("XUI_Bails_390_Need_Interpreter") {

      exec(http("XUI_Bails_390_005_Need_Interpreter")
        .post("/data/case-types/Bail/validate?pageId=startApplicationinterpreter")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsNeedInterpreter.json"))
        .check(substring("interpreterYesNo")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*==================================================================================================
    * Does the applicant have a disability which may affect them at the hearing? - no
    ====================================================================================================*/

    .group("XUI_Bails_400_Have_Disability") {

      exec(http("XUI_Bails_400_005_Have_Disability")
        .post("/data/case-types/Bail/validate?pageId=startApplicationdisabilityDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsHaveDisability.json"))
        .check(substring("applicantDisability1")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*==================================================================================================
    * Would the applicant be able to join the hearing by video link? - yes
    ====================================================================================================*/

    .group("XUI_Bails_410_Video_Link") {

      exec(http("XUI_Bails_410_005_Video_Link")
        .post("/data/case-types/Bail/validate?pageId=startApplicationapplicantVideoHearingDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsVideoLink.json"))
        .check(substring("videoHearing1")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*==================================================================================================
    * Enter the legal representative’s details
    ====================================================================================================*/

    .group("XUI_Bails_420_Legal_Reps_Details") {

      exec(http("XUI_Bails_420_005_Legal_Reps_Details")
        .post("/data/case-types/Bail/validate?pageId=startApplicationlegalRepresentativeDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsLegalRepsDetails.json"))
        .check(substring("isLegalRep")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*==================================================================================================
    * Check your answers
    ====================================================================================================*/

    .group("XUI_Bails_430_Check_Your_Answers") {

      exec(http("XUI_Bails_430_005_Check_Your_Answers")
        .post(BaseURL + "/data/case-types/Bail/cases?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsCheckYourAnswers.json"))
        .check(jsonPath("$.id").saveAs("caseId"))
        .check(jsonPath("$.callback_response_status").is("CALLBACK_COMPLETED")))

    }
    .pause(MinThinkTime, MaxThinkTime)


  val SubmitBailApplication =

    /*======================================================================================
    * Load the newly created case
    ======================================================================================*/

    group("XUI_Bails_440_Open_Case") {
      exec(http("XUI_Bails_440_005_Open_Case")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Application started")))

    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Click on 'Submit the Application'
    ======================================================================================*/

    .group("XUI_Bails_450_Open_Sumbit_Application") {

      exec(Common.configurationui)

      .exec(Common.configJson)

      .exec(Common.TsAndCs)

      .exec(Common.configUI)

      .exec(Common.userDetails)

      .exec(Common.monitoringTools)

      .exec(Common.isAuthenticated)

      .exec(Common.profile)

      .exec(http("XUI_Bails_450_005_Open_Sumbit_Application")
        .get("/data/internal/cases/${caseId}/event-triggers/submitApplication?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_id").is("${caseId}"))
        .check(substring("access_granted")))

      .exec(Common.isAuthenticated)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * The applicant has confirmed that the facts stated in this application are true.
    ======================================================================================*/

    .group("XUI_Bails_460_Facts_Are_True") {

      exec(http("XUI_Bails_460_005_Facts_Are_True")
        .post("/data/case-types/Bail/validate?pageId=submitApplicationdeclarationOnSubmit")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFactsAreTrue.json"))
        .check(substring("declarationOnSubmit")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit the application
    ======================================================================================*/

    .group("XUI_Bails_470_Submit_The_Application") {

      exec(http("XUI_Bails_470_005_Submit_The_Application")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsSubmitTheApplication.json"))
        .check(jsonPath("$.state").is("applicationSubmitted")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  val UploadBailSummary =

    /*======================================================================================
    * Load case
    ======================================================================================*/

    group("XUI_Bails_480_Open_Case") {
      exec(http("XUI_Bails_480_005_Open_Case")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Application submitted")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Click on 'Upload Bail Summary'
    ======================================================================================*/

    .group("XUI_Bails_490_Upload_Bail_Open") {
      exec(Common.profile)

      .exec(http("XUI_Bails_490_005_Upload_Bail_Open")
        .get("/data/internal/cases/${caseId}/event-triggers/uploadBailSummary?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_id").is("${caseId}"))
        .check(substring("access_granted")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Bail Summary
    ======================================================================================*/

    .group("XUI_Bails_500_Upload_Bail") {

      exec(http("XUI_Bails_500_005_Upload_Bail")
        .post("/documentsv2")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("X-XSRF-TOKEN", "${XSRFToken}")
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "Bail")
        .formParam("jurisdictionId", "IA")
        .bodyPart(RawFileBodyPart("files", "120KB.pdf")
          .fileName("120KB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(jsonPath("$.documents[0].hashToken").saveAs("DocumentHash"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL")))

      .exec(http("XUI_Bails_500_010_Upload_Bail")
        .post("/data/case-types/Bail/validate?pageId=uploadBailSummarybailSummaryDocumentUpload")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsBailUpload.json"))
        .check(substring("uploadBailSummaryDocs")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Bail Summary Submit
    ======================================================================================*/

    .group("XUI_Bails_510_Upload_Bail_Submit") {

      exec(http("XUI_Bails_510_005_Upload_Bail_Submit")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsUploadSubmit.json"))
        .check(jsonPath("$.state").is("bailSummaryUploaded")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  val RecordBailDecision =

    /*======================================================================================
    * Load case
    ======================================================================================*/

    group("XUI_Bails_520_Open_Case") {
      exec(http("XUI_Bails_520_005_Open_Case")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Bail summary")))

    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Click on 'Record the Decision'
    ======================================================================================*/

    .group("XUI_Bails_530_Record_Decision_Open") {
      exec(Common.profile)

      .exec(http("XUI_Bails_530_Record_Decision_Open")
        .get("/data/internal/cases/${caseId}/event-triggers/recordTheDecision?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_id").is("${caseId}"))
        .check(substring("access_granted")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Judge Name
    ======================================================================================*/

    .group("XUI_Bails_540_Judge_Name") {
      exec(http("XUI_Bails_540_005_Judge_Name")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionrecordDecisionJudgeDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsJudgeName.json"))
        .check(substring("judgeDetailsName")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Is Secretary of State consent needed? - no
    ======================================================================================*/

    .group("XUI_Bails_550_Secretary_Consent") {
      exec(http("XUI_Bails_550_005_Secretary_Consent")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionrecordDecisionConsentDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsSecretaryConsent.json"))
        .check(substring("secretaryOfStateConsentYesOrNo")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * What is the Tribunal's decision? - Granted
    ======================================================================================*/

    .group("XUI_Bails_560_Tribunal_Decision") {
      exec(http("XUI_Bails_560_005_Tribunal_Decision")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionrecordDecisionConsentDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsTribunalDecision.json"))
        .check(substring("decisionGrantedOrRefused")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Will the applicant be released with immediate effect? - Yes
    ======================================================================================*/

    .group("XUI_Bails_570_Immediate_Effect") {

      exec(http("XUI_Bails_570_005_Immediate_Effect")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionreleaseStatus")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsImmediateEffect.json"))
        .check(substring("releaseStatusYesOrNo")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select the conditions the applicant will be subject to - Appearance and Activites
    ======================================================================================*/

    .group("XUI_Bails_580_Conditions_For_Applicant") {

      exec(http("XUI_Bails_580_005_Conditions_For_Applicant")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionconditionDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsConditionsForApplicant.json"))
        .check(substring("conditionsForBail")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Will the applicant be subject to a financial condition? - Yes
    ======================================================================================*/

    .group("XUI_Bails_590_Subject_To_Financial") {

      exec(http("XUI_Bails_590_005_Subject_To_Financial")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionrecordFinancialCondition")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsSubjectToFinancial.json"))
        .check(substring("recordFinancialConditionYesOrNo")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Financial condition details - Yes
    ======================================================================================*/

    .group("XUI_Bails_600_Financial_Condition_Details") {

      exec(http("XUI_Bails_600_005_Financial_Condition_Details")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionrecordFinancialConditionDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialConditionDetails.json"))
        .check(substring("recordFinancialConditionCorrectYesOrEdit")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Confirm financial condition amount
    ======================================================================================*/

    .group("XUI_Bails_610_Financial_Condition_Confirm") {

      exec(http("XUI_Bails_610_005_Financial_Condition_Confirm")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionconfirmFinancialConditionAmount")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialConditionConfirm.json"))
        .check(substring("financialCondAmount1")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Did the judge agree to accept financial condition supporter 1? - Yes
    ======================================================================================*/

    .group("XUI_Bails_620_Accept_Financial_Condition") {

      exec(http("XUI_Bails_610_005_Financial_Condition_Confirm")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionfinancialConditionSupporter1JudgeAgree")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsAcceptFinancialCondition.json"))
        .check(substring("judgeHasAgreedToSupporter1")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Financial condition supporter details
    ======================================================================================*/

    .group("XUI_Bails_630_Financial_Supporter_Details") {

      exec(http("XUI_Bails_630_005_Financial_Supporter_Details")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionfinancialConditionSupporter1Confirmation")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsFinancialSupporterDetails.json"))
        .check(substring("financialAmountSupporterUndertakes1")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*================================================================================================
    * Will the future management of bail for this applicant transfer to the Secretary of State? - Yes
    ================================================================================================*/

    .group("XUI_Bails_640_Transfer_To_Secretary") {

      exec(http("XUI_Bails_640_005_Transfer_To_Secretary")
        .post("/data/case-types/Bail/validate?pageId=recordTheDecisionrecordTheDecisionBailTransfer")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsTransferToSecretary.json"))
        .check(substring("bailTransferDirections")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check your answers
    ======================================================================================*/

    .group("XUI_Bails_650_Check_Your_Answers") {

      exec(http("XUI_Bails_650_005_Check_Your_Answers")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsJudgeCheckAnswers.json"))
        .check(jsonPath("$.state").is("unsignedDecision")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  val UploadSignedDecision =

    /*======================================================================================
    * Load case
    ======================================================================================*/

    group("XUI_Bails_660_Open_Case") {
      exec(http("XUI_Bails_660_005_Open_Case")
        .get("/data/internal/cases/${caseId}")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
        .check(jsonPath("$.state.name").is("Unsigned decision")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Signed Decision
    ======================================================================================*/

    .group("XUI_Bails_670_Signed_Decision_Open") {
      exec(Common.profile)

      .exec(http("XUI_Bails_670_005_Signed_Decision_Open")
        .get("/data/internal/cases/${caseId}/event-triggers/uploadSignedDecisionNotice?ignore-warning=false")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_id").is("${caseId}"))
        .check(substring("access_granted")))

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload Decision Notice
    ======================================================================================*/

    .group("XUI_Bails_680_Upload_Decision_Notice") {

      exec(http("XUI_Bails_680_005_Upload_Decision_Notice")
        .post("/documentsv2")
        .headers(Headers.commonHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("X-XSRF-TOKEN", "${XSRFToken}")
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "Bail")
        .formParam("jurisdictionId", "IA")
        .bodyPart(RawFileBodyPart("files", "120KB.pdf")
          .fileName("120KB.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .check(jsonPath("$.documents[0].hashToken").saveAs("DocumentHash"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Submit the Uploaded Decision Notice
    ======================================================================================*/

    .group("XUI_Bails_690_Upload_Decision_Notice") {

      exec(http("XUI_Bails_690_005_Upload_Decision_Notice")
        .post("/data/case-types/Bail/validate?pageId=uploadSignedDecisionNoticesignedDecisionNoticeUpload")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsUploadDecisionNotice.json"))
        .check(substring("uploadSignedDecisionNoticeDocument")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Upload signed decision notice Submit
    ======================================================================================*/

    .group("XUI_Bails_700_Upload_Signed_Notice_Submit") {

      exec(http("XUI_Bails_700_Upload_Signed_Notice_Submit")
        .post("/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/bails/BailsUploadSignedNoticeSubmit.json"))
        .check(jsonPath("$.after_submit_callback_response.confirmation_header").is("# You uploaded the signed decision notice")))
    }

    .pause(MinThinkTime, MaxThinkTime)
    
}