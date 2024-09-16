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
      .check(substring("You have been named as the respondent in a domestic abuse application and have an order from the court")))

     .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Keep Details Private
  //========================================================================================   

  val KeepDetailsPrivate = 

    exec(http("PRL_FL401Respondent_070_OpenKeepDetailsPrivate")
	  .get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("Do the other people named in this application")))

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_080_SelectDetailsKnown")
	  .post(prlURL + "/respondent/keep-details-private/details_known")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("detailsKnown", "yes")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Do you want to keep your contact details private from the other people named in the application")))


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
      .check(CsrfCheck.save)
	  .check(substring("The court will not keep your contact details private")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_100_ConfirmDetails")
	  .post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
	  .check(substring("You have been named as the respondent in a domestic abuse application")))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Contact Preferences
  //========================================================================================   

  val ContactDetails =

  	exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth(),
      "PRLAppDobYear" -> Common.getDobYear(),
      "PRLChildDobYear" -> Common.getDobYearChild()))

	.exec(http("PRL_FL401Respondent_110_OpenContactDetails")
	  .get(prlURL + "/respondent/confirm-contact-details/checkanswers/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("Read the information to make sure it is correct, and add any missing details")))

	// Redirect to
	//.exec(http("PRL_FL401Respondent_120_OpenContactDetails")
	//  .get(prlURL + "/respondent/confirm-contact-details/checkanswers")
	//  .headers(Headers.navigationHeader)
    //  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    //  .check(CsrfCheck.save))

	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Edit (Place of Birth)
  //========================================================================================   
	
	.exec(http("PRL_FL401Respondent_120_EditPlaceOfBirth")
	  .get(prlURL + "/respondent/confirm-contact-details/personaldetails")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(regex("""name="citizenUserFirstNames" type="text" value="(.+?)">""").saveAs("citizenFirstName"))
	  .check(regex("""name="citizenUserLastNames" type="text" value="(.+?)">""").saveAs("citizenLastName"))
	  .check(regex("""name="citizenUserDateOfBirth-day" type="text" value="(.+?)" inputmode="numeric"""").saveAs("citizenDOBDay"))
	  .check(regex("""name="citizenUserDateOfBirth-month" type="text" value="(.+?)" inputmode="numeric"""").saveAs("citizenDOBMonth"))
	  .check(regex("""name="citizenUserDateOfBirth-year" type="text" value="(.+?)" inputmode="numeric"""").saveAs("citizenDOBYear"))
	  .check(substring("Your name and date of birth")))


	  //name="citizenUserFirstNames" type="text" value="Jane">
	  //name="citizenUserLastNames" type="text" value="Smith">
	  //name="citizenUserDateOfBirth-day" type="text" value="10" inputmode="numeric"
	  //name="citizenUserDateOfBirth-month" type="text" value="4" inputmode="numeric"
	  //name="citizenUserDateOfBirth-year" type="text" value="1981" inputmode="numeric"
	  
	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Enter Place of Birth and Continue
  //========================================================================================  

	.exec(http("PRL_FL401Respondent_130_SubmitPlaceOfBirthContinue")
	  .post(prlURL + "/respondent/confirm-contact-details/personaldetails")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("citizenUserFirstNames", "#{citizenFirstName}")  
	  .formParam("citizenUserLastNames", "#{citizenLastName}")
	  .formParam("citizenUserAdditionalName", "")
	  .formParam("citizenUserDateOfBirth-day", "#{citizenDOBDay}")
	  .formParam("citizenUserDateOfBirth-month", "#{citizenDOBMonth}")
	  .formParam("citizenUserDateOfBirth-year", "#{citizenDOBYear}")
	  .formParam("citizenUserPlaceOfBirth", "#{PRLRandomString} City")
	  .formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Check your details")))

	// Redirect to
	//.exec(http("PRL_FL401Respondent_140_SubmitPlaceOfBirthCheckAnswers")
	//  .get(prlURL + "/respondent/confirm-contact-details/checkanswers")
	//  .headers(Headers.navigationHeader)
    //  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    //  .check(CsrfCheck.save))

	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Save & Continue
  //========================================================================================   

	.exec(http("PRL_FL401Respondent_140_Save&Continue")
	  .post(prlURL + "/respondent/confirm-contact-details/checkanswers")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("citizenUserPlaceOfBirth", "#{PRLRandomString} City")
	  .formParam("saveAndContinue", "true"))

	.pause(MinThinkTime, MaxThinkTime)

	// Redirect to
	//.exec(http("PRL_FL401Respondent_150_Save&ContinueGetTasklist")
	//  .get(prlURL + "/task-list/respondent")
	//  .headers(Headers.navigationHeader)
    //  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    //  .check(CsrfCheck.save))


  //========================================================================================
  // Select Contact Preferences
  //========================================================================================   

  val ContactPreferences = 

    exec(http("PRL_FL401Respondent_110_OpenContactPreferences")
	  .get(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("You can choose to receive case updates by email or post")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_120_ChooseContactPreference")
	  .post(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("partyContactPreference", "email")
	  .formParam("onlycontinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("You have decided to receive updates by email")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_130_ChooseContactPreferenceReview")
	  .post(prlURL + "/respondent/contact-preference/review")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("You will receive digital updates about the case.")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_140_ConfirmContactPreference")
	  .post(prlURL + "/respondent/contact-preference/confirmation")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
	  .check(substring("You have been named as the respondent in a domestic abuse application")))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Support you need during your case
  //========================================================================================     
  
  val SupportYouNeed =

    exec(http("PRL_FL401Respondent_150_ReasonableAdjustmentsIntro")
	  .get(prlURL + "/respondent/reasonable-adjustments/intro")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("Some people need support during their case")))
      
	  .pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_160_ReasonableAdjustmentsStart")
	  .post(prlURL + "/respondent/reasonable-adjustments/intro")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Think about all communication with the court")))

	//Redirected to: 
	//.exec(http("PRL_FL401Respondent_170_LanguageRequirements")
	//  .get(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
	//  .headers(Headers.navigationHeader)
    //  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    //  .check(CsrfCheck.save)
	//  .check(substring("Language requirements and special arrangements")))

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_175_LanguageRequirements")
	  .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("ra_languageReqAndSpecialArrangements", "Perf Test - Add Reasonable Adjustments")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Review your language requirements and special arrangements")))

	//Redirected to:
	//.exec(http("PRL_FL401Respondent_177_LanguageRequirementsReview")
	//  .get(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements/review")
	//  .headers(Headers.navigationHeader)
    //  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    //  .check(CsrfCheck.save)
	//  .check(substring("Review your language requirements and special arrangements")))

	 .pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_175_LanguageRequirementsReview")
	  .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements/review")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	 .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Do you have a physical, mental or learning disability")))

	//Redirect to:
	//.exec(http("PRL_RA_030_LaunchReasonableAdjustments")
	//  .get(prlURL + "/reasonable-adjustments/launch")
	//  .headers(Headers.navigationHeader)
    //  .check(substring("Select all that apply to you"))
    //  .check(CsrfCheck.save)
    //  .check(substring("Do you have a physical, mental or learning disability")))

	.exec(http("PRL_FL401Respondent_180_NoDisabilities")
	  .post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("enabled", "none")
	  .formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
	  .check(substring("Review the support")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_190_ReviewSupport")
	  .post(cuiRaURL + "/review")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
	  .check(substring("You have submitted your request to the court")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401Respondent_200_ConfirmSupport")
	  .post(prlURL + "/respondent/reasonable-adjustments/confirmation")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
	  .check(substring("Child arrangements and family injunction cases")))

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


// write cases for use in Add RA script
	.exec { session =>
	val fw = new BufferedWriter(new FileWriter("AddRAData.csv", true))
	try {
		fw.write(session("user").as[String] + "," + session("password").as[String] + "," + session("caseId").as[String] + "\r\n")
	} finally fw.close()
	session
	} 
  
}