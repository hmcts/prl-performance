package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}

/*======================================================================================
* As a Citizen Respond to the c100 Case and make Reasonable Adjustments through CUI.
======================================================================================*/

object PRL_C100_Citizen_CUI_RA {
  
  val BaseURL = Environment.baseURL
  val prlURL = Environment.prlURL
  val IdamUrl = Environment.idamURL
  val PRLCitizens = csv("UserDataPRLCUIRACitizen.csv").circular


  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime


  val CUIRA =

    /*======================================================================================
    * Citizen Home
    ======================================================================================*/

      group("PRL_CitizenC100_010_PRLHome") {

        exec(_.setAll(
          "PRLRandomString" -> (Common.randomString(7)),
          "PRLRandomPhone" -> (Common.randomNumber(8)),
          "PRLAppDobDay" -> Common.getDay(),
          "PRLAppDobMonth" -> Common.getMonth(),
          "PRLAppDobYear" -> Common.getDobYear(),
          "PRLChildDobYear" -> Common.getDobYearChild()))

          .feed(PRLCitizens)

          .exec(http("PRL_CitizenC100_010_005_PRLHome")
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
     * Select an Active PRL case
     ======================================================================================*/
    .group("PRL_CitizenC100_CUIRA_030_SelectCase") {

      exec(http("PRL_CitizenC100_CUIRA_030_SelectCase")
        .get(prlURL + "/case/1713369453249596")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        //.check(substring("Respond to an application about a child")))
        .check(substring("Child arrangements - Child arrangements and family injunction cases - Private law - GOV.UK")))
       }
    .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
    * Select an Active PRL case Part 2 - Check with Jon M if this is actually needed
    ======================================================================================*/
        .group("PRL_CitizenC100_CUIRA_030_005_SelectCase") {

          exec(http("PRL_CitizenC100_CUIRA_030_005_SelectCase")
            .get(prlURL + "/task-list/respondent")
            .headers(Headers.navigationHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .check(substring(" Respond to an application about a child")))
        }
        .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Tell Us If you Need Support - https://privatelaw.perftest.platform.hmcts.net/respondent/reasonable-adjustments/intro
    ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_040_RAIntro") {

          exec(http("PRL_CitizenC100_CUIRA_040_RAIntro")
            .get(prlURL + "/respondent/reasonable-adjustments/intro")
            .headers(Headers.navigationHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .check(substring("Support before a court hearing")))

        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
        * RA Intro Post Request
        ======================================================================================*/
        .group("PRL_CitizenC100_CUIRA_050_RAIntro") {

          feed(postcodeFeeder)

            .exec(http("PRL_CitizenC100_CUIRA_050_RAIntro")
              .post(prlURL + "/respondent/reasonable-adjustments/intro")
              .headers(Headers.commonHeader)
              .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
              .header("content-type", "application/x-www-form-urlencoded")
              .formParam("_csrf", "#{csrf}")
              .formParam("onlyContinue", "true")
              .check(substring("language-requirements-and-special-arrangements")))


        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
* language-requirements-and-special-arrangements Post Request
======================================================================================*/
        .group("PRL_CitizenC100_CUIRA_060_RAIntro") {

          feed(postcodeFeeder)

            .exec(http("PRL_CitizenC100_CUIRA_060_RAIntro")
              .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
              .headers(Headers.commonHeader)
              .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
              .header("content-type", "application/x-www-form-urlencoded")
              .formParam("_csrf", "#{csrf}")
              .formParam("ra_languageReqAndSpecialArrangements", "CUI RA PerfTest Journey")
              .formParam("onlyContinue", "true")
              .check(substring("Review your language requirements and special arrangements")))

        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
      Review your language requirements and special arrangements
    Need to validate substring
          ======================================================================================*/
        .group("PRL_CitizenC100_CUIRA_070_RAIntro") {

            exec(http("PRL_CitizenC100_CUIRA_070_RAIntro")
              .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements/review")
              .headers(Headers.commonHeader)
              .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
              .header("content-type", "application/x-www-form-urlencoded")
              .formParam("_csrf", "#{csrf}")
              .formParam("onlyContinue", "true")
              .check(substring("Do you have a physical, mental or learning disability or health condition that means you need support during your case?")))

        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
   * Launch CUIRA  and Capture session ID
   ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_080_Launch_CUIRA") {

          exec(http("PRL_CitizenC100_CUIRA_080_Launch_CUIRA")
            .get(prlURL + "/reasonable-adjustments/launch")
            .headers(Headers.navigationHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .check(substring("Do you have a physical, mental or learning disability or health condition that means you need support during your case?")))

        }

//DG Need to capture this here Location:
        //https://cui-ra.perftest.platform.hmcts.net/dc/p/3a9e2899-6b55-4c3e-86df-93037f735986

        /*======================================================================================
   * Launch CUIRA  PART 2
   ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_090_Launch CUIRA") {

          exec(http("PRL_CitizenC100_CUIRA_090_Launch CUIRA")
            .get( "https://cui-ra.perftest.platform.hmcts.net/dc/p/3a9e2899-6b55-4c3e-86df-93037f735986")
            .headers(Headers.navigationHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .check(substring("Do you have a physical, mental or learning disability or health condition that means you need support during your case?")))

        }

        /*======================================================================================
   * Launch CUIRA  PART 3 - https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001
   ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_100_Launch CUIRA") {

          exec(http("PRL_CitizenC100_CUIRA_100_Launch CUIRA")
            .get( "https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001")
            .headers(Headers.navigationHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .check(substring("Do you have a physical, mental or learning disability or health condition that means you need support during your case?")))

        }

        /*======================================================================================
   * Do you have a physical, mental or learning disability or health condition that means you need support during your case?
   ======================================================================================*/
        .group("PRL_CitizenC100_CUIRA_110_SupportRequired") {

          exec(http("PRL_CitizenC100_CUIRA_110_SupportRequired")
            .post("https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001")
            .headers(Headers.commonHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .header("content-type", "application/x-www-form-urlencoded")
            .formParam("enabled", "PF0001-RA0001-RA0004")
            .formParam("enabled", "PF0001-RA0001-RA0008")
            .formParam("enabled", "PF0001-RA0001-RA0006")
            .formParam("_csrf", "#{csrf}")
            .check(substring("I need adjustments to get to, into and around our buildings")))

        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
       * I need adjustments to get to, into and around our buildings
       ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_120_BuildingAdjustment") {

          exec(http("PRL_CitizenC100_CUIRA_120_BuildingAdjustment")
            .post("https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001-RA0004")
            .headers(Headers.commonHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .header("content-type", "application/x-www-form-urlencoded")
            .formParam("enabled", "PF0001-RA0001-RA0004-RA0024")
            .formParam("enabled", "PF0001-RA0001-RA0004-RA0022")
            .formParam("enabled", "PF0001-RA0001-RA0004-RA0025")
            .formParam("data[PF0001-RA0001-RA0004-RA0021][flagComment]", "")
            .formParam("data[PF0001-RA0001-RA0004-OT0001][flagComment]", "")
            .formParam("_csrf", "#{csrf}")
            .check(substring("I need help communicating and understanding")))
        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
       * I need help communicating and understanding"
       ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_130_CommunicatingandUnderstanding") {

          exec(http("PRL_CitizenC100_CUIRA_130_CommunicatingandUnderstanding")
            .post("https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001-RA0008")
            .headers(Headers.commonHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .header("content-type", "application/x-www-form-urlencoded")
            .formParam("enabled", "PF0001-RA0001-RA0008-RA0047")
            .formParam("enabled", "PF0001-RA0001-RA0008-RA0037")
            .formParam("enabled", "PF0001-RA0001-RA0008-RA0009")
            .formParam("data[PF0001-RA0001-RA0008-OT0001][flagComment]", "")
            .formParam("_csrf", "#{csrf}")
            .check(substring("Hearing Enhancement System (Hearing/Induction Loop, Infrared Receiver")))
        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
     * Hearing Enhancement System (Hearing/Induction Loop, Infrared Receiver
     ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_140_CommunicatingandUnderstanding") {

          exec(http("PRL_CitizenC100_CUIRA_140_CommunicatingandUnderstanding")
            .post("https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001-RA0008-RA0009")
            .headers(Headers.commonHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .header("content-type", "application/x-www-form-urlencoded")
            .formParam("enabled", "PF0001-RA0001-RA0008-RA0009-RA0043")
            .formParam("enabled", "PF0001-RA0001-RA0008-RA0009-RA0045")
            .formParam("data[PF0001-RA0001-RA0008-RA0009-OT0001][flagComment]", "")
            .formParam("_csrf", "#{csrf}")
            .check(substring("I need something to feel comfortable during my hearing")))
        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
          * I need something to feel comfortable during my hearing
          ======================================================================================*/

        .group("PRL_CitizenC100_CUIRA_150_ComfortableDuringHearing") {

          exec(http("PRL_CitizenC100_CUIRA_150_CommunicatingandUnderstanding")
            .post("https://cui-ra.perftest.platform.hmcts.net/journey/flags/display/PF0001-RA0001-RA0006")
            .headers(Headers.commonHeader)
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .header("content-type", "application/x-www-form-urlencoded")
            .formParam("enabled", "PF0001-RA0001-RA0006-RA0030")
            .formParam("data[PF0001-RA0001-RA0006-OT0001][flagComment]", "")
            .formParam("_csrf", "#{csrf}")
            .check(substring("Review the support you've requested")))
        }
        .pause(MinThinkTime, MaxThinkTime)

        /*======================================================================================
         * DG NEED TO ADD THE FINAL STEP
         ======================================================================================*/







}