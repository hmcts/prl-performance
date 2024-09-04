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
		.get(prlURL + "pin-activation/enter-pin")
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
      .check(CsrfCheck.save))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_070_SelectDetailsKnown")
			.post(prlURL + "/respondent/keep-details-private/details_known")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("detailsKnown", "yes")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))

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
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_090_ConfirmDetails")
			.post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)
      
  val ContactPreferences = 

    exec(http("PRL_C100Respondent_100_OpenContactPreferences")
			.get(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_110_ChooseContactPreference")
			.post(prlURL + "/respondent/contact-preference/choose-a-contact-preference")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("partyContactPreference", "email")
			.formParam("onlycontinue", "true")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_120_ChooseContactPreferenceReview")
			.post(prlURL + "/respondent/contact-preference/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_130_ConfirmContactPreference")
			.post(prlURL + "/respondent/contact-preference/confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)
  
  val SupportYouNeed =

    exec(http("PRL_C100Respondent_140_ReasonableAdjustmentsIntro")
			.get(prlURL + "/respondent/reasonable-adjustments/intro")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_150_ReasonableAdjustmentsStart")
			.post(prlURL + "/respondent/reasonable-adjustments/intro")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_160_LanguageRequirements")
			.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("ra_languageReqAndSpecialArrangements", "")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_170_NoDisabilities")
			.post(cuiRaURL + "/journey/flags/display/PF0001-RA0001")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("enabled", "none")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_180_ReviewSupport")
			.post(cuiRaURL + "/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
      .check(CsrfCheck.save))
      
		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_C100Respondent_190_ConfirmSupport")
			.post(prlURL + "/respondent/reasonable-adjustments/confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)

  val RespondToApplication = 

    exec(http("request_0")
			.get(prlURL + "/tasklistresponse/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Respond to the application")))
      
		.pause(1)

    .exec(http("request_1")
			.get(prlURL + "/tasklistresponse/legalrepresentation/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      .check(substring("Will you be using a legal representative")))
      
		.pause(1)

		.exec(http("request_12")
			.post(prlURL + "/tasklistresponse/legalrepresentation/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("legalRepresentation", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Complete your response")))
      
		.pause(4)

		.exec(http("request_18")
			.post(prlURL + "/tasklistresponse/legalrepresentation/solicitornotdirect")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      // .check(substring(""))
      )
      
		.pause(2)

		.exec(http("request_24")
			.get(prlURL + "/tasklistresponse/consent-to-application/consent/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save)
      // .check(substring(""))
      )
      
		.pause(13)

		.exec(http("request_30")
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
      // .check(substring(""))
      )
      
		.pause(1)

		.exec(http("request_36")
			.post(prlURL + "/tasklistresponse/consent-to-application/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(6)

		.exec(http("request_42")
			.get(prlURL + "/tasklistresponse/miam/miam-start/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
		.pause(11)

		.exec(http("request_48")
			.post(prlURL + "/tasklistresponse/miam/miam-start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("miamStart", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(4)

		.exec(http("request_54")
			.post(prlURL + "/tasklistresponse/miam/willingness-to-attend-miam")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("miamWillingness", "Yes")
			.formParam("miamNotWillingExplnation", "Perf testing")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(1)

		.exec(http("request_60")
			.post(prlURL + "/tasklistresponse/miam/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(3)

		.exec(http("request_78")
			.get(prlURL + "/tasklistresponse/proceedings/start/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
		.pause(5)

		.exec(http("request_84")
			.post(prlURL + "/tasklistresponse/proceedings/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("proceedingsStart", "No")
			.formParam("proceedingsStartOrder", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(1)

		.exec(http("request_90")
			.post(prlURL + "/tasklistresponse/proceedings/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(8)

		.exec(http("request_102")
			.post(prlURL + "/respondent/safety-concerns/concern-guidance")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(5)

		.exec(http("request_108")
			.post(prlURL + "/respondent/safety-concerns/concerns-for-safety")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("c1A_haveSafetyConcerns", "No")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
    .exec(http("request_114")
			.post(prlURL + "/respondent/safety-concerns/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(3)

		.exec(http("request_120")
			.get(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))
      
		.pause(4)

		.exec(http("request_126")
			.post(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/willing-to-respond")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("aoh_wishToRespond", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(1)

		.exec(http("request_132")
			.post(prlURL + "/respondent/tasklistresponse/respond-to-allegations-of-harm/review")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(2)

		.exec(http("request_138")
			.get(prlURL + "/tasklistresponse/international-factors/start/#{caseId}")
			.headers(Headers.navigationHeader)
      .check(CsrfCheck.save))
      
		.pause(3)

		.exec(http("request_144")
			.post(prlURL + "/tasklistresponse/international-factors/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsStartProvideDetails", "")
			.formParam("start", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(3)

		.exec(http("request_150")
			.post(prlURL + "/tasklistresponse/international-factors/parents")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsParentsProvideDetails", "")
			.formParam("parents", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(3)

		.exec(http("request_156")
			.post(prlURL + "/tasklistresponse/international-factors/jurisdiction")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsJurisdictionProvideDetails", "")
			.formParam("jurisdiction", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(3)

		.exec(http("request_162")
			.post(prlURL + "/tasklistresponse/international-factors/request")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("iFactorsRequestProvideDetails", "")
			.formParam("request", "No")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(1)

		.exec(http("request_168")
			.post(prlURL + "/tasklistresponse/international-factors/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(2)

		.exec(http("request_174")
			.post(prlURL + "/tasklistresponse/start")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))
      
		.pause(5)

		.exec(http("request_180")
			.post(prlURL + "/tasklistresponse/summary")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("declarationCheck", "")
			.formParam("declarationCheck", "declaration")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Response submitted successfully")))
      
		.pause(9)

    .exec(http("request_198")
			.post(prlURL + "/tasklistresponse/summary-confirmation")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)

  val UploadDocuments = 

    exec(http("request_0")
			.get(prlURL + "/respondent/documents/upload")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("Select the type of document")))
      
		.pause(2)

    .exec(http("request_1")
			.get(prlURL + "/respondent/documents/upload/other-documents/has-the-court-asked-for-this-documents")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("Has the court asked for this document?"))
      .check(CsrfCheck.save))
      
		.pause(2)

    .exec(http("request_2")
			.post(prlURL + "/respondent/documents/upload/other-documents/has-the-court-asked-for-this-documents")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
      .formParam("hasCourtAskedForThisDoc", "Yes")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Before you submit a document")))

		.pause(5)

		.exec(http("request_3")
			.post(prlURL + "/respondent/documents/upload/other-documents/document-sharing-details")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Sharing your documents")))

		.pause(5)

		.exec(http("request_4")
			.post(prlURL + "/respondent/documents/upload/other-documents/sharing-your-documents")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("haveReasonForDocNotToBeShared", "No")
			.formParam("saveAndContinue", "true")
      .check(CsrfCheck.save)
      .check(regex("""action="?docCategory=other-documents&_csrf=(.+?)"""").saveAs("docCsrf"))
      .check(substring("Other documents")))

		.pause(19)

		.exec(http("request_5")
			.post(prlURL + "/respondent/documents/upload/other-documents/upload-your-documents?docCategory=other-documents&_csrf=#{docCsrf}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
      .formParam("_csrf", "#{docCsrf}")
      .formParam("docCategory", "other-documents")
      .bodyPart(RawFileBodyPart("documents", "120KB.pdf")
      .contentType("application/pdf")
      .fileName("120KB.pdf")
      .transferEncoding("binary"))
      .check(CsrfCheck.save))

		.pause(6)

		.exec(http("request_6")
			.post(prlURL + "/respondent/documents/upload/other-documents/upload-your-documents")
			.headers(Headers.uploadHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("declarationCheck", "")
			.formParam("declarationCheck", "declaration")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save)
      .check(substring("Document submitted")))

		.pause(4)

		.exec(http("request_7")
			.post(prlURL + "/respondent/documents/upload/other-documents/upload-documents-success?_csrf=#{csrf}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("returnToCaseView", "true")
			.formParam("answers-checked", "true")
      .check(substring("Respond to an application about a child")))

    

}