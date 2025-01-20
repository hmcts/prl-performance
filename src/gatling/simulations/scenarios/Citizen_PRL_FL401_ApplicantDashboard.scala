package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}
import scala.util.Random


import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Citizen FL401 Link & Access Case, view Dashboard 
======================================================================================*/

object Citizen_PRL_FL401_ApplicantDashboard {

  val prlURL = Environment.prlURL
  val cuiRaURL = Environment.cuiRaURL
  val pcqURL = Environment.pcqURL

  // Variables for user flow control
  val hwfScreens = 0; // Controls whether or not to select help with fees (internal no redirect to gov.uk)
  val pcqScreens = 0; // Controls whether or not to go to PCQ questions

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

/*=======================================================================================
Click Access Code &  Enter Case ID & Pin, Continue
========================================================================================*/
  val RetrieveCase =

    exec(http("PRL_FL401ApplicantDashboard_030_EnterPinPage")
		.get(prlURL + "/pin-activation/enter-pin")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Access your case")))

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_040_EnterPinAndCase") {
		exec(http("PRL_FL401ApplicantDashboard_040_005_EnterPinAndCase")
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

	.group("PRL_FL401ApplicantDashboard_040_CaseActivated") {
		exec(http("PRL_FL401ApplicantDashboard_040_005_CaseActivated")
		.post(prlURL + "/pin-activation/case-activated")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Case added to your account")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	//======================================================================================
	//Click Continue
	//======================================================================================

	.group("PRL_FL401ApplicantDashboard_050_CaseActivatedContinue") {
		exec(http("PRL_FL401ApplicantDashboard_050_005_CaseActivatedContinue")
		.post(prlURL + "/pin-activation/case-activated")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(substring("The court has issued your application")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Open Case CUI
	======================================================================================*/

  val GetCase =

	group("PRL_FL401ApplicantDashboard_050_OpenCase") {
    	exec(http("PRL_FL401ApplicantDashboard_050_005_OpenCase")
		.get(prlURL + "/case/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(substring("Case number")))
	}

     .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Keep Details Private Link
	======================================================================================*/

  val KeepDetailsPrivate =

	group("PRL_FL401ApplicantDashboard_060_OpenKeepDetailsPrivate") {
    	exec(http("PRL_FL401ApplicantDashboard_060_005_OpenKeepDetailsPrivate")
		.get(prlURL + "/applicant/keep-details-private/details_known/#{caseId}")
		.headers(Headers.navigationHeader)
     	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Do the other people named in this application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_070_SelectDetailsKnown") {
		exec(http("PRL_FL401ApplicantDashboard_070_005_SelectDetailsKnown")
		.post(prlURL + "/applicant/keep-details-private/details_known")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("detailsKnown", "yes")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Do you want to keep your contact details private from the other people named in the application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_080_SelectKnownDetails") {
		exec(http("PRL_FL401ApplicantDashboard_080_005_SelectKnownDetails")
		.post(prlURL + "/applicant/keep-details-private/start_alternative")
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

	.group("PRL_FL401ApplicantDashboard_090_ConfirmDetails") {
		exec(http("PRL_FL401ApplicantDashboard_090_005_ConfirmDetails")
		.post(prlURL + "/applicant/keep-details-private/private_details_not_confirmed")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("Respond to an application about a child")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Contact Preferences Link
	======================================================================================*/

  val ContactPreferences =

    exec(http("PRL_FL401ApplicantDashboard_100_OpenContactPreferences")
		.get(prlURL + "/applicant/contact-preference/choose-a-contact-preference")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("You can choose to receive case updates by email or post")))

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_110_ChooseContactPreference") {
		exec(http("PRL_FL401ApplicantDashboard_110_005_ChooseContactPreference")
		.post(prlURL + "/applicant/contact-preference/choose-a-contact-preference")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("partyContactPreference", "email")
		.formParam("onlycontinue", "true")
		.check(CsrfCheck.save)
		.check(substring("You have decided to receive updates by email")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_120_ChooseContactPreferenceReview") {
		exec(http("PRL_FL401ApplicantDashboard_120_005_ChooseContactPreferenceReview")
		.post(prlURL + "/applicant/contact-preference/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("You will receive digital updates about the case.")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_130_ConfirmContactPreference") {
		exec(http("PRL_FL401ApplicantDashboard_130_005_ConfirmContactPreference")
		.post(prlURL + "/applicant/contact-preference/confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("Respond to an application about a child")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Confirm or Edit your contact details link
	======================================================================================*/

  val ConfirmEditContactDetails =

	group("PRL_FL401ApplicantDashboard_131_OpenContactPreferences") {
    	exec(http("PRL_FL401ApplicantDashboard_131_005_OpenContactPreferences")
		.get(prlURL + "/applicant/confirm-contact-details/checkanswers/#{caseId}")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Check your details")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Edit for living in refuge details
	======================================================================================*/

    .exec(http("PRL_FL401ApplicantDashboard_132_005_EditStayingInRefuge")
		.get(prlURL + "/applicant/refuge/staying-in-refuge")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Do you currently live in a refuge?")))

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Staying in Refuge --> No --> Continue
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_133_StayingInRefugeNo") {
    	exec(http("PRL_FL401ApplicantDashboard_133_005_StayingInRefugeNo")
		.post(prlURL + "/applicant/refuge/staying-in-refuge")
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

    .exec(http("PRL_FL401ApplicantDashboard_134_005_ConfirmAddressContinue")
		.get(prlURL + "/applicant/confirm-contact-details/checkanswers?")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Check your details")))

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Check your details --> Save & continue
	======================================================================================*/

    .exec(http("PRL_FL401ApplicantDashboard_134_005_CheckAnswersSaveContinue")
		.post(prlURL + "/applicant/confirm-contact-details/checkanswers?")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("Respond to the application")))

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Support you need during your case Link (Reasonable Adjustments)
	======================================================================================*/

  val SupportYouNeed =

    exec(http("PRL_FL401ApplicantDashboard_140_ReasonableAdjustmentsIntro")
		.get(prlURL + "/applicant/reasonable-adjustments/intro")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Some people need support during their case")))

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_150_ReasonableAdjustmentsStart") {
		exec(http("PRL_FL401ApplicantDashboard_150_005_ReasonableAdjustmentsStart")
		.post(prlURL + "/applicant/reasonable-adjustments/intro")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Think about all communication with the court")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_160_LanguageRequirements") {
		exec(http("PRL_FL401ApplicantDashboard_160_005_LanguageRequirements")
		.post(prlURL + "/applicant/reasonable-adjustments/language-requirements-and-special-arrangements")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("ra_languageReqAndSpecialArrangements", "")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Do you have a physical, mental or learning disability")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_170_NoDisabilities") {
		exec(http("PRL_FL401ApplicantDashboard_170_005_NoDisabilities")
		.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("enabled", "none")
		.formParam("_csrf", "#{csrf}")
		.check(CsrfCheck.save)
		.check(substring("Review the support")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_180_ReviewSupport") {
		exec(http("PRL_FL401ApplicantDashboard_180_005_ReviewSupport")
		.post(cuiRaURL + "/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.check(CsrfCheck.save)
		.check(substring("You have submitted your request to the court")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_190_ConfirmSupport") {
		exec(http("PRL_FL401ApplicantDashboard_190_005_ConfirmSupport")
		.post(prlURL + "/applicant/reasonable-adjustments/confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(substring("Respond to an application about a child")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Respond to the application Link
	======================================================================================*/

  val RespondToApplication =

    exec(http("PRL_FL401ApplicantDashboard_200_TaskListPage")
		.get(prlURL + "/tasklistresponse/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Respond to the application")))

	.pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_FL401ApplicantDashboard_210_LegalRepresentation")
		.get(prlURL + "/tasklistresponse/legalrepresentation/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Will you be using a legal representative")))

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_220_NoLegalRepresentation") {
		exec(http("PRL_FL401ApplicantDashboard_220_005_NoLegalRepresentation")
		.post(prlURL + "/tasklistresponse/legalrepresentation/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("legalRepresentation", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Complete your response")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_230_ConfirmNoLegalRepresentation") {
		exec(http("PRL_FL401ApplicantDashboard_230_005_ConfirmNoLegalRepresentation")
		.post(prlURL + "/tasklistresponse/legalrepresentation/solicitornotdirect")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_240_DoYouConsentToApplication") {
		exec(http("PRL_FL401ApplicantDashboard_240_005_DoYouConsentToApplication")
		.get(prlURL + "/tasklistresponse/consent-to-application/consent/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Your understanding of the application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_250_CheckYourConsent") {
		exec(http("PRL_FL401ApplicantDashboard_250_005_CheckYourConsent")
		.post(prlURL + "/tasklistresponse/consent-to-application/consent")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("doYouConsent", "Yes")
		.formParam("reasonForNotConsenting", "")
		.formParam("applicationReceivedDate-day", "02")
		.formParam("applicationReceivedDate-month", "09")
		.formParam("applicationReceivedDate-year", "2024")
		.formParam("courtOrderDetails", "")
		.formParam("courtPermission", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your consent to the application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_260_ConfirmYourConsent") {
		exec(http("PRL_FL401ApplicantDashboard_260_005_ConfirmYourConsent")
		.post(prlURL + "/tasklistresponse/consent-to-application/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_270_HaveYouAttendedMIAM") {
		exec(http("PRL_FL401ApplicantDashboard_270_005_HaveYouAttendedMIAM")
		.get(prlURL + "/tasklistresponse/miam/miam-start/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Have you attended a Mediation Information and Assessment Meeting")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_280_WillingToAttendMIAM") {
		exec(http("PRL_FL401ApplicantDashboard_280_005_WillingToAttendMIAM")
		.post(prlURL + "/tasklistresponse/miam/miam-start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("miamStart", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Would you be willing to attend a MIAM")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_290_MIAMCheckYourAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_290_005_MIAMCheckYourAnswers")
		.post(prlURL + "/tasklistresponse/miam/willingness-to-attend-miam")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("miamWillingness", "Yes")
		.formParam("miamNotWillingExplnation", "Perf testing")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Mediation Information and Assessment Meeting (MIAM) attendance")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_300_MIAMConfirmAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_300_005_MIAMConfirmAnswers")
		.post(prlURL + "/tasklistresponse/miam/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_310_EverBeenInvolvedInProceedings") {
		exec(http("PRL_FL401ApplicantDashboard_310_005_EverBeenInvolvedInProceedings")
		.get(prlURL + "/tasklistresponse/proceedings/start/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Have you or the children ever been involved in court proceedings")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_320_PreviousProceedingsCheckYourAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_320_005_PreviousProceedingsCheckYourAnswers")
		.post(prlURL + "/tasklistresponse/proceedings/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("proceedingsStart", "No")
		.formParam("proceedingsStartOrder", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Current or previous proceedings")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_330_PreviousProceedingsConfirmAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_330_005_PreviousProceedingsConfirmAnswers")
		.post(prlURL + "/tasklistresponse/proceedings/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_340_SafetyConcerns") {
		exec(http("PRL_FL401ApplicantDashboard_340_005_SafetyConcerns")
		.post(prlURL + "/applicant/safety-concerns/concern-guidance")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Do you have any concerns for your safety or the safety of the children")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_350_AnySafetyConcerns") {
		exec(http("PRL_FL401ApplicantDashboard_350_005_AnySafetyConcerns")
		.post(prlURL + "/applicant/safety-concerns/concerns-for-safety")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("c1A_haveSafetyConcerns", "No")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Please review your answers before you finish your application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_360_SafetyConcernsConfirmAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_360_005_SafetyConcernsConfirmAnswers")
		.post(prlURL + "/applicant/safety-concerns/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_FL401ApplicantDashboard_370_RespondToAllegations")
		.get(prlURL + "/applicant/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Do you wish to respond to the applicant")))

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_380_RespondToAllegationsCheckYourAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_380_005_RespondToAllegationsCheckYourAnswers")
		.post(prlURL + "/applicant/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("aoh_wishToRespond", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Check your answers")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_390_RespondToAllegationsConfirmAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_390_005_RespondToAllegationsConfirmAnswers")
		.post(prlURL + "/applicant/tasklistresponse/respond-to-allegations-of-harm/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_400_StartInternationalFactors") {
		exec(http("PRL_FL401ApplicantDashboard_400_005_StartInternationalFactors")
		.get(prlURL + "/tasklistresponse/international-factors/start/#{caseId}")
		.headers(Headers.navigationHeader)
		.check(CsrfCheck.save)
		.check(substring("lives mainly based outside of England and Wales")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_410_ChildrensLivesBasedOutsideUK") {
		exec(http("PRL_FL401ApplicantDashboard_410_005_ChildrensLivesBasedOutsideUK")
		.post(prlURL + "/tasklistresponse/international-factors/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("iFactorsStartProvideDetails", "")
		.formParam("start", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("parents (or anyone significant to the children) mainly based outside of England and Wales")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_420_ChildrensParentsBasedOutsideUK") {
		exec(http("PRL_FL401ApplicantDashboard_420_005_ChildrensParentsBasedOutsideUK")
		.post(prlURL + "/tasklistresponse/international-factors/parents")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("iFactorsParentsProvideDetails", "")
		.formParam("parents", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Could another person in the application apply for a similar order in a country outside England or Wales")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_430_OtherPersonOutsideUK") {
		exec(http("PRL_FL401ApplicantDashboard_430_005_OtherPersonOutsideUK")
		.post(prlURL + "/tasklistresponse/international-factors/jurisdiction")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("iFactorsJurisdictionProvideDetails", "")
		.formParam("jurisdiction", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Has another country asked (or been asked) for information or help for the children")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_440_OtherCountryAskedForInfo") {
		exec(http("PRL_FL401ApplicantDashboard_440_005_OtherCountryAskedForInfo")
		.post(prlURL + "/tasklistresponse/international-factors/request")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("iFactorsRequestProvideDetails", "")
		.formParam("request", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("International elements")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_450_InternationalFactorsConfirmAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_450_005_InternationalFactorsConfirmAnswers")
		.post(prlURL + "/tasklistresponse/international-factors/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_460_ReviewAndSubmit") {
		exec(http("PRL_FL401ApplicantDashboard_460_005_ReviewAndSubmit")
		.post(prlURL + "/tasklistresponse/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Please review your answers before you complete your response")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_FL401ApplicantDashboard_470_CheckYourAnswers") {
		exec(http("PRL_FL401ApplicantDashboard_470_005_CheckYourAnswers")
		.post(prlURL + "/tasklistresponse/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("declarationCheck", "")
		.formParam("declarationCheck", "declaration")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Equality and diversity questions")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	//================================================================================================
	//PCQ Pages - Equality and diversity questions
	//================================================================================================

 	//=====================
    // Flag for PCQ
    //=====================
    // Save the flag for PCQ screens into session
    .exec { session =>
    session.set("pcqScreens", pcqScreens)
    }

.doIf(session => session("pcqScreens").as[Int] != 1) {

	/*======================================================================================
    * Equality and diversity questions - I don't want to answer these questions
    ======================================================================================*/

    group("PRL_FL401ApplicantDashboard_471_PCQStartNo") {
      exec(http("PRL_FL401ApplicantDashboard_571_005_PCQStartNo")
        .post(pcqURL + "/opt-out")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("opt-out-button", "")
        //.check(regex("""card-details" name="cardDetails" method="POST" action="/card_details/(.*)"""").optional.saveAs("chargeId")) // Needed if fees are added due to extra apps?
        .check(regex("""<strong>(.{16})<\/strong>""").optional.saveAs("caseNumber"))
        .check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        .check(substring("Response submitted successfully")))
    }
} // end of doIf

.doIf(session => session("pcqScreens").as[Int] == 1) { // PCQ Steps if flag is set to 1
    /*======================================================================================
    * Equality and diversity questions - Continue to questions
    ======================================================================================*/

    group("PRL_FL401ApplicantDashboard_471_PCQStartYes") {
      exec(http("PRL_FL401ApplicantDashboard_471_005_PCQStartYes")
        .post(pcqURL + "/start-page")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .check(CsrfCheck.save)
        .check(substring("What is your main language")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your Language? - English
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_472_SelectLanguage") {
      exec(http("PRL_FL401ApplicantDashboard_472_005_SelectLanguage")
        .post(pcqURL + "/language")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("language_main", "4")
        .formParam("language_other", "")
        .check(CsrfCheck.save)
        .check(substring("What is your sex?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your Sex? - Prefer not to say
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_473_SelectSex") {
      exec(http("PRL_FL401ApplicantDashboard_473_005_SelectSex")
        .post(pcqURL + "/sex")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sex", "0")
        .check(CsrfCheck.save)
        .check(substring("Which of the following best describes how you think of yourself?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - Which of the following best describes how you think of yourself? - Hetero or straight
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_474_SelectSexualOrientation") {
      exec(http("PRL_FL401ApplicantDashboard_474_005_SelectSexualOrientation")
        .post(pcqURL + "/sexual-orientation")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("sexuality", "1")
        .formParam("sexuality_other", "")
        .check(CsrfCheck.save)
        .check(substring("Are you married or in a legally registered civil partnership?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

   /*======================================================================================
    * Equality and diversity questions - Are you married or in a legally registered civil partnership? - Yes
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_475_SelectMaritialStatus") {
      exec(http("PRL_FL401ApplicantDashboard_475_005_SelectMaritialStatus")
        .post(pcqURL + "/marital-status")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("marriage", "1")
        .check(CsrfCheck.save)
        .check(substring("What is your ethnic group?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your ethnic group? - Prefer not to say
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_476_SelectEthnicGroup") {
      exec(http("PRL_FL401ApplicantDashboard_476_005_SelectEthnicGroup")
        .post(pcqURL + "/ethnic-group")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("ethnic_group", "0")
        .check(CsrfCheck.save)
        .check(substring("What is your religion?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - What is your religion? - Prefer not to say
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_477_SelectReligion") {
      exec(http("PRL_FL401ApplicantDashboard_477_005_SelectReligion")
        .post(pcqURL + "/religion")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("religion_other", "")
        .formParam("religion", "0")
        .check(CsrfCheck.save)
        .check(substring("Do you have any physical or mental health conditions")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - Do you have any physical or mental health conditions? - No
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_478_HealthConditions") {
      exec(http("PRL_FL401ApplicantDashboard_478_005_HealthConditions")
        .post(pcqURL + "/disability")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("disability_conditions", "2")
        .check(CsrfCheck.save)
        .check(substring("Are you pregnant or have you been pregnant in the last year?")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - Are you pregnant or have you been pregnant in the last year? - No
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_479_SelectPregnancy") {
      exec(http("PRL_FL401ApplicantDashboard_479_005_SelectPregnancy")
        .post(pcqURL + "/pregnant")
        .headers(Headers.commonHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        .header("content-type", "application/x-www-form-urlencoded")
        .formParam("_csrf", "#{csrf}")
        .formParam("pregnancy", "2")
        .check(substring("You have answered the equality questions")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Equality and diversity questions - You have answered the equality questions - Continue to next steps
    ======================================================================================*/

    .group("PRL_FL401ApplicantDashboard_4791_PCQReturnToService") {
      exec(http("PRL_FL401ApplicantDashboard_4791_005_PCQReturnToService")
        .get(pcqURL + "/return-to-service")
        .headers(Headers.navigationHeader)
        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
		.check(CsrfCheck.save)
        //.check(regex("""card-details" name="cardDetails" method="POST" action="/card_details/(.*)"""").optional.saveAs("chargeId"))
        //.check(regex("""csrf" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf"))
        //.check(regex("""csrf2" name="csrfToken" type="hidden" value="(.*)"""").optional.saveAs("csrf2"))
        //.check(regex("""<strong>(.{16})<\/strong>""").optional.saveAs("caseNumber"))
        .check(status.in(302, 200)))
    }

    .pause(MinThinkTime, MaxThinkTime)

} // End of PCQ steps

	/*======================================================================================
	* Select Your Application Link 
	======================================================================================*/

  val YourApplication =

    exec(http("PRL_FL401ApplicantDashboard_500_YourApplication")
      .get(prlURL + "/applicant/documents/download/type/fl401-application/en")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Check the allegations of harm and violence (PDF)
	======================================================================================*/

  val CheckHarmViolenceAllegations =

    exec(http("PRL_FL401ApplicantDashboard_510_HarmAndViolenceDoc")
      .get(prlURL + "/applicant/documents/download/type/aoh-document/en")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Make a request to the court about your case --> Click Link
	======================================================================================*/

  val MakeRequestToCourtAboutCase =  // ** NEW FUNCTIONALITY FOR PRL R7.0 (Out of scope for R6.0)

    exec(http("PRL_FL401ApplicantDashboard_520_MakeRequestToCourtAboutCase")
	  .get(prlURL + "/applicant/application-within-proceedings/list-of-applications/1")
	  .headers(Headers.navigationHeader)
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
	  .check(substring("Make a request to the court about your case")))

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select The response to application link
	======================================================================================*/

  val ResponseToApplication = // ** NEW FUNCTIONALITY FOR PRL R6.0

    exec(http("PRL_FL401ApplicantDashboard_510_RespondentDocuments")
      .get(prlURL + "/applicant/documents/view/applicant/doc")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(regex("<a href=/applicant/documents/download/(.*) target=\"_blank\">C7_Document.pdf</a>").saveAs("respondentDocIdName"))
	  .check(substring("Respondent's documents")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select the document link to open doc
	======================================================================================*/

    .exec(http("PRL_FL401ApplicantDashboard_510_RespondentDocumentDownload")
      .get(prlURL + "/applicant/documents/download/#{respondentDocIdName}")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Return to CaseView
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_510_ReturnToCaseView") {
       exec(http("PRL_FL401ApplicantDashboard_510_ReturnToCaseView")
      .get(prlURL + "/case/#{caseId}")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("Case number #{caseId}")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Upload documents, applications and statements Link
	======================================================================================*/

  val UploadDocumentsApplicationsStatements =

    exec(http("PRL_FL401ApplicantDashboard_510_DocumentsUpload")
      .get(prlURL + "/applicant/documents/upload")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("Select the type of document")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Your Position Statement Link
	======================================================================================*/

    .exec(http("PRL_FL401ApplicantDashboard_520_YourPositionStatement")
      .get(prlURL + "/applicant/documents/upload/your-position-statements/has-the-court-asked-for-this-documents")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
      .check(substring("Your position statement")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Your Position Statement Link - Has the court asked for this document? --> Yes, Continue
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_530_HasCourtAskedForDocumentYes") {
       exec(http("PRL_FL401ApplicantDashboard_530_005_HasCourtAskedForDocumentYes")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/has-the-court-asked-for-this-documents")
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

	.group("PRL_FL401ApplicantDashboard_540_DocumentSharingDetails") {
       exec(http("PRL_FL401ApplicantDashboard_540_005_DocumentSharingDetails")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/document-sharing-details")
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

	.group("PRL_FL401ApplicantDashboard_550_SharingDocumentsNo") {
       exec(http("PRL_FL401ApplicantDashboard_550_005_SharingDocumentsNo")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/sharing-your-documents")
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

	.group("PRL_FL401ApplicantDashboard_560_WitnessStatementSubmit") {
       exec(http("PRL_FL401ApplicantDashboard_560_005_WitnessStatementSubmit")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/upload-your-documents?docCategory=your-position-statements&_csrf=#{csrf}")
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

	.group("PRL_FL401ApplicantDashboard_570_UploadDocumentContinue") {
       exec(http("PRL_FL401ApplicantDashboard_570_005_UploadDocumentContinue")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/upload-your-documents")
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
	* Select upload another document link
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_580_UploadDocumentContinue") {
       exec(http("PRL_FL401ApplicantDashboard_580_005_UploadDocumentContinue")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/upload-documents-success?_csrf=#{csrf}")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("returnToUploadDoc", "true")
      .check(substring("Select the type of document")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Emails, screenshots, images and other media files link (media files)
	======================================================================================*/

 	.exec(http("PRL_FL401ApplicantDashboard_590_SelectMediaFilesLink")
      .get(prlURL + "/applicant/documents/upload/media-files/has-the-court-asked-for-this-documents")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
      .check(substring("Emails, screenshots, images")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Your Position Statement Link - Has the court asked for this document? --> Yes, Continue
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_600_HasCourtAskedForDocumentYes") {
       exec(http("PRL_FL401ApplicantDashboard_600_005_HasCourtAskedForDocumentYes")
      .post(prlURL + "/applicant/documents/upload/media-files/has-the-court-asked-for-this-documents")
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

	.group("PRL_FL401ApplicantDashboard_610_DocumentSharingDetails") {
       exec(http("PRL_FL401ApplicantDashboard_610_005_DocumentSharingDetails")
      .post(prlURL + "/applicant/documents/upload/media-files/document-sharing-details")
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

	.group("PRL_FL401ApplicantDashboard_620_SharingDocumentsNo") {
       exec(http("PRL_FL401ApplicantDashboard_620_005_SharingDocumentsNo")
      .post(prlURL + "/applicant/documents/upload/media-files/sharing-your-documents")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("haveReasonForDocNotToBeShared", "No")
	  .formParam("saveAndContinue", "true")
	  .check(CsrfCheck.save)
	  .check(regex("""csrf" id="csrfToken" value="(.*)"""").optional.saveAs("csrfStatic"))
      .check(substring("Emails, screenshots, images")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Witness statements and evidence, select choose file button, select file and then upload
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_630_UploadDocument") {
       exec(http("PRL_FL401ApplicantDashboard_630_005_UploadDocument")
      .post(prlURL + "/applicant/documents/upload/media-files/upload-your-documents?docCategory=media-files&_csrf=#{csrf}")
      .headers(Headers.navigationHeader)
	  .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
	  .header("Content-Type","multipart/form-data")
	  .bodyPart(RawFileBodyPart("uploadDocumentFileUpload", "PRL-ScreenShot.jpg")
		.contentType("image/jpeg")
		.fileName("PRL-ScreenShot.jpg")
		.transferEncoding("binary"))
		.asMultipartForm
	  .formParam("filename", "PRL-ScreenShot.jpg")
	  .formParam("uploadFile", "true")
	  .check(CsrfCheck.save)
      .check(substring("Remove")))
    }

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Witness statements and evidence, Select checkbox declaration -> Continue
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_640_UploadDocumentContinue") {
       exec(http("PRL_FL401ApplicantDashboard_640_005_UploadDocumentContinue")
      .post(prlURL + "/applicant/documents/upload/media-files/upload-your-documents")
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
	* Select return to case overview
	======================================================================================*/

	.group("PRL_FL401ApplicantDashboard_650_ReturnToCaseOverview") {
       exec(http("PRL_FL401ApplicantDashboard_650_005_UploadDocumentContinue")
      .post(prlURL + "/applicant/documents/upload/your-position-statements/upload-documents-success?_csrf=#{csrf}")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("returnToCaseView", "true")
      .check(substring("Case number")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select View All Documents Link
	======================================================================================*/

  val ViewAllDocuments =

    exec(http("PRL_FL401ApplicantDashboard_660_ViewAllDocuments")
      .get(prlURL + "/applicant/documents/view/all-categories")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(substring("View all documents")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select View your served application pack
	======================================================================================*/

  val ViewServedAppPack =

    exec(http("PRL_FL401ApplicantDashboard_670_ViewServedApplicationPack")
      .get(prlURL + "/applicant/documents/view/application-pack-documents")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(regex("""<a href=/applicant/documents/download/(.*?) target=\"_blank\">""").findAll.saveAs("documentsToView"))
	  .check(substring("You should read the cover letter first")))

    .pause(MinThinkTime, MaxThinkTime)

	//================================================================================================
	//Get total documents to download and a random number between 1 and total documents
	//================================================================================================
	.exec(session => {
		val documentsToView = session("documentsToView").as[Seq[String]]
		val maxDocIndex = documentsToView.length
		println(s"maxDocIndex $maxDocIndex") 
		val randomDocIndex = Random.between(1, maxDocIndex + 1) 
		println(s"randomDocIndex $randomDocIndex") 
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
		.exec(http("PRL_FL401ApplicantDashboard_68#{counter}_ApplicationPackDocumentDownload")
		  .get(prlURL + "/applicant/documents/download/#{downloadDocument}")
		  .headers(Headers.navigationHeader)
		  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		  .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime) 
	}

	/*======================================================================================
	* Select View Respondents Documents link
	======================================================================================*/

  val ViewRespondentsDocuments =

    exec(http("PRL_FL401ApplicantDashboard_690_ViewRespondentDocs")
      .get(prlURL + "/respondent/documents/view/respondent/doc")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(regex("""<a href=/respondent/documents/download/(.*?) target=\"_blank\">""").findAll.saveAs("documentsToView"))
	  .check(substring("Respondent&#39;s documents")))

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
		.exec(http("PRL_FL401ApplicantDashboard_70#{counter}_RespondentAppPackDocumentDownload")
		  .get(prlURL + "/respondent/documents/download/#{downloadDocument}")
		  .headers(Headers.navigationHeader)
		  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		  .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime) 
	}


	/*======================================================================================
	* Select View Applicants Documents link
	======================================================================================*/

  val ViewApplicantsDocuments =

    exec(http("PRL_FL401ApplicantDashboard_690_ViewRespondentDocs")
      .get(prlURL + "/applicant/documents/view/applicant/doc")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(regex("""<a href=/applicant/documents/download/(.*?) target=\"_blank\">""").findAll.saveAs("documentsToView"))
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
		.exec(http("PRL_FL401ApplicantDashboard_70#{counter}_RespondentAppPackDocumentDownload")
		  .get(prlURL + "/applicant/documents/download/#{downloadDocument}")
		  .headers(Headers.navigationHeader)
		  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		  .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime) 
	}

	/*======================================================================================
	* Select View All Orders from the Court Link
	======================================================================================*/

  val ViewOrdersFromTheCourt =

    exec(http("PRL_FL401ApplicantDashboard_690_ViewOrdersFromTheCourt")
      .get(prlURL + "/applicant/documents/view/orders-from-the-court")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(regex("""<a href=/applicant/documents/download/(.*?) target=\"_blank\">""").findAll.saveAs("documentsToView"))
	  .check(substring("Orders from the court")))

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
		.exec(http("PRL_FL401ApplicantDashboard_70#{counter}_CourtOrderDocumentDownload")
		  .get(prlURL + "/applicant/documents/download/#{downloadDocument}")
		  .headers(Headers.navigationHeader)
		  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		  .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime) 
	}

	/*======================================================================================
	* write cases for use in Add RA script
	======================================================================================*/

  val WriteDataToFile =

	exec { session =>
	val fw = new BufferedWriter(new FileWriter("AddRAData.csv", true))
	try {
		fw.write(session("user").as[String] + "," + session("password").as[String] + "," + session("caseId").as[String] + "," + "FL401" + "\r\n")
	} finally fw.close()
	session
	}

}