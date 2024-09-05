package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Respond to a FL401 case within PRL CUI
======================================================================================*/

object Citizen_PRL_FL401_Respondent {
  
  val prlURL = Environment.prlURL
  val cuiRaURL = Environment.cuiRaURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  //========================================================================================
  // Enter Case ID & Pin
  //========================================================================================

  val RetrieveCase =

    exec(http("PRL_FL401Respondent_030_EnterPinPage")
	  .get(prlURL + "/pin-activation/enter-pin")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Access your case")))

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_040_EnterPinAndCase")
	  .post(prlURL + "/pin-activation/enter-pin")
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("caseCode", "#{caseId}")
	  .formParam("accessCode", "#{accessCode}")
	  .formParam("onlyContinue", "true")
	  .check(CsrfCheck.save))

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_050_CaseActivated")
	  .post(prlURL + "/pin-activation/case-activated")
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
	  .check(substring("You have been named as the respondent")))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Open the Case
  //========================================================================================

  val GetCase = 

    exec(http("PRL_FL401Respondent_060_OpenCase")
	  .get(prlURL + "/case/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("You should respond within 14 days of receiving the application")))

     .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Keep Details Private
  //========================================================================================   

  val KeepDetailsPrivate = 

    exec(http("PRL_FL401Respondent_070_OpenKeepDetailsPrivate")
	  .get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_080_SelectDetailsKnown")
	  .post(prlURL + "/respondent/keep-details-private/details_known")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("detailsKnown", "yes")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save))

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_090_SelectKnownDetails")
	  .post(prlURL + "/respondent/keep-details-private/start_alternative")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("contactDetailsPrivate", "")
	  .formParam("contactDetailsPrivate", "")
	  .formParam("contactDetailsPrivate", "")
	  .formParam("contactDetailsPrivate", "address")
	  .formParam("contactDetailsPrivate", "phoneNumber")
	  .formParam("contactDetailsPrivate", "email")
	  .formParam("startAlternative", "No")
	  .formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_100_ConfirmDetails")
	  .post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Contact Preferences
  //========================================================================================   

  val ContactPreferences = 

    exec(http("PRL_FL401Respondent_110_OpenContactPreferences")
	  .get(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_120_ChooseContactPreference")
	  .post(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("partyContactPreference", "email")
	  .formParam("onlycontinue", "true")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_130_ChooseContactPreferenceReview")
	  .post(prlURL + "/respondent/contact-preference/review")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_140_ConfirmContactPreference")
	  .post(prlURL + "/respondent/contact-preference/confirmation")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Support you need during your case
  //========================================================================================     
  
  val SupportYouNeed =

    exec(http("PRL_FL401Respondent_150_ReasonableAdjustmentsIntro")
	  .get(prlURL + "/respondent/reasonable-adjustments/intro")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
	  .pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_160_ReasonableAdjustmentsStart")
	  .post(prlURL + "/respondent/reasonable-adjustments/intro")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
	  .pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_170_LanguageRequirements")
	  .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("ra_languageReqAndSpecialArrangements", "")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_180_NoDisabilities")
	  .post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("enabled", "none")
	  .formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_190_ReviewSupport")
	  .post(cuiRaURL + "/review")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_200_ConfirmSupport")
	  .post(prlURL + "/respondent/reasonable-adjustments/confirmation")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select check the application
  //========================================================================================  

  val CheckApplication =

    exec(http("PRL_FL401Respondent_210_CheckApplication")
			.get(prlURL + "/respondent/documents/download/type/cada-document")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)


  // Enter Case ID & Pin
  // Begin required steps

  

  
}