package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}
import scala.util.Random

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

	.group("PRL_FL401Respondent_040_EnterPinAndCase") {
	  exec(http("PRL_FL401Respondent_040_005_EnterPinAndCase")
	  .post(prlURL + "/pin-activation/enter-pin")
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("caseCode", "#{caseId}")
	  .formParam("accessCode", "#{accessCode}")
	  .formParam("onlyContinue", "true")
	  .check(CsrfCheck.save))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_050_CaseActivated") {
	  exec(http("PRL_FL401Respondent_050_005_CaseActivated")
	  .post(prlURL + "/pin-activation/case-activated")
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
	  .check(status.is(200)))
	}

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Open the Case
  //========================================================================================

  val GetCase = 

	group("PRL_FL401Respondent_060_OpenCase") {
      exec(http("PRL_FL401Respondent_060_005_OpenCase")
	  .get(prlURL + "/case/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("<strong id=\"editYouContactDetails-status\"")))
	}

     .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Keep Details Private
  //========================================================================================   

  val KeepDetailsPrivate = 

	group("PRL_FL401Respondent_160_OpenKeepDetailsPrivate") {
      exec(http("PRL_FL401Respondent_160_005_OpenKeepDetailsPrivate")
	  .get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("Do the other people named in this application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Does the other person named in your application (the respondent) know any of your contact details? - Yes
  //========================================================================================   

	.group("PRL_FL401Respondent_170_SelectDetailsKnown") {
	  exec(http("PRL_FL401Respondent_170_005_SelectDetailsKnown")
	  .post(prlURL + "/respondent/keep-details-private/details_known")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("detailsKnown", "yes")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Do you want to keep your contact details private from the other people named in the application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_180_SelectKnownDetails") {
	  exec(http("PRL_FL401Respondent_180_005_SelectKnownDetails")
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
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_190_ConfirmDetails") {
	  exec(http("PRL_FL401Respondent_190_005_ConfirmDetails")
	  .post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
	  .check(substring("<strong id=\"keepYourDetailsPrivate-status\" class=\"govuk-tag app-task-list__tag govuk-tag--green\">Completed</strong>")))
	}

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Contact Preferences
  //========================================================================================   

  val ContactDetails = // Section no longer needed in R6.0? 

  	exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth()))

	.group("PRL_FL401Respondent_070_OpenContactDetails") {
	  exec(http("PRL_FL401Respondent_070_005_OpenContactDetails")
	  .get(prlURL + "/respondent/confirm-contact-details/checkanswers/#{caseId}")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("Read the information to make sure it is correct, and add any missing details")))
	}

	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Edit (Place of Birth)
  //========================================================================================   
	
	.exec(http("PRL_FL401Respondent_080_EditPlaceOfBirth")
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
	  
	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Enter Place of Birth and Continue
  //========================================================================================  

	.group("PRL_FL401Respondent_090_SubmitPlaceOfBirthContinue") {
	  exec(http("PRL_FL401Respondent_090_005_SubmitPlaceOfBirthContinue")
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
	}

	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Save & Continue
  //========================================================================================   

	.group("PRL_FL401Respondent_100_Save&Continue") {
	  exec(http("PRL_FL401Respondent_100_005_Save&Continue")
	  .post(prlURL + "/respondent/confirm-contact-details/checkanswers")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("citizenUserPlaceOfBirth", "#{PRLRandomString} City")
	  .formParam("saveAndContinue", "true"))
	}

	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select link: Confirm or edit your contact details
  //========================================================================================   

  val ConfirmEditContactDetails = 

	group("PRL_FL401Respondent_070_OpenContactDetails") {
    	exec(http("PRL_FL401Respondent_070_005_OpenContactDetails")
		.get(prlURL + "/respondent/confirm-contact-details/checkanswers/#{caseId}")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Check your details")))
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Edit for living in refuge details 
	======================================================================================*/

    .exec(http("PRL_FL401Respondent_080_EditStayingInRefuge")
		.get(prlURL + "/respondent/refuge/staying-in-refuge")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Do you currently live in a refuge?")))
      
	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Staying in Refuge --> No --> Continue
	======================================================================================*/

	.group("PRL_FL401Respondent_090_StayingInRefugeNo") {
    	exec(http("PRL_FL401Respondent_090_005_StayingInRefugeNo")
		.post(prlURL + "/respondent/refuge/staying-in-refuge")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("isCitizenLivingInRefuge", "No")
		.formParam("onlyContinue", "true")
		.check(substring("Your address")))
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Your address --> Continue
	======================================================================================*/
	
    .exec(http("PRL_FL401Respondent_100_ConfirmAddressContinue")
		.get(prlURL + "/respondent/confirm-contact-details/checkanswers?")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Check your details")))
      
	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Check your details --> Save & continue
	======================================================================================*/
	
	.group("PRL_FL401Respondent_110_CheckAnswersSaveContinue") {
       exec(http("PRL_FL401Respondent_110_005_CheckAnswersSaveContinue")
		.post(prlURL + "/respondent/confirm-contact-details/checkanswers?")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("<strong id=\"editYouContactDetails-status\" class=\"govuk-tag app-task-list__tag govuk-tag--green\">Completed</strong>")))
	}
      
	.pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Contact Preferences --- Open Link
  //========================================================================================   

  val ContactPreferences = 

    exec(http("PRL_FL401Respondent_120_OpenContactPreferences")
	  .get(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("You can choose to receive case updates by email or post")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_130_ChooseContactPreference") {
	  exec(http("PRL_FL401Respondent_130_005_ChooseContactPreference")
	  .post(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("partyContactPreference", "email")
	  .formParam("onlycontinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("You have decided to receive updates by email")))
	}
     
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_140_ChooseContactPreferenceReview") {
	  exec(http("PRL_FL401Respondent_140_005_ChooseContactPreferenceReview")
	  .post(prlURL + "/respondent/contact-preference/review")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("You will receive digital updates about the case.")))
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_150_ConfirmContactPreference") {
	  exec(http("PRL_FL401Respondent_150_005_ConfirmContactPreference")
	  .post(prlURL + "/respondent/contact-preference/confirmation")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("saveAndContinue", "true")
	  .check(substring("<strong id=\"contactPreferences-status\" class=\"govuk-tag app-task-list__tag govuk-tag--green\">Completed</strong>")))
	}

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select Support you need during your case link (Reasonable Adjustments)
  //========================================================================================     
  
  val SupportYouNeed =

    exec(http("PRL_FL401Respondent_200_ReasonableAdjustmentsIntro")
	  .get(prlURL + "/respondent/reasonable-adjustments/intro")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
	  .check(substring("Some people need support during their case")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_210_ReasonableAdjustmentsStart") {
	  exec(http("PRL_FL401Respondent_210_005_ReasonableAdjustmentsStart")
	  .post(prlURL + "/respondent/reasonable-adjustments/intro")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Think about all communication with the court")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_220_LanguageRequirements") {
	  exec(http("PRL_FL401Respondent_220_005_LanguageRequirements")
	  .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
	  .header("Content-Type","application/x-www-form-urlencoded")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("ra_languageReqAndSpecialArrangements", "Perf Test - Add Reasonable Adjustments")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Review your language requirements and special arrangements")))
	}

	 .pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_230_LanguageRequirementsReview") {
	  exec(http("PRL_FL401Respondent_230_005_LanguageRequirementsReview")
	  .post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements/review")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
	  .check(substring("Reasonable adjustment for")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_240_NoDisabilities") {
	  exec(http("PRL_FL401Respondent_124_005_NoDisabilities")
	  .post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("enabled", "none")
	  .formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
	  .check(substring("Review the support")))
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_250_ReviewSupport") {
	  exec(http("PRL_FL401Respondent_250_005_ReviewSupport")
	  .post(cuiRaURL + "/review")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
	  .check(substring("You have submitted your request to the court")))
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401Respondent_260_ConfirmSupport") {
	  exec(http("PRL_FL401Respondent_260_005_ConfirmSupport")
	  .post(prlURL + "/respondent/reasonable-adjustments/confirmation")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
	  .check(substring("#{caseId}</p>")))
	}

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Select check the application
  //========================================================================================  

  val CheckApplication =

    exec(http("PRL_FL401Respondent_270_CheckApplication")
	  .get(prlURL + "/respondent/documents/download/type/cada-document/en")
	  .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Make a request to the court about your case --> Click Link
	======================================================================================*/

  val MakeRequestToCourtAboutCase =  // ** NEW FUNCTIONALITY FOR PRL R7.0 (Out of scope for R6.0)

    exec(http("PRL_FL401Respondent_280_MakeRequestToCourtAboutCase")
	  .get(prlURL + "/respondent/application-within-proceedings/list-of-applications/1")
	  .headers(Headers.navigationHeader)
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
	  .check(substring("Make a request to the court about your case")))

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Upload documents, applications and statements Link
	======================================================================================*/

  val UploadDocumentsApplicationsStatements =

    exec(http("PRL_FL401Respondent_290_DocumentsUpload")
      .get(prlURL + "/respondent/documents/upload")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("Select the type of document")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Your Position Statement Link
	======================================================================================*/

    .exec(http("PRL_FL401Respondent_300_YourPositionStatement")
      .get(prlURL + "/respondent/documents/upload/your-position-statements/has-the-court-asked-for-this-documents")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
      .check(substring("Your position statement")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Your Position Statement Link - Has the court asked for this document? --> Yes, Continue
	======================================================================================*/

	.group("PRL_FL401Respondent_310_HasCourtAskedForDocumentYes") {
       exec(http("PRL_FL401Respondent_310_005_HasCourtAskedForDocumentYes")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/has-the-court-asked-for-this-documents")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("hasCourtAskedForThisDoc", "Yes")
	  .formParam("onlyContinue", "true")
	  .check(CsrfCheck.save)
      .check(substring("Before you submit a document")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Before you submit a document --> Continue
	======================================================================================*/

	.group("PRL_FL401Respondent_320_DocumentSharingDetails") {
       exec(http("PRL_FL401Respondent_320_005_DocumentSharingDetails")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/document-sharing-details")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("onlyContinue", "true")
	  .check(CsrfCheck.save)
      .check(substring("Sharing your documents")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Sharing your documents, Is there a good reason... --> No, Continue
	======================================================================================*/

	.group("PRL_FL401Respondent_330_SharingDocumentsNo") {
       exec(http("PRL_FL401Respondent_330_005_SharingDocumentsNo")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/sharing-your-documents")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("haveReasonForDocNotToBeShared", "No")
	  .formParam("saveAndContinue", "true")
	  .check(CsrfCheck.save)
      .check(substring("Position statement")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Position statement, Write witness statement free text-> Submit
	======================================================================================*/

	.group("PRL_FL401Respondent_340_WitnessStatementSubmit") {
       exec(http("PRL_FL401Respondent_340_005_WitnessStatementSubmit")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/upload-your-documents?docCategory=your-position-statements&_csrf=#{csrf}")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("statementText", "Test+witness+statement.+a+fair+amount+of+text+should+be+here+as+a+witness+statement+would+be+relatively+long.+%0D%0A%0D%0AYou+can+write+your+statement+in+the+text+box+or+upload+it.%0D%0A%0D%0AIf+you+are+uploading+documents+from+a+computer%2C+name+the+files+clearly.+For+example%2C+letter-from-school.doc.%0D%0A%0D%0AFiles+must+end+with+JPG%2C+BMP%2C+PNG%2CTIF%2C+PDF%2C+DOC+or+DOCX+and+have+a+maximum+size+of+20mb.%0D%0A%0D%0Aroceedings+for+contempt+of+court+may+be+brought+against+anyone+who+makes%2C+or+causes+to+be+made%2C+a+false+statement+verified+by+a+statement+of+truth+without+an+honest+belief+in+its+truth.%0D%0A%0D%0AThis+confirms+that+the+information+you+are+submitting+is+true+and+accurate%2C+to+the+best+of+your+knowledge.")
	  .formParam("generateDocument", "true")
	  .check(CsrfCheck.save)
      .check(substring("_position_statements_")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Position statement, Select checkbox declaratiom -> Continue
	======================================================================================*/

	.group("PRL_FL401Respondent_350_UploadDocumentContinue") {
       exec(http("PRL_FL401Respondent_350_005_UploadDocumentContinue")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/upload-your-documents")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("declarationCheck", "")
	  .formParam("declarationCheck", "declaration")
	  .formParam("onlyContinue", "true")
	  .check(CsrfCheck.save)
      .check(substring("Document submitted")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select View All Documents Link
	======================================================================================*/

  val ViewAllDocuments =

    exec(http("PRL_FL401Respondent_360_ViewAllDocuments")
      .get(prlURL + "/respondent/documents/view/all-categories")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(substring("View all documents")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select View Applicants Documents link
	======================================================================================*/

  val ViewApplicantsDocuments =

    exec(http("PRL_FL401Respondent_370_ViewApplicantsDocs")
      .get(prlURL + "/respondent/documents/view/applicant/doc")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(regex("""<a href=/respondent/documents/download/(.*?) target=\"_blank\">""").findAll.saveAs("documentsToView"))
	  .check(substring("Applicant&#39;s documents")))

    .pause(MinThinkTime, MaxThinkTime)

	//================================================================================================
	//Get total documents to download and a random number between 1 and total documents
	//================================================================================================
	.exec(session => {
		val documentsToView = session("documentsToView").as[Seq[String]]
		val maxDocIndex = documentsToView.length
		println(s"Respondents Docs maxDocIndex: $maxDocIndex") 
		val randomDocIndex = Random.between(1, maxDocIndex + 1) 
		println(s"Respondents Docs randomDocIndex: $randomDocIndex") 
		session.set("randomDocIndex", randomDocIndex) 
	})
	//================================================================================================
	//View a random number of documents within the application pack
	//================================================================================================
	.repeat(session => session("randomDocIndex").as[Int], "counter") {
		exec(session => {
		val documentsToView = session("documentsToView").as[Seq[String]] 
		val currentIndex = session("counter").as[Int] 
		val currentDocument = documentsToView(currentIndex) 
		session.set("downloadDocument", currentDocument)
		})
	//================================================================================================
	//Open a document from within the pack
	//================================================================================================
		.exec(http("PRL_FL401Respondent_38#{counter}_ApplicantDocumentDownload")
		  .get(prlURL + "/respondent/documents/download/#{downloadDocument}")
		  .headers(Headers.navigationHeader)
		  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		  .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime) 
	}

	/*======================================================================================
	* Select View Check details of your court hearings link
	======================================================================================*/

  val ViewCourtHearings =

	group("PRL_FL401Respondent_390_ViewCourtHearings") {
      exec(http("PRL_FL401Respondent_470_005_ViewCourtHearings")
      .get(prlURL + "/respondent/hearings/#{caseId}")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(substring("Your court hearings")))
	}

    .pause(MinThinkTime, MaxThinkTime)

// write cases for use in Add RA script
	.exec { session =>
	val fw = new BufferedWriter(new FileWriter("AddRAData.csv", true))
	try {
		fw.write(session("user").as[String] + "," + session("password").as[String] + "," + session("caseId").as[String] + "," + "FL401" + "\r\n")
	} finally fw.close()
	session
	} 
  
}