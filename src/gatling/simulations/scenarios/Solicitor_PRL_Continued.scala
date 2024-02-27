package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers, CsrfCheck}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_Continued {
  
  val BaseURL = Environment.baseURL
  val prlURL = "https://privatelaw.#{env}.platform.hmcts.net"
  val IdamUrl = Environment.idamURL
  val PRLAccessCode = csv("accessCodeList.csv").circular
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular

  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime


  val PRL =

  /*======================================================================================
* Citizen Home
======================================================================================*/

    group("PRL_Citizen_250_PRLHome") {

      exec(_.setAll(
        "PRLRandomString" -> (Common.randomString(7)),
        "PRLAppDobDay" -> Common.getDay(),
        "PRLAppDobMonth" -> Common.getMonth(),
        "PRLAppDobYear" -> Common.getDobYear()))

        .feed(PRLAccessCode)
        .feed(PRLCitizens)

        .exec(http("PRL_Citizen_250_005_PRLHome")
          .get(prlURL + "/citizen-home")
          .headers(Headers.navigationHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .check(CsrfCheck.save)
          .check(substring("Enter your access details")))
    }

      .pause(MinThinkTime, MaxThinkTime)


      /*===============================================================================================
* Enter Your Access Details
===============================================================================================*/

      .group("PRL_Citizen_260_AccessDetails") {
        exec(http("PRL_Citizen_260_005_AccessDetails")
          .post(prlURL + "/citizen-home")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("caseCode", "#{caseId}")
          .formParam("accessCode", "#{accessCode}")
          .formParam("accessCodeCheck", "true")
          .check(CsrfCheck.save)
          .check(substring("Sign in or create an account")))
      }
      .pause(MinThinkTime, MaxThinkTime)


      /*===============================================================================================
* Login
===============================================================================================*/

      .group("PRL_Citizen_270_Login") {
        exec(http("XUI_PRL_243_005_PRLLogin")
          .post(IdamUrl + "/login?client_id=prl-citizen-frontend&response_type=code&redirect_uri=" + prlURL + "/receiver")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("username", "#{user}")
          .formParam("password", "#{password}")
          .formParam("save", "Sign in")
          .formParam("selfRegistrationEnabled", "true")
          .formParam("_csrf", "#{csrf}")
          .check(substring("Your private law account")))
      }
      .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Select Case
======================================================================================*/

  .group("PRL_Citizen_280_SelectCase") {

    exec(http("PRL_Citizen_280_005_SelectCase")
        .get(prlURL + "/respondent/task-list/#{caseId}")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(substring("You have a new order from the court")))

  }

    .pause(MinThinkTime, MaxThinkTime)



  /*======================================================================================
* Select 'Check the application(PDF)'
======================================================================================*/

  .group("PRL_Citizen_290_ApplicationPDFDownload") {

    exec(http("PRL_Citizen_290_005_ApplicationPDFDownload")
      .get(prlURL + "/yourdocuments/alldocuments/cadafinaldocumentrequest?updateCase=Yes")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"))
     //need to find what to check for .check(substring("Your current postcode")))

  }

    .pause(MinThinkTime, MaxThinkTime)



  /*======================================================================================
* Select 'Respond to the allegations of harm and violence'
======================================================================================*/

  .group("PRL_Citizen_300_AllegationsOfHarm") {

    exec(http("PRL_Citizen_300_005_AllegationsOfHarm")
      .get(prlURL + "/tasklistresponse/international-factors/start")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Do the children live outside of England or Wales?")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Do the children live outside of England or Wales? - No
======================================================================================*/

  .group("PRL_Citizen_310_OutsideOfEngland") {

    exec(http("PRL_Citizen_310_005_OutsideOfEngland")
      .post(prlURL + "/tasklistresponse/international-factors/start")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("iFactorsStartProvideDetails", "")
      .formParam("start", "No")
      .formParam("saveAndContinue", "true")
      .check(substring("Do the children&#39;s parents or anyone significant to the children live outside of England or Wales?")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Do the children's parents or anyone significant to the children live outside of England or Wales? - No
======================================================================================*/

  .group("PRL_Citizen_320_ParentsOutsideOfEngland") {

    exec(http("PRL_Citizen_320_005_ParentsOutsideOfEngland")
      .post(prlURL + "/tasklistresponse/international-factors/parents")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("iFactorsParentsProvideDetails", "")
      .formParam("parents", "No")
      .formParam("saveAndContinue", "true")
      .check(substring("Could another person in the application apply for a similar order in a country outside England or Wales?")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Could another person in the application apply for a similar order in a country outside England or Wales? - No
======================================================================================*/

  .group("PRL_Citizen_330_PersonOutsideOfEngland") {

    exec(http("PRL_Citizen_330_005_PersonOutsideOfEngland")
      .post(prlURL + "/tasklistresponse/international-factors/jurisdiction")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("iFactorsJurisdictionProvideDetails", "")
      .formParam("jurisdiction", "No")
      .formParam("saveAndContinue", "true")
      .check(substring("Has another country asked (or been asked) for information or help for the children?")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Has another country asked (or been asked) for information or help for the children?
======================================================================================*/

  .group("PRL_Citizen_340_AnotherCountryAsked") {

    exec(http("PRL_Citizen_340_005_AnotherCountryAsked")
      .post(prlURL + "/tasklistresponse/international-factors/request")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("iFactorsRequestProvideDetails", "")
      .formParam("request", "No")
      .formParam("saveAndContinue", "true")
      .check(substring("International elements")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Respond to the allegations of harm and violence Check Your Answers
======================================================================================*/

  .group("PRL_Citizen_350_RespondToTheAllegationsCheck") {

    exec(http("PRL_Citizen_350_005_RespondToTheAllegationsCheck")
      .post(prlURL + "/tasklistresponse/international-factors/summary")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("saveAndContinue", "true")
      .check(substring("Respond to the application")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Select 'Respond to the application'
======================================================================================*/

  .group("PRL_Citizen_360_RespondToApplication") {

    exec(http("PRL_Citizen_360_005_RespondToApplication")
      .get(prlURL + "/tasklistresponse/start")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Legal representation")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Select 'Do you have a legal representative?'
======================================================================================*/

  .group("PRL_Citizen_370_LegalRepresentative") {

    exec(http("PRL_Citizen_370_005_LegalRepresentative")
      .get(prlURL + "/tasklistresponse/legalrepresentation/start")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Will you be using a legal representative to respond to the application?")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Will you be using a legal representative to respond to the application? - No
======================================================================================*/

  .group("PRL_Citizen_380_UseLegalRepresentative") {

    exec(http("PRL_Citizen_380_005_UseLegalRepresentative")
      .post(prlURL + "/tasklistresponse/legalrepresentation/start")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("legalRepresentation", "No")
      .check(substring("Complete your response")))

  }

    .pause(MinThinkTime, MaxThinkTime)



  /*======================================================================================
* Transfer your case to your legal representative
======================================================================================*/

  .group("PRL_Citizen_390_TransferRepresentative") {

    exec(http("PRL_Citizen_390_005_TransferRepresentative")
      .post(prlURL + "/redirect/tasklistresponse")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .check(substring("""id="do_you_have_legal_representation-status" class="govuk-tag app-task-list__tag govuk-tag--green">Completed""")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Do you consent to the application?
======================================================================================*/

  .group("PRL_Citizen_400_ConsentToApplication") {

    exec(http("PRL_Citizen_400_005_ConsentToApplication")
      .get(prlURL + "/tasklistresponse/consent-to-application/consent/#{caseId}")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Your understanding of the application")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Your understanding of the application
======================================================================================*/

  .group("PRL_Citizen_410_UnderstandingOfApplication") {

    exec(http("PRL_Citizen_410_005_UnderstandingOfApplication")
      .post(prlURL + "/tasklistresponse/consent-to-application/consent")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("doYouConsent", "Yes")
      .formParam("reasonForNotConsenting", "")
      .formParam("applicationReceivedDate-day", "#{PRLAppDobDay}")
      .formParam("applicationReceivedDate-month", "#{PRLAppDobMonth}")
      .formParam("applicationReceivedDate-year", Common.getDobYearChild())
      .formParam("courtOrderDetails", "")
      .formParam("courtPermission", "No")
      .formParam("onlyContinue", "true")
      .check(substring("Your consent to the application")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Your consent to the application submit
======================================================================================*/

  .group("PRL_Citizen_420_ConsentSubmit") {

    exec(http("PRL_Citizen_420_005_ConsentSubmit")
      .post(prlURL + "/tasklistresponse/consent-to-application/summary")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("saveAndContinue", "true")
      .check(substring("""id="consent-to-the-application-status" class="govuk-tag app-task-list__tag govuk-tag--green">Completed""")))

  }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
* Select 'Keep your details private'
======================================================================================*/

    .group("PRL_Citizen_430_DetailsPrivate") {
      exec(http("PRL_Citizen_430_005_DetailsPrivate")
        .get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .check(CsrfCheck.save)
        .check(substring("Do the other people named in this application (the applicants) know any of your contact details?")))

        .exec(Common.userDetails)

      //   .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Do the other people named in this application (the applicants) know any of your contact details? - Yes
===============================================================================================*/

    .group("PRL_Citizen_440_KnowContactDetails") {
      exec(http("PRL_Citizen_440_005_KnowContactDetails")
        .post(prlURL + "/respondent/keep-details-private/details_known")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("detailsKnown", "yes")
        .formParam("onlyContinue", "true")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Do you want to keep your contact details private from the other people named in the application (the applicants)?")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Do you want to keep your contact details private from the other people named in the application (the applicants)? - Yes
===============================================================================================*/

    .group("PRL_Citizen_450_ContactDetailsPrivate") {
      exec(http("PRL_Citizen_450_005_ContactDetailsPrivate")
        .post(prlURL + "/respondent/keep-details-private/start_alternative")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("contactDetailsPrivate", "")
        .formParam("contactDetailsPrivate", "")
        .formParam("contactDetailsPrivate", "")
        .formParam("startAlternative", "No")
        .formParam("saveAndContinue", "true")
        .check(substring("The court will not keep your contact details private")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* What the court will do
===============================================================================================*/

    .group("PRL_Citizen_460_CourtWillDo") {
      exec(http("PRL_Citizen_460_005_CourtWillDo")
        .post(prlURL + "/respondent/keep-details-private/private_details_confirmed")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("saveAndContinue", "true")
        .check(substring("""id="keep-your-details-private-status" class="govuk-tag app-task-list__tag govuk-tag--green">Completed""")))
    }
    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Select 'Confirm or edit your contact details'
======================================================================================*/

  .group("PRL_Citizen_470_ContactDetails") {

    exec(http("PRL_Citizen_470_005_ContactDetails")
      .get(prlURL + "/respondent/confirm-contact-details/checkanswers/{caseId}")
      .headers(Headers.navigationHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .check(substring("Name")))
  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Select 'Name'
======================================================================================*/

  .group("PRL_Citizen_480_Name") {

    exec(http("PRL_Citizen_480_005_Name")
      .get(prlURL + "/respondent/confirm-contact-details/personaldetails")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Your name and date of birth")))

      .exec(Common.userDetails)

    //   .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
  }

    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
  * Enter Name, DoB and Address
  ===============================================================================================*/

    .group("PRL_Citizen_490_EnterName") {
      exec(http("PRL_Citizen_490_005_EnterName")
        .post(prlURL + "/respondent/confirm-contact-details/personaldetails")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("citizenUserFirstNames", "#{PRLRandomString} First")
        .formParam("citizenUserLastNames", "#{PRLRandomString} Last")
        .formParam("previousName", "")
        .formParam("citizenUserDateOfBirth-day", "#{PRLAppDobDay}")
        .formParam("citizenUserDateOfBirth-month", "#{PRLAppDobMonth}")
        .formParam("citizenUserDateOfBirth-year", "#{PRLAppDobYear}")
        .formParam("citizenUserPlaceOfBirth", "#{PRLRandomString} Address")
        .formParam("saveAndContinue", "true")
        .check(CsrfCheck.save)
        .check(substring("Read the information to make sure it is correct, and add any missing details")))
    }
    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Select 'Address'
======================================================================================*/

  .group("PRL_Citizen_500_Address") {

    exec(http("PRL_Citizen_500_005_Address")
      .get(prlURL + "/respondent/confirm-contact-details/address/lookup")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Your current postcode")))

      .exec(Common.userDetails)

    //   .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
  }

    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
  * Enter PostCode
  ===============================================================================================*/

    .group("PRL_Citizen_510_PostCode") {
      feed(postcodeFeeder)

        .exec(http("PRL_Citizen_510_005_PostCode")
          .post(prlURL + "/respondent/confirm-contact-details/address/lookup")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
          .header("content-type", "application/x-www-form-urlencoded")
          .formParam("_csrf", "#{csrf}")
          .formParam("citizenUserAddressPostcode", "#{postcode}")
          .formParam("saveAndContinue", "true")
          .check(regex("""<option value="([0-9]+)">""").findRandom.saveAs("addressIndex")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Select Address
===============================================================================================*/

    .group("PRL_Citizen_520_SelectAddress") {

      exec(http("PRL_Citizen_520_005_SelectAddress")
        .post(prlURL + "/respondent/confirm-contact-details/address/select")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("citizenUserSelectAddress", "{#addressIndex}")
        .formParam("saveAndContinue", "true")
        .check(regex("""name="citizenUserAddress1" type="text" value="(.+)""").saveAs("address"))
        .check(regex("""name="citizenUserAddressTown" type="text" value="(.+)""").saveAs("town"))
      //  .check(regex("""name="citizenUserAddressCounty" type="text" value="(.+)"""").saveAs("addressLines(2)"))
        .check(substring("Building and street")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Your Address
===============================================================================================*/

    .group("PRL_Citizen_530_YourAddress") {

      exec(http("PRL_Citizen_530_005_YourAddress")
        .post(prlURL + "/respondent/confirm-contact-details/addressconfirmation")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("citizenUserAddress1", "#{address}")
        .formParam("citizenUserAddress2", "")
        .formParam("citizenUserAddressTown", "#{town}")
        .formParam("citizenUserAddressCounty", "")
        .formParam("citizenUserAddressPostcode", "#{postcode}")
        .formParam("saveAndContinue", "true")
        .check(substring("Have you lived at this address for more than 5 years?")))

    }
    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Have you lived at this address for more than 5 years? - Yes
===============================================================================================*/

    .group("PRL_Citizen_540_LivedAtAddress") {

      exec(http("PRL_Citizen_540_005_LivedAtAddress")
        .post(prlURL + "/respondent/confirm-contact-details/addresshistory")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("isAtAddressLessThan5Years", "Yes")
        .formParam("citizenUserAddressHistory", "")
        .formParam("saveAndContinue", "true")
        .check(substring("Check your details")))
    }
    .pause(MinThinkTime, MaxThinkTime)


  /*===============================================================================================
* Select 'Phone Number'
===============================================================================================*/

  .group("PRL_Citizen_550_PhoneNumber") {

    exec(http("PRL_Citizen_550_005_PhoneNumber")
      .get(prlURL + "/respondent/confirm-contact-details/contactdetails")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("UK telephone number")))

      .exec(Common.userDetails)

    //   .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
  }

    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Contact Details
===============================================================================================*/

    .group("PRL_Citizen_560_ContactDetails") {

      exec(http("PRL_Citizen_560_005_ContactDetails")
        .post(prlURL + "/respondent/confirm-contact-details/contactdetails")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("citizenUserPhoneNumber", "07000000000")
        .formParam("citizenUserEmailAddress", "#{PRLRandomString}@gmail.com")
        .formParam("citizenUserSafeToCall", "#{PRLRandomString} Call")
        .formParam("saveAndContinue", "true")
        .check(substring("Check your details")))
    }
    .pause(MinThinkTime, MaxThinkTime)


    /*===============================================================================================
* Save your Details
===============================================================================================*/

//    .group("XUI_PRL_360_DetailsSubmit") {

  //    exec(http("XUI_PRL_360_005_DetailsSubmit")
   //     .post(prlURL + "/respondent/confirm-contact-details/checkanswers")
   //     .headers(Headers.commonHeader)
   //     .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
   //     .header("content-type", "application/x-www-form-urlencoded")
   //     .formParam("_csrf", "#{csrf}")
   //     .formParam("saveAndContinue", "true")
   //     .check(substring("""id="confirm-or-edit-your-contact-details-status" class="govuk-tag app-task-list__tag govuk-tag--green">Completed""")))
   // }
   // .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Miam
======================================================================================*/

  .group("PRL_Citizen_570_Miam") {

    exec(http("PRL_Citizen_570_005_Miam")
      .get(prlURL + "/tasklistresponse/miam/miam-start/#{caseId}")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Have you attended a Mediation Information and Assessment Meeting (MIAM)?")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Have you attended a Mediation Information and Assessment Meeting (MIAM)? - yes
======================================================================================*/

  .group("PRL_Citizen_580_AttendedMiam") {

    exec(http("PRL_Citizen_580_005_AttendedMiam")
      .post(prlURL + "/tasklistresponse/miam/miam-start")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("miamStart", "Yes")
      .formParam("onlyContinue", "true")
      .check(substring("Check your answers")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Miam Submit
======================================================================================*/

  .group("PRL_Citizen_590_MiamSubmit") {

    exec(http("PRL_Citizen_590_005_MiamSubmit")
      .post(prlURL + "/tasklistresponse/miam/summary")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("saveAndContinue", "true")
      .check(substring("""id="medation-miam-status" class="govuk-tag app-task-list__tag govuk-tag--green">Completed""")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Review and Submit
======================================================================================*/

  .group("PRL_Citizen_600_ReviewAndSubmit") {

    exec(http("PRL_Citizen_600_005_ReviewAndSubmit")
      .get(prlURL + "/tasklistresponse/summary?onlyContinue=true")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Please review your answers before you complete your response.")))

  }

    .pause(MinThinkTime, MaxThinkTime)


  /*======================================================================================
* Check Your Answers - final submit
======================================================================================*/

  .group("PRL_Citizen_610_ReviewSubmit") {

    exec(http("PRL_Citizen_610_005_ReviewSubmit")
      .post(prlURL + "/tasklistresponse/summary?onlyContinue=true")
      .headers(Headers.commonHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .header("content-type", "application/x-www-form-urlencoded")
      .formParam("_csrf", "#{csrf}")
      .formParam("declarationCheck", "declaration")
      .formParam("saveAndContinue", "true")
      .check(substring("Response submitted successfully")))

  }

    .pause(MinThinkTime, MaxThinkTime)


}