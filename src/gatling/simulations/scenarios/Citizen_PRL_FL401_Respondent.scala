package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Respond to a FL401 case within PRL CUI
======================================================================================*/

object Citizen_PRL_FL401_Respondent {
  
  val PayURL = Environment.payURL
  val prlURL = Environment.prlURL
  val IdamUrl = Environment.idamURL
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular
  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  //========================================================================================
  // Enter Case ID & Pin
  //========================================================================================

  val RetrieveCase =

    exec(http("PRL_FL401Respondent_030_EnterPinPage")
			.get(prlURL + "pin-activation/enter-pin")
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

		.exec(http("PRL_FL401Respondent_040_CaseActivated")
			.post(prlURL + "/pin-activation/case-activated")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("onlyContinue", "true")
      .check(substring("Respond to an application about a child")))

    .pause(MinThinkTime, MaxThinkTime)

  //========================================================================================
  // Open the Case
  //========================================================================================

  val GetCase = 

    exec(http("PRL_FL401Respondent_050_OpenCase")
			.get(prlURL + "/case/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(substring("You should respond within 14 days of receiving the application")))

     .pause(MinThinkTime, MaxThinkTime)


  //========================================================================================
  // Select Keep Details Private
  //========================================================================================   

  val KeepDetailsPrivate = 

    exec(http("PRL_FL401Respondent_060_OpenKeepDetailsPrivate")
			.get(prlURL + "/respondent/keep-details-private/details_known/#{caseId}")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      .check(CsrfCheck.save))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_FL401Respondent_070_SelectDetailsKnown")
			.post(prlURL + "/respondent/keep-details-private/details_known")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("detailsKnown", "yes")
			.formParam("onlyContinue", "true")
      .check(CsrfCheck.save))

		.pause(MinThinkTime, MaxThinkTime)

		.exec(http("PRL_FL401Respondent_080_SelectKnownDetails")
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

		.exec(http("PRL_FL401Respondent_090_ConfirmDetails")
			.post(prlURL + "/respondent/keep-details-private/private_details_not_confirmed")
			.headers(Headers.navigationHeader)
      .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.formParam("_csrf", "#{csrf}")
			.formParam("saveAndContinue", "true"))

    .pause(MinThinkTime, MaxThinkTime)


  // Enter Case ID & Pin
  // Begin required steps

  

  
}