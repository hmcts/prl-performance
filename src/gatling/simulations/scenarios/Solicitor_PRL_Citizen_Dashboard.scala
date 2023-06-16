package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_Citizen_Dashboard {
  
  val BaseURL = Environment.baseURL
  val prlURL = Environment.prlURL
  val IdamUrl = Environment.idamURL
  //val PRLcases = csv("cases.csv").circular
  val PRLAccessCode = csv("accessCodeList.csv").circular
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular


  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime


  val DashBoard =

    /*======================================================================================
    * Citizen Home
    ======================================================================================*/

      group("PRL_Citizen_010_PRLHome") {

        exec(_.setAll(
          "PRLRandomString" -> (Common.randomString(7)),
          "PRLRandomPhone" -> (Common.randomNumber(8)),
          "PRLAppDobDay" -> Common.getDay(),
          "PRLAppDobMonth" -> Common.getMonth(),
          "PRLAppDobYear" -> Common.getDobYear(),
          "PRLChildDobYear" -> Common.getDobYearChild()))

          .feed(PRLCitizens)
          .feed(PRLAccessCode)

          .exec(http("PRL_Citizen_010_005_PRLHome")
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

    .group("PRL_Citizen_020_Login") {
      exec(http("PRL_Citizen_020_005_Login")
        .post(IdamUrl + "/login?client_id=prl-citizen-frontend&response_type=code&redirect_uri=" + prlURL + "/receiver")
     //   .post("https://idam-web-public.aat.platform.hmcts.net/login?client_id=prl-citizen-frontend&response_type=code&redirect_uri=https://prl-citizen-frontend-pr-741.service.core-compute-preview.internal/receiver")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("username", "${user}")
        .formParam("password", "${password}")
        .formParam("save", "Sign in")
        .formParam("selfRegistrationEnabled", "true")
        .formParam("_csrf", "${csrf}")
        .check(substring("Child arrangements and family injunction cases")))
    }
    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * New Child Arrangements Application C100
    ======================================================================================*/

    .group("PRL_Citizen_030_C100StartApplication") {

      exec(http("PRL_Citizen_030_005_C100StartApplication")
          .get(prlURL + "/task-list/applicant")
          .headers(Headers.navigationHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .check(substring("You have not started your application")))

    }
    .pause(MinThinkTime, MaxThinkTime)



    /*======================================================================================
    * Start The Application C100
    ======================================================================================*/

    .group("PRL_Citizen_040_StartTheApplicationC100") {

      exec(http("PRL_Citizen_040_005_StartTheApplicationC100")
        .get(prlURL + "/c100-rebuild/start")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
      //  .check(CsrfCheck.save)
        .check(substring("What you’ll need to complete your application")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Dashboard
    ======================================================================================*/

    .group("PRL_Citizen_041_Dashboard") {

      exec(http("PRL_Citizen_041_005_Dashboard")
        .get(prlURL)
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .check(substring("Child arrangements and family injunction cases")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Activate Access Code
    ======================================================================================*/

    .group("PRL_Citizen_042_ActivateAccessCode") {

      exec(http("PRL_Citizen_042_005_ActivateAccessCode")
        .get(prlURL + "/pin-activation/enter-pin")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .check(CsrfCheck.save)
        .check(substring("Access your case")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * CaseAdded
    ======================================================================================*/

    .group("PRL_Citizen_043_CaseAdded") {

        exec(http("PRL_Citizen_043_005_AccessYourCase")
          .post(prlURL + "/pin-activation/enter-pin")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "${csrf}")
          .formParam("caseCode", "${caseId}")
          .formParam("accessCode", "${accessCode}")
          .formParam("accessCodeCheck", "true")
          .check(substring("Case added to your account")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Access Code Continue
    ======================================================================================*/

    .group("PRL_Citizen_044_AccessCodeContinue") {

        exec(http("PRL_Citizen_044_005_AccessCodeContinue")
          .post(prlURL + "/pin-activation/case-activated")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "${csrf}")
          .formParam("saveAndContinue", "true")
          .check(substring("You have a new order from the court")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
    * Access Case
    ======================================================================================*/

    .group("PRL_Citizen_045_AccessCase") {

        exec(http("PRL_Citizen_045_005_AccessCase")
          .get(prlURL + "/c100-rebuild/case/${caseToTest}/retrive")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .header("content-type", "application/x-www-form-urlencoded")
          .check(substring("Your application")))

    }
    .pause(MinThinkTime, MaxThinkTime)








}