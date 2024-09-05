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

  // Enter Case ID & Pin

  val RetrieveCase =

    exec(http("PRL_C100Respondent_030_EnterPinPage")
			.get(prlURL + "/pin-activation/enter-pin")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Access your case")))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_040_EnterPinAndCase")
			.post(prlURL + "/pin-activation/enter-pin")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("caseCode", "#{caseId}")
			.formParam("accessCode", "#{accessCode}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_040_CaseActivated")
			.post(prlURL + "/pin-activation/case-activated")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)

  // Begin required steps

  val GetCase = 

    exec(http("PRL_C100Respondent_050_OpenCase")
			.get(prlURL + "/case/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("You should respond within 14 days of receiving the application")))

     .pause(MinThinkTime, MaxThinkTime)

  val KeepDetailsPrivate = 

    exec(http("PRL_C100Respondent_060_OpenKeepDetailsPrivate")
			.get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Do the other people named in this application")))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_070_SelectDetailsKnown")
			.post(prlURL + "/respondent/keep-details-private/details_known")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("detailsKnown", "yes")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Do you want to keep your contact details private from the other people named in the application")))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_080_SelectKnownDetails")
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

		.exec(http("PRL_C100Respondent_090_ConfirmDetails")
			.post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)
      
  val ContactPreferences = 

    exec(http("PRL_C100Respondent_100_OpenContactPreferences")
			.get(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("You can choose to receive case updates by email or post")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_110_ChooseContactPreference")
			.post(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("partyContactPreference", "email")
			.formParam("onlycontinue", "true")
      .check(CsrfCheck.save)
      .check(substring("You have decided to receive updates by email")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_120_ChooseContactPreferenceReview")
			.post(prlURL + "/respondent/contact-preference/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("You will receive digital updates about the case.")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_130_ConfirmContactPreference")
			.post(prlURL + "/respondent/contact-preference/confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)
  
  val SupportYouNeed =

    exec(http("PRL_C100Respondent_140_ReasonableAdjustmentsIntro")
			.get(prlURL + "/respondent/reasonable-adjustments/intro")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Some people need support during their case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_150_ReasonableAdjustmentsStart")
			.post(prlURL + "/respondent/reasonable-adjustments/intro")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Think about all communication with the court")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_160_LanguageRequirements")
			.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("ra_languageReqAndSpecialArrangements", "")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("We know some people need support to access information and use our services")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_170_NoDisabilities")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "none")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("Review the support")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_180_ReviewSupport")
			.post(cuiRaURL + "/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("You have submitted your request to the court")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_190_ConfirmSupport")
			.post(prlURL + "/respondent/reasonable-adjustments/confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)

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

		.exec(http("PRL_C100Respondent_220_NoLegalRepresentation")
			.post(prlURL + "/tasklistresponse/legalrepresentation/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("legalRepresentation", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Complete your response")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_230_ConfirmNoLegalRepresentation")
			.post(prlURL + "/tasklistresponse/legalrepresentation/solicitornotdirect")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_240_DoYouConsentToApplication")
			.get(prlURL + "/tasklistresponse/consent-to-application/consent/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Your understanding of the application")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_250_CheckYourConsent")
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
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_260_ConfirmYourConsent")
			.post(prlURL + "/tasklistresponse/consent-to-application/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_270_HaveYouAttendedMIAM")
			.get(prlURL + "/tasklistresponse/miam/miam-start/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Have you attended a Mediation Information and Assessment Meeting")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_280_WillingToAttendMIAM")
			.post(prlURL + "/tasklistresponse/miam/miam-start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("miamStart", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Would you be willing to attend a MIAM")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_290_MIAMCheckYourAnswers")
			.post(prlURL + "/tasklistresponse/miam/willingness-to-attend-miam")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("miamWillingness", "Yes")
			.formParam("miamNotWillingExplnation", "Perf testing")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Mediation Information and Assessment Meeting (MIAM) attendance")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_300_MIAMConfirmAnswers")
			.post(prlURL + "/tasklistresponse/miam/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_310_EverBeenInvolvedInProceedings")
			.get(prlURL + "/tasklistresponse/proceedings/start/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Have you or the children ever been involved in court proceedings")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_320_PreviousProceedingsCheckYourAnswers")
			.post(prlURL + "/tasklistresponse/proceedings/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("proceedingsStart", "No")
			.formParam("proceedingsStartOrder", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Current or previous proceedings")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_330_PreviousProceedingsConfirmAnswers")
			.post(prlURL + "/tasklistresponse/proceedings/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_340_SafetyConcerns")
			.post(prlURL + "/respondent/safety-concerns/concern-guidance")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Do you have any concerns for your safety or the safety of the children")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_350_AnySafetyConcerns")
			.post(prlURL + "/respondent/safety-concerns/concerns-for-safety")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("c1A_haveSafetyConcerns", "No")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Please review your answers before you finish your application")))

    .pause(MinThinkTime, MaxThinkTime)
      
    .exec(http("PRL_C100Respondent_360_SafetyConcernsConfirmAnswers")
			.post(prlURL + "/respondent/safety-concerns/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_370_RespondToAllegations")
			.get(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Do you wish to respond to the applicant")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_380_RespondToAllegationsCheckYourAnswers")
			.post(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("aoh_wishToRespond", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Check your answers")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_390_RespondToAllegationsConfirmAnswers")
			.post(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_400_StartInternationalFactors")
			.get(prlURL + "/tasklistresponse/international-factors/start/#{caseId}")
			.headers(Headers.navigationHeader)
      .check(CsrfCheck.save)
      .check(substring("lives mainly based outside of England and Wales")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_410_ChildrensLivesBasedOutsideUK")
			.post(prlURL + "/tasklistresponse/international-factors/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsStartProvideDetails", "")
			.formParam("start", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("parents (or anyone significant to the children) mainly based outside of England and Wales")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_420_ChildrensParentsBasedOutsideUK")
			.post(prlURL + "/tasklistresponse/international-factors/parents")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsParentsProvideDetails", "")
			.formParam("parents", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Could another person in the application apply for a similar order in a country outside England or Wales")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_430_OtherPersonOutsideUK")
			.post(prlURL + "/tasklistresponse/international-factors/jurisdiction")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsJurisdictionProvideDetails", "")
			.formParam("jurisdiction", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Has another country asked (or been asked) for information or help for the children")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_440_OtherCountryAskedForInfo")
			.post(prlURL + "/tasklistresponse/international-factors/request")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsRequestProvideDetails", "")
			.formParam("request", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("International elements")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_450_InternationalFactorsConfirmAnswers")
			.post(prlURL + "/tasklistresponse/international-factors/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Your response will be shared with the other people in this case")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_460_ReviewAndSubmit")
			.post(prlURL + "/tasklistresponse/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Please review your answers before you complete your response")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_470_CheckYourAnswers")
			.post(prlURL + "/tasklistresponse/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("declarationCheck", "")
			.formParam("declarationCheck", "declaration")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Response submitted successfully")))
      
		.pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_C100Respondent_480_SubmitYourResponse")
			.post(prlURL + "/tasklistresponse/summary-confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_C100Respondent_490_ReturnToSummary")
			.post(prlURL + "/tasklistresponse/summary-confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)

  val CheckApplication =

    exec(http("PRL_C100Respondent_500_CheckApplication")
      .get(prlURL + "/respondent/documents/download/type/cada-document")
      .headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(status.is(200)))

    .pause(MinThinkTime, MaxThinkTime)

  val ReasonableAdjustmentsAdd = 

    exec(http("PRL_RA_010_OpenAdditionalSupport")
			.get(prlURL + "/respondent/reasonable-adjustments/intro")
			.headers(Headers.navigationHeader)
      .check(substring("Tell us if your support needs have changed"))
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_020_StartAdditionalSupport")
			.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("ra_languageReqAndSpecialArrangements", "Perf Test - Add Reasonable Adjustments")
			.formParam("onlyContinue", "true")
      .check(substring("Review your language requirements and special arrangements"))
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_RA_030_LaunchReasonableAdjustments")
			.get(prlURL + "/reasonable-adjustments/launch")
			.headers(Headers.navigationHeader)
      .check(substring("Tell us if your support needs have changed"))
      .check(CsrfCheck.save)
      .check(substring("Do you have a physical, mental or learning disability")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_040_BuildingSupport")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0004")
			.formParam("enabled", "PF0001-RA0001-RA0002")
			.formParam("enabled", "PF0001-RA0001-RA0008")
			.formParam("enabled", "PF0001-RA0001-RA0003")
			.formParam("enabled", "PF0001-RA0001-RA0006")
			.formParam("enabled", "PF0001-RA0001-RA0005")
			.formParam("enabled", "PF0001-RA0001-RA0007")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need adjustments to get to, into and around our buildings")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_050_BuildingEntranceSupport")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0004")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0024")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0022")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0025")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0023")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0021")
			.formParam("data[PF0001-RA0001-RA0004-RA0021][flagComment]", "Perf test reasons")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0019")
			.formParam("enabled", "PF0001-RA0001-RA0004-RA0020")
			.formParam("enabled", "PF0001-RA0001-RA0004-OT0001")
			.formParam("data[PF0001-RA0001-RA0004-OT0001][flagComment]", "Perf test support")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need documents in an alternative format")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_060_AlternativeDocumentsFormat")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0002")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0014")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0012")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0010")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0011")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0013")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0015")
			.formParam("enabled", "PF0001-RA0001-RA0002-RA0016")
			.formParam("enabled", "PF0001-RA0001-RA0002-OT0001")
			.formParam("data[PF0001-RA0001-RA0002-OT0001][flagComment]", "Perf test formats")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need help communicating and understanding")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_070_HelpCommunicating")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0008")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0047")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0037")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0009")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0038")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0041")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0040")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0042")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0039")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0046")
			.formParam("enabled", "PF0001-RA0001-RA0008-OT0001")
			.formParam("data[PF0001-RA0001-RA0008-OT0001][flagComment]", "Perf test communicating")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("Hearing Enhancement System")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_080_HearingEnhancements")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0008-RA0009")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0009-RA0043")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0009-RA0045")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0009-RA0044")
			.formParam("enabled", "PF0001-RA0001-RA0008-RA0009-OT0001")
			.formParam("data[PF0001-RA0001-RA0008-RA0009-OT0001][flagComment]", "Perf test hearing enhancements")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("Which Sign Language Interpreter do you need to request")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_090_SignLanguageHelp")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0008-RA0042")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("typeahead", "")
			.formParam("selected", "sign-lps")
			.formParam("enabled", "OT0001")
			.formParam("data[PF0001-RA0001-RA0008-RA0042][subTypeValue]", "Perf testing support")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need help with forms")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_100_HelpWithForms")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0003")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0003-RA0017")
			.formParam("enabled", "PF0001-RA0001-RA0003-RA0018")
			.formParam("enabled", "PF0001-RA0001-RA0003-OT0001")
			.formParam("data[PF0001-RA0001-RA0003-OT0001][flagComment]", "Perf testing guidance")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need something to feel comfortable during my hearing")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_110_HelpToFeelComfortable")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0006")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0006-RA0030")
			.formParam("enabled", "PF0001-RA0001-RA0006-RA0033")
			.formParam("enabled", "PF0001-RA0001-RA0006-RA0031")
			.formParam("enabled", "PF0001-RA0001-RA0006-RA0032")
			.formParam("enabled", "PF0001-RA0001-RA0006-OT0001")
			.formParam("data[PF0001-RA0001-RA0006-OT0001][flagComment]", "Perf testing breaks")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need to bring support with me to a hearing")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_120_BringSupport")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0005")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0005-RA0028")
			.formParam("enabled", "PF0001-RA0001-RA0005-RA0027")
			.formParam("data[PF0001-RA0001-RA0005-RA0027][flagComment]", "Perf test friend")
			.formParam("enabled", "PF0001-RA0001-RA0005-RA0026")
			.formParam("data[PF0001-RA0001-RA0005-RA0026][flagComment]", "Perf test carer")
			.formParam("enabled", "PF0001-RA0001-RA0005-RA0029")
			.formParam("data[PF0001-RA0001-RA0005-RA0029][flagComment]", "Perf test dog")
			.formParam("enabled", "PF0001-RA0001-RA0005-OT0001")
			.formParam("data[PF0001-RA0001-RA0005-OT0001][flagComment]", "Perf test support")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("I need to request a certain type of hearing")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_130_RequestTypeOfHearing")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001-RA0007")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "PF0001-RA0001-RA0007-RA0034")
			.formParam("enabled", "PF0001-RA0001-RA0007-RA0036")
			.formParam("enabled", "PF0001-RA0001-RA0007-RA0035")
			.formParam("enabled", "PF0001-RA0001-RA0007-OT0001")
			.formParam("data[PF0001-RA0001-RA0007-OT0001][flagComment]", "Perf test hearing")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("Review the support you've requested")))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_140_ReviewTheRequestedSupport")
			.post(cuiRaURL + "/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save)
      .check(substring("You have submitted your request to the court")))

    .pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_RA_150_SubmitReasonableAdjustments")
			.post(prlURL + "/respondent/reasonable-adjustments/confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
      .formParam("onlyContinue", "true")
      .check(substring("Your court hearings")))

    .pause(MinThinkTime, MaxThinkTime)

}