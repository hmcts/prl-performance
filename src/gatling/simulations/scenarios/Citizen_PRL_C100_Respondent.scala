package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Citizen_PRL_C100_Respondent {
  
  val prlURL = Environment.prlURL
  val cuiRaURL = Environment.cuiRaURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

/*=======================================================================================
Click Access Code &  Enter Case ID & Pin
========================================================================================*/
  val RetrieveCase =

    exec(http("PRL_C100Respondent_030_EnterPinPage")
		.get(prlURL + "/pin-activation/enter-pin")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Access your case")))

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_040_EnterPinAndCase") {
		exec(http("PRL_C100Respondent_040_005_EnterPinAndCase")
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

	.group("PRL_C100Respondent_040_CaseActivated") {
		exec(http("PRL_C100Respondent_040_005_CaseActivated")
		.post(prlURL + "/pin-activation/case-activated")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(substring("Case number")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*// write cases for use in Add RA script
	.exec { session =>
	val fw = new BufferedWriter(new FileWriter("AddRAData.csv", true))
	try {
		fw.write(session("user").as[String] + "," + session("password").as[String] + "," + session("caseId").as[String] + "," + "C100" + "\r\n")
	} finally fw.close()
	session
	} */

  	/*======================================================================================
	* Open Case CUI
	======================================================================================*/

  val GetCase = 

	group("PRL_C100Respondent_050_OpenCase") {
    	exec(http("PRL_C100Respondent_050_005_OpenCase")
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

	group("PRL_C100Respondent_060_OpenKeepDetailsPrivate") {
    	exec(http("PRL_C100Respondent_060_005_OpenKeepDetailsPrivate")
		.get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
		.headers(Headers.navigationHeader)
     	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("Do the other people named in this application")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_070_SelectDetailsKnown") {
		exec(http("PRL_C100Respondent_070_005_SelectDetailsKnown")
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

	.group("PRL_C100Respondent_080_SelectKnownDetails") {
		exec(http("PRL_C100Respondent_080_005_SelectKnownDetails")
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

	.group("PRL_C100Respondent_090_ConfirmDetails") {
		exec(http("PRL_C100Respondent_090_005_ConfirmDetails")
		.post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
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

    exec(http("PRL_C100Respondent_100_OpenContactPreferences")
		.get(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
		.headers(Headers.navigationHeader)
      	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save)
      	.check(substring("You can choose to receive case updates by email or post")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_110_ChooseContactPreference") {
		exec(http("PRL_C100Respondent_110_005_ChooseContactPreference")
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

	.group("PRL_C100Respondent_120_ChooseContactPreferenceReview") {
		exec(http("PRL_C100Respondent_120_005_ChooseContactPreferenceReview")
		.post(prlURL + "/respondent/contact-preference/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("You will receive digital updates about the case.")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_130_ConfirmContactPreference") {
		exec(http("PRL_C100Respondent_130_005_ConfirmContactPreference")
		.post(prlURL + "/respondent/contact-preference/confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("Respond to an application about a child")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Support you need during your case Link (Reasonable Adjustments)
	======================================================================================*/
  
  val SupportYouNeed =

    exec(http("PRL_C100Respondent_140_ReasonableAdjustmentsIntro")
		.get(prlURL + "/respondent/reasonable-adjustments/intro")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Some people need support during their case")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_150_ReasonableAdjustmentsStart") {
		exec(http("PRL_C100Respondent_150_005_ReasonableAdjustmentsStart")
		.post(prlURL + "/respondent/reasonable-adjustments/intro")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Think about all communication with the court")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_160_LanguageRequirements") {
		exec(http("PRL_C100Respondent_160_005_LanguageRequirements")
		.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("ra_languageReqAndSpecialArrangements", "")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("We know some people need support to access information and use our services")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_170_NoDisabilities") {
		exec(http("PRL_C100Respondent_170_005_NoDisabilities")
		.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("enabled", "none")
		.formParam("_csrf", "#{csrf}")
		.check(CsrfCheck.save)
		.check(substring("Review the support")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_180_ReviewSupport") {
		exec(http("PRL_C100Respondent_180_005_ReviewSupport")
		.post(cuiRaURL + "/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.check(CsrfCheck.save)
		.check(substring("You have submitted your request to the court")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_190_ConfirmSupport") {
		exec(http("PRL_C100Respondent_190_005_ConfirmSupport")
		.post(prlURL + "/respondent/reasonable-adjustments/confirmation")
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

    exec(http("PRL_C100Respondent_200_TaskListPage")
		.get(prlURL + "/tasklistresponse/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Respond to the application")))
      
	.pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_C100Respondent_210_LegalRepresentation")
		.get(prlURL + "/tasklistresponse/legalrepresentation/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Will you be using a legal representative")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_220_NoLegalRepresentation") {
		exec(http("PRL_C100Respondent_220_005_NoLegalRepresentation")
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

	.group("PRL_C100Respondent_230_ConfirmNoLegalRepresentation") {
		exec(http("PRL_C100Respondent_230_005_ConfirmNoLegalRepresentation")
		.post(prlURL + "/tasklistresponse/legalrepresentation/solicitornotdirect")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_240_DoYouConsentToApplication") {
		exec(http("PRL_C100Respondent_240_005_DoYouConsentToApplication")
		.get(prlURL + "/tasklistresponse/consent-to-application/consent/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Your understanding of the application")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_250_CheckYourConsent") {
		exec(http("PRL_C100Respondent_250_005_CheckYourConsent")
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

	.group("PRL_C100Respondent_260_ConfirmYourConsent") {
		exec(http("PRL_C100Respondent_260_005_ConfirmYourConsent")
		.post(prlURL + "/tasklistresponse/consent-to-application/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_270_HaveYouAttendedMIAM") {
		exec(http("PRL_C100Respondent_270_005_HaveYouAttendedMIAM")
		.get(prlURL + "/tasklistresponse/miam/miam-start/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Have you attended a Mediation Information and Assessment Meeting")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_280_WillingToAttendMIAM") {
		exec(http("PRL_C100Respondent_280_005_WillingToAttendMIAM")
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

	.group("PRL_C100Respondent_290_MIAMCheckYourAnswers") {
		exec(http("PRL_C100Respondent_290_005_MIAMCheckYourAnswers")
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

	.group("PRL_C100Respondent_300_MIAMConfirmAnswers") {
		exec(http("PRL_C100Respondent_300_005_MIAMConfirmAnswers")
		.post(prlURL + "/tasklistresponse/miam/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_310_EverBeenInvolvedInProceedings") {
		exec(http("PRL_C100Respondent_310_005_EverBeenInvolvedInProceedings")
		.get(prlURL + "/tasklistresponse/proceedings/start/#{caseId}")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Have you or the children ever been involved in court proceedings")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_320_PreviousProceedingsCheckYourAnswers") {
		exec(http("PRL_C100Respondent_320_005_PreviousProceedingsCheckYourAnswers")
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

	.group("PRL_C100Respondent_330_PreviousProceedingsConfirmAnswers") {
		exec(http("PRL_C100Respondent_330_005_PreviousProceedingsConfirmAnswers")
		.post(prlURL + "/tasklistresponse/proceedings/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_340_SafetyConcerns") {
		exec(http("PRL_C100Respondent_340_005_SafetyConcerns")
		.post(prlURL + "/respondent/safety-concerns/concern-guidance")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Do you have any concerns for your safety or the safety of the children")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_350_AnySafetyConcerns") {
		exec(http("PRL_C100Respondent_350_005_AnySafetyConcerns")
		.post(prlURL + "/respondent/safety-concerns/concerns-for-safety")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("c1A_haveSafetyConcerns", "No")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Please review your answers before you finish your application")))
	}

	.pause(MinThinkTime, MaxThinkTime)
	
	.group("PRL_C100Respondent_360_SafetyConcernsConfirmAnswers") {
		exec(http("PRL_C100Respondent_360_005_SafetyConcernsConfirmAnswers")
		.post(prlURL + "/respondent/safety-concerns/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.exec(http("PRL_C100Respondent_370_RespondToAllegations")
		.get(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(CsrfCheck.save)
		.check(substring("Do you wish to respond to the applicant")))
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_380_RespondToAllegationsCheckYourAnswers") {
		exec(http("PRL_C100Respondent_380_005_RespondToAllegationsCheckYourAnswers")
		.post(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("aoh_wishToRespond", "No")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Check your answers")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_390_RespondToAllegationsConfirmAnswers") {
		exec(http("PRL_C100Respondent_390_005_RespondToAllegationsConfirmAnswers")
		.post(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_400_StartInternationalFactors") {
		exec(http("PRL_C100Respondent_400_005_StartInternationalFactors")
		.get(prlURL + "/tasklistresponse/international-factors/start/#{caseId}")
		.headers(Headers.navigationHeader)
		.check(CsrfCheck.save)
		.check(substring("lives mainly based outside of England and Wales")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_410_ChildrensLivesBasedOutsideUK") {
		exec(http("PRL_C100Respondent_410_005_ChildrensLivesBasedOutsideUK")
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

	.group("PRL_C100Respondent_420_ChildrensParentsBasedOutsideUK") {
		exec(http("PRL_C100Respondent_420_005_ChildrensParentsBasedOutsideUK")
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

	.group("PRL_C100Respondent_430_OtherPersonOutsideUK") {
		exec(http("PRL_C100Respondent_430_005_OtherPersonOutsideUK")
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

	.group("PRL_C100Respondent_440_OtherCountryAskedForInfo") {
		exec(http("PRL_C100Respondent_440_005_OtherCountryAskedForInfo")
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

	.group("PRL_C100Respondent_450_InternationalFactorsConfirmAnswers") {
		exec(http("PRL_C100Respondent_450_005_InternationalFactorsConfirmAnswers")
		.post(prlURL + "/tasklistresponse/international-factors/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Your response will be shared with the other people in this case")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_460_ReviewAndSubmit") {
		exec(http("PRL_C100Respondent_460_005_ReviewAndSubmit")
		.post(prlURL + "/tasklistresponse/start")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Please review your answers before you complete your response")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_470_CheckYourAnswers") {
		exec(http("PRL_C100Respondent_470_005_CheckYourAnswers")
		.post(prlURL + "/tasklistresponse/summary")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("declarationCheck", "")
		.formParam("declarationCheck", "declaration")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Response submitted successfully")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_480_SubmitYourResponse") {
		exec(http("PRL_C100Respondent_480_005_SubmitYourResponse")
		.post(prlURL + "/tasklistresponse/summary-confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("Respond to an application about a child")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_C100Respondent_490_ReturnToSummary") {
		exec(http("PRL_C100Respondent_490_005_ReturnToSummary")
		.post(prlURL + "/tasklistresponse/summary-confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("saveAndContinue", "true")
		.check(substring("Respond to an application about a child")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Check application Link
	======================================================================================*/

  val CheckApplication =

    exec(http("PRL_C100Respondent_500_CheckApplication")
      .get(prlURL + "/respondent/documents/download/type/cada-document")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Check the allegations of harm and violence (PDF)
	======================================================================================*/

  val CheckHarmViolenceAllegations =

    exec(http("PRL_C100Respondent_510_HarmAndViolenceDoc")
      .get(prlURL + "/respondent/documents/download/type/aoh-document/en")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Make a request to the court about your case --> Click Link
	======================================================================================*/

  val MakeRequestToCourtAboutCase =  // ** NEW FUNCTIONALITY FOR PRL R7.0 (Out of scope for R6.0)

    exec(http("PRL_C100Respondent_520_MakeRequestToCourtAboutCase")
	  .get(prlURL + "/respondent/application-within-proceedings/list-of-applications/1")
	  .headers(Headers.navigationHeader)
	  .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
	  .check(substring("Make a request to the court about your case")))

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select View All Documents Link
	======================================================================================*/

  val ViewAllDocuments =

    exec(http("PRL_C100Respondent_510_CheckApplication")
      .get(prlURL + "/respondent/documents/view/all-categories")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(substring("View all documents")))

    .pause(MinThinkTime, MaxThinkTime)


	/*======================================================================================
	* Select View All Documents Link
	======================================================================================*/

  val UploadDocumentsApplicationsStatements =

    exec(http("PRL_C100Respondent_510_DocumentsUpload")
      .get(prlURL + "/respondent/documents/upload")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("Select the type of document")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Your Position Statement Link
	======================================================================================*/

    .exec(http("PRL_C100Respondent_520_YourPositionStatement")
      .get(prlURL + "/respondent/documents/upload/your-position-statements/has-the-court-asked-for-this-documents")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
      .check(substring("Your position statement")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Your Position Statement Link - Has the court asked for this document? --> Yes, Continue
	======================================================================================*/

	.group("PRL_C100Respondent_530_HasCourtAskedForDocumentYes") {
       exec(http("PRL_C100Respondent_530_005_HasCourtAskedForDocumentYes")
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

	.group("PRL_C100Respondent_540_DocumentSharingDetails") {
       exec(http("PRL_C100Respondent_540_005_DocumentSharingDetails")
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

	.group("PRL_C100Respondent_550_SharingDocumentsNo") {
       exec(http("PRL_C100Respondent_550_005_SharingDocumentsNo")
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

	.group("PRL_C100Respondent_560_WitnessStatementSubmit") {
       exec(http("PRL_C100Respondent_560_005_WitnessStatementSubmit")
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

	.group("PRL_C100Respondent_570_UploadDocumentContinue") {
       exec(http("PRL_C100Respondent_570_005_UploadDocumentContinue")
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
	* Select upload another document link
	======================================================================================*/

	.group("PRL_C100Respondent_580_UploadDocumentContinue") {
       exec(http("PRL_C100Respondent_580_005_UploadDocumentContinue")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/upload-documents-success?_csrf=#{csrf}")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("returnToUploadDoc", "true")
      .check(substring("Select the type of document")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Select Emails, screenshots, images and other media files link (media files)
	======================================================================================*/

 	.exec(http("PRL_C100Respondent_590_SelectMediaFilesLink")
      .get(prlURL + "/respondent/documents/upload/media-files/has-the-court-asked-for-this-documents")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .check(CsrfCheck.save)
      .check(substring("Emails, screenshots, images")))

    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Your Position Statement Link - Has the court asked for this document? --> Yes, Continue
	======================================================================================*/

	.group("PRL_C100Respondent_600_HasCourtAskedForDocumentYes") {
       exec(http("PRL_C100Respondent_600_005_HasCourtAskedForDocumentYes")
      .post(prlURL + "/respondent/documents/upload/media-files/has-the-court-asked-for-this-documents")
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

	.group("PRL_C100Respondent_610_DocumentSharingDetails") {
       exec(http("PRL_C100Respondent_610_005_DocumentSharingDetails")
      .post(prlURL + "/respondent/documents/upload/media-files/document-sharing-details")
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

	.group("PRL_C100Respondent_620_SharingDocumentsNo") {
       exec(http("PRL_C100Respondent_620_005_SharingDocumentsNo")
      .post(prlURL + "/respondent/documents/upload/media-files/sharing-your-documents")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .formParam("haveReasonForDocNotToBeShared", "No")
	  .formParam("saveAndContinue", "true")
	  .check(CsrfCheck.save)
      .check(substring("Emails, screenshots, images")))
	}

    .pause(MinThinkTime, MaxThinkTime)	

	/*======================================================================================
	* Witness statements and evidence, select choose file button, select file and then upload
	======================================================================================*/

	.group("PRL_C100Respondent_630_UploadDocument") {
       exec(http("PRL_C100Respondent_630_005_UploadDocument")
      .post(prlURL + "/respondent/documents/upload/media-files/upload-your-documents?docCategory=media-files&_csrf={#csrf}")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("_csrf", "#{csrf}")
	  .bodyPart(RawFileBodyPart("documents", "PRL-ScreenShot-172KB.jpg"))
	  .contentType("image/jpeg")
	  .fileName("PRL-ScreenShot-172KB.jpg")
	  .formParam("docCategory", "media-files")
	  .check(CsrfCheck.save)
      .check(substring("Remove")))
    }
	
    .pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Witness statements and evidence, Select checkbox declaratiom -> Continue
	======================================================================================*/

	.group("PRL_C100Respondent_640_UploadDocumentContinue") {
       exec(http("PRL_C100Respondent_640_005_UploadDocumentContinue")
      .post(prlURL + "/respondent/documents/upload/media-files/upload-your-documents")
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

	.group("PRL_C100Respondent_580_ReturnToCaseOverview") {
       exec(http("PRL_C100Respondent_580_005_UploadDocumentContinue")
      .post(prlURL + "/respondent/documents/upload/your-position-statements/upload-documents-success?_csrf=#{csrf}")
      .headers(Headers.navigationHeader)
	  .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
	  .formParam("returnToCaseView", "true")
      .check(substring("Select the type of document")))
	}

    .pause(MinThinkTime, MaxThinkTime)





	// write cases for use in Add RA script
	.exec { session =>
	val fw = new BufferedWriter(new FileWriter("AddRAData.csv", true))
	try {
		fw.write(session("user").as[String] + "," + session("password").as[String] + "," + session("caseId").as[String] + "," + "C100" + "\r\n")
	} finally fw.close()
	session
	} 

}