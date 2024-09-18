package scenarios

import java.io.{BufferedWriter, FileWriter}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

/*======================================================================================
* Add / Modify Reasonable Adjustments for a Citizen user
======================================================================================*/

object Citizen_ReasonableAdjustments {
 
  val prlURL = Environment.prlURL
  val cuiRaURL = Environment.cuiRaURL
  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

	/*======================================================================================
	* Open Case CUI
	======================================================================================*/

  val GetCase = 

	group("PRL_RA_OpenCase") {
    .exec(http("PRL_RA_OpenCase_005")
	.get(prlURL + "/case/#{caseId}")
	.headers(Headers.navigationHeader)
    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
    .check(substring("Check the application (PDF)")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	/*======================================================================================
	* Add Reasonable Adjustments
	======================================================================================*/

  val ReasonableAdjustmentsAdd = 

	exec(http("PRL_RA_010_OpenAdditionalSupport")
	.get(prlURL + "/respondent/reasonable-adjustments/intro")
	.headers(Headers.navigationHeader)
	.check(substring("Tell us if you need support"))
	.check(CsrfCheck.save))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_020_StartAdditionalSupport") {
		exec(http("PRL_RA_020_005_StartAdditionalSupport")
		.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("ra_languageReqAndSpecialArrangements", "Perf Test - Add Reasonable Adjustments")
		.formParam("onlyContinue", "true")
		.check(substring("Review your language requirements and special arrangements"))
		.check(CsrfCheck.save))
	}

	.pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_RA_030_LaunchReasonableAdjustments")
	 	.get(prlURL + "/reasonable-adjustments/launch")
		.headers(Headers.navigationHeader)
      	.check(substring("Select all that apply to you"))
      	.check(CsrfCheck.save)
      	.check(substring("Do you have a physical, mental or learning disability")))
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_040_BuildingSupport") {
		exec(http("PRL_RA_040_005_BuildingSupport")
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
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_050_BuildingEntranceSupport") {
		exec(http("PRL_RA_050_005_BuildingEntranceSupport")
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
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_060_AlternativeDocumentsFormat") {
		exec(http("PRL_RA_060_005_AlternativeDocumentsFormat")
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
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_070_HelpCommunicating") {
		exec(http("PRL_RA_070_005_HelpCommunicating")
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
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_080_HearingEnhancements") {
		exec(http("PRL_RA_080_005_HearingEnhancements")
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
	}
      
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

	.group("PRL_RA_100_HelpWithForms") {
		exec(http("PRL_RA_100_005HelpWithForms")
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
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_110_HelpToFeelComfortable") {
		exec(http("PRL_RA_110_005_HelpToFeelComfortable")
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
	}
      
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_120_BringSupport") {
		exec(http("PRL_RA_120_005_BringSupport")
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
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_130_RequestTypeOfHearing") {
		exec(http("PRL_RA_130_005_RequestTypeOfHearing")
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
		.check(substring("New support you want to request now")))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_140_ReviewTheRequestedSupport") {
		exec(http("PRL_RA_140_005_ReviewTheRequestedSupport")
		.post(cuiRaURL + "/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.check(CsrfCheck.save)
		.check(substring("You have submitted your request to the court")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_150_SubmitReasonableAdjustments") {
		exec(http("PRL_RA_150_005_SubmitReasonableAdjustments")
		.post(prlURL + "/respondent/reasonable-adjustments/confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(substring("Your court hearings")))
	}

	.pause(MinThinkTime, MaxThinkTime)

	//===============================================
	//Write added RA case to the Modify RA data file
	//===============================================

	.exec { session =>
	val fw = new BufferedWriter(new FileWriter("ModifyRAData.csv", true))
	try {
		fw.write(session("user").as[String] + "," + session("password").as[String] + "," + session("caseId").as[String] + "," + session("caseType").as[String] + "\r\n")
	} finally fw.close()
	session
	} 

	/*======================================================================================
	* Modify Reasonable Adjustments
	======================================================================================*/

  val ReasonableAdjustmentsModify = 

    exec(http("PRL_RA_010_OpenAdditionalSupport")
		.get(prlURL + "/respondent/reasonable-adjustments/intro")
		.headers(Headers.navigationHeader)
		.check(substring("Tell us if your support needs have changed"))
     	 .check(CsrfCheck.save))

    .pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_020_StartAdditionalSupport") {
    	exec(http("PRL_RA_020_005_StartAdditionalSupport")
		.post(prlURL + "/respondent/reasonable-adjustments/intro")
		.headers(Headers.navigationHeader)
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(CsrfCheck.save)
		.check(substring("Language requirements and special arrangements")))
	}

    .pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_030_ReviewSpecialArrangements") {
    	exec(http("PRL_RA_030_005_ReviewSpecialArrangements")
		.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("ra_languageReqAndSpecialArrangements", "Perf Test - Modify Reasonable Adjustments")
		.formParam("onlyContinue", "true")
		.check(substring("Review your language requirements and special arrangements"))
		.check(CsrfCheck.save))
	}
	
	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_040_ReviewTheRequestedSupport") {
		exec(http("PRL_RA_040_005_ReviewTheRequestedSupport")
		.post(prlURL + "/respondent/reasonable-adjustments/language-requirements-and-special-arrangements/review")
		.headers(Headers.navigationHeader)
		.header("Content-type", "application/x-www-form-urlencoded")
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(substring("Support for")))
	}

    .pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_RA_050_RequestRAChanges")
		.get(cuiRaURL + "/home/intro")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(substring("I want to tell you that my support needs have changed")))

    .pause(MinThinkTime, MaxThinkTime)

    .exec(http("PRL_RA_060_SelectRAToModify")
		.get(cuiRaURL + "/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.check(substring("Review the support"))
		.check(regex("""id="remove-(.+?)" class='govuk-link govuk-link--no-visited-state'""").saveAs("reasonableAdjustmentToRemove")))

    .pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_070_RemoveRA") {
		exec(http("PRL_RA_070_005_RemoveRA")
		.get(cuiRaURL + "/review/set-inactive?id=#{reasonableAdjustmentToRemove}")
		.headers(Headers.navigationHeader)
     	.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
      	.check(CsrfCheck.save))
	}

	.pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_080_ConfirmRAModify") {
		exec(http("PRL_RA_080_005_ConfirmRAModify")
		.post(cuiRaURL + "/review")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.check(CsrfCheck.save))
	}

    .pause(MinThinkTime, MaxThinkTime)

	.group("PRL_RA_090_ReturnToCaseView") {
   	 	exec(http("PRL_RA_090_005_ReturnToCaseView")
		.post(prlURL + "/respondent/reasonable-adjustments/confirmation")
		.headers(Headers.navigationHeader)
		.header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
		.formParam("_csrf", "#{csrf}")
		.formParam("onlyContinue", "true")
		.check(substring("Your court hearings")))
	}

    .pause(MinThinkTime, MaxThinkTime)




}