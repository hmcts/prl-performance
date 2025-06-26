package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

/*======================================================================================
* Navigate through application guidance to be directed into CUI Login
======================================================================================*/

object Citizen_PRL_ApplicationGuidance {
  
  val prlURL = Environment.prlURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CompleteApplicationGuidance =

    /*======================================================================================
    * complete-your-application-guidance Landing Page
    ======================================================================================*/

    exec(http("PRL_Citizen_010_CompleteApplicationGuidanceLanding")
        .get(prlURL + "/complete-your-application-guidance")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(CsrfCheck.save)
        .check(substring("Completing your application")))

    .pause(MinThinkTime, MaxThinkTime)

    /*===============================================================================================
    * Click Continue 
    ===============================================================================================*/

    .group("PRL_Citizen_020_CompleteApplicationGuidanceContinue") {
      exec(http("PRL_Citizen_020_005_CompleteApplicationGuidanceContinue")
        .post(prlURL + "/complete-your-application-guidance")
        .headers(Headers.navigationHeader)
        .header("content-type", "application/x-www-form-urlencoded")
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .formParam("_csrf", "#{csrf}")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you agree to pay the court fee online")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you agree to pay the court fee online using a debit or credit card? --> Yes --> Continue
    ======================================================================================*/

    .group("PRL_Citizen_030_AgreeCourtFeeContinue") {
      exec(http("PRL_Citizen_030_005_AgreeCourtFeeContinue")
        .post(prlURL + "/agree-court-fee")
        .headers(Headers.navigationHeader)
        .header("content-type", "application/x-www-form-urlencoded")
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .formParam("_csrf", "#{csrf}")
        .formParam("applicationPayOnline", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Will you be using a legal representative")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Will you be using a legal representative in these proceedings? -> Yes --> Continue
    ======================================================================================*/

    .group("PRL_Citizen_040_UsingLegalRepContinue") {
      exec(http("PRL_Citizen_040_005_UsingLegalRepContinue")
        .post(prlURL + "/legal-representative-proceedings")
        .headers(Headers.navigationHeader)
        .header("content-type", "application/x-www-form-urlencoded")
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .formParam("_csrf", "#{csrf}")
        .formParam("legalRepresentativeForProceedings", "Yes")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you want your legal representative to complete the application for you")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Do you want your legal representative to complete the application for you? -> No --> Continue
    ======================================================================================*/

    .group("PRL_Citizen_050_CompleteApplicationLegalRepContinue") {
      exec(http("PRL_Citizen_050_005_CompleteApplicationLegalRepContinue")
        .post(prlURL + "/complete-your-application-legal-representative")
        .headers(Headers.navigationHeader)
        .header("content-type", "application/x-www-form-urlencoded")
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .formParam("_csrf", "#{csrf}")
        .formParam("legalRepresentativeForApplication", "No")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Sign in or create an account")))
    }

    .pause(MinThinkTime, MaxThinkTime)
  
    }
