package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Citizen_PRL_C100_Respondent {
  
  val PayURL = Environment.payURL
  val prlURL = Environment.prlURL
  val IdamUrl = Environment.idamURL
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular
  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

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

    .group("PRL_CitizenC100_610_DescribePyshicalAbuse") {
      exec(http("PRL_CitizenC100_610_005_DescribePyshicalAbuse")
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
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
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
        .formParam("cardholderName", "John Smith")
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
      val fw = new BufferedWriter(new FileWriter("caseNumber.csv", true))
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