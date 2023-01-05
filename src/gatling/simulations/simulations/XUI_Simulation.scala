package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scenarios._
import utils._

import scala.io.Source
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.pause.PauseType

import scala.concurrent.duration._
import scala.util.Random

class XUI_Simulation extends Simulation {

	val CaseworkerUserFeeder = csv("UserDataCaseworkers.csv").circular
	val UserFeederDivorce = csv("UserDataDivorce.csv").circular
	val UserFeederFPL = csv("UserDataFPL.csv").circular
	val UserFeederFR = csv("UserDataFR.csv").circular
	val UserFeederIAC = csv("UserDataIAC.csv").circular
	val UserFeederNFD = csv("UserDataNFD.csv").circular
	val UserFeederProbate = csv("UserDataProbate.csv").circular
	val UserFeederPRL = csv("UserDataPRL.csv").circular
	val UserFeederPRL2 = csv("UserDataPRL2.csv").circular
	val UserFeederBails = csv("UserDataBails.csv").circular
	val UserFeederBailsHO = csv("UserDataBailsHO.csv").circular
	val UserFeederBailsJudge = csv("UserDataBailsJudge.csv").circular

	//Read in text labels required for each NFD case type - sole and joint case labels are different, so are fed directly into the JSON payload bodies
	val nfdSoleLabelsInitialised = Source.fromResource("bodies/nfd/labels/soleLabelsInitialised.txt").mkString
	val nfdSoleLabelsPopulated = Source.fromResource("bodies/nfd/labels/soleLabelsPopulated.txt").mkString
	val nfdJointLabelsInitialised = Source.fromResource("bodies/nfd/labels/jointLabelsInitialised.txt").mkString
	val nfdJointLabelsPopulated = Source.fromResource("bodies/nfd/labels/jointLabelsPopulated.txt").mkString

	val randomFeeder = Iterator.continually(Map("prl-percentage" -> Random.nextInt(100)))

	/* TEST TYPE DEFINITION */
	/* pipeline = nightly pipeline against the AAT environment (see the Jenkins_nightly file) */
	/* perftest (default) = performance test against the perftest environment */
	val testType = scala.util.Properties.envOrElse("TEST_TYPE", "perftest")

	//set the environment based on the test type
	val environment = testType match {
		case "perftest" => "perftest"
		//TODO: UPDATE PIPELINE TO 'aat' ONCE DATA STRATEGY IS IMPLEMENTED. UNTIL THEN, PIPELINE WILL RUN AGAINST PERFTEST
		case "pipeline" => "perftest"
		case _ => "**INVALID**"
	}

	/* ******************************** */
	/* ADDITIONAL COMMAND LINE ARGUMENT OPTIONS */
	val debugMode = System.getProperty("debug", "off") //runs a single user e.g. ./gradle gatlingRun -Ddebug=on (default: off)
	val env = System.getProperty("env", environment) //manually override the environment aat|perftest e.g. ./gradle gatlingRun -Denv=aat
	/* ******************************** */

	/* PERFORMANCE TEST CONFIGURATION */
	val bailsTargetPerHour: Double = 10
	val prlTargetPerHour: Double = 200
	val probateTargetPerHour: Double = 250
	val iacTargetPerHour: Double = 20
	val fplTargetPerHour: Double = 10
	val divorceTargetPerHour: Double = 240
	val nfdSoleTargetPerHour: Double = 120
	val nfdJointTargetPerHour: Double = 120
	val frTargetPerHour: Double = 100
	val caseworkerTargetPerHour: Double = 1000

	//This determines the percentage split of PRL journeys, by C100 or FL401
	val prlC100Percentage = 100 //Percentage of C100s (the rest will be FL401s) - should be 66 for the 2:1 ratio

	val rampUpDurationMins = 5
	val rampDownDurationMins = 5
	val testDurationMins = 60

	val numberOfPipelineUsers = 5
	val pipelinePausesMillis: Long = 3000 //3 seconds

	//Determine the pause pattern to use:
	//Performance test = use the pauses defined in the scripts
	//Pipeline = override pauses in the script with a fixed value (pipelinePauseMillis)
	//Debug mode = disable all pauses
	val pauseOption: PauseType = debugMode match {
		case "off" if testType == "perftest" => constantPauses
		case "off" if testType == "pipeline" => customPauses(pipelinePausesMillis)
		case _ => disabledPauses
	}

	val httpProtocol = http
		.baseUrl(Environment.baseURL.replace("${env}", s"${env}"))
		.inferHtmlResources()
		.silentResources
		.header("experimental", "true") //used to send through client id, s2s and bearer tokens. Might be temporary

	before {
		println(s"Test Type: ${testType}")
		println(s"Test Environment: ${env}")
		println(s"Debug Mode: ${debugMode}")
	}

	/*===============================================================================================
	* XUI Solicitor Private Law Scenario
 	===============================================================================================*/
	val PRLSolicitorScenario = scenario("***** Private Law Create Case *****")
		.exitBlockOnFail {
	//.repeat(1) {
			feed(UserFeederPRL)
				.exec(_.set("env", s"${env}")
							.set("caseType", "PRLAPPS"))
			//	.exec(Homepage.XUIHomePage)
		//		.exec(Login.XUILogin)
				.feed(randomFeeder)
				.doIfOrElse(session => session("prl-percentage").as[Int] < prlC100Percentage) {
					//		repeat(10) {
					//C100 Journey

				/*		exec(Solicitor_PRL_C100.CreatePrivateLawCase)
							.exec(Solicitor_PRL_C100.TypeOfApplication)
							.exec(Solicitor_PRL_C100.HearingUrgency)
							.exec(Solicitor_PRL_C100.ApplicantDetails)
							.exec(Solicitor_PRL_C100.ChildDetails)
							.exec(Solicitor_PRL_C100.RespondentDetails)
							.exec(Solicitor_PRL_C100.MIAM)
							.exec(Solicitor_PRL_C100.AllegationsOfHarm)
							.exec(Solicitor_PRL_C100.ViewPdfApplication)
							.exec(Solicitor_PRL_C100.SubmitAndPay)

				 */




					exec(Solicitor_PRL_C100_Citizen.C100Case)


				//	exec(Solicitor_PRL_AddAnOrder.AddAnOrder)
				//			.exec(Solicitor_PRL_Continued.PRL)







				} 
        {
					//FL401 Journey
					exec(Solicitor_PRL_FL401.CreatePrivateLawCase)
					.exec(Solicitor_PRL_FL401.TypeOfApplication)
					.exec(Solicitor_PRL_FL401.WithoutNoticeOrder)
					.exec(Solicitor_PRL_FL401.ApplicantDetails)
					.exec(Solicitor_PRL_FL401.RespondentDetails)
					.exec(Solicitor_PRL_FL401.ApplicantsFamily)
					.exec(Solicitor_PRL_FL401.Relationship)
					.exec(Solicitor_PRL_FL401.Behaviour)
					.exec(Solicitor_PRL_FL401.TheHome)
					.exec(Solicitor_PRL_FL401.UploadDocuments)
					.exec(Solicitor_PRL_FL401.ViewPDF)
					.exec(Solicitor_PRL_FL401.StatementOfTruth)
				 }

				.exec(Logout.XUILogout)

		}



	/*===============================================================================================
	* XUI Legal Rep Bails Scenario
 	===============================================================================================*/
	val BailsScenario = scenario("***** Bails Create Application *****")
		.exitBlockOnFail {
			feed(UserFeederBails)
				.exec(_.set("env", s"${env}")
					.set("caseType", "Bail"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
					.exec(Solicitor_Bails.CreateBailApplication)
					.exec(Solicitor_Bails.SubmitBailApplication)
				.exec(Logout.XUILogout)
				.feed(UserFeederBailsHO)
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
					.exec(Solicitor_Bails.UploadBailSummary)
				.exec(Logout.XUILogout)
				.feed(UserFeederBailsJudge)
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
					.exec(Solicitor_Bails.RecordBailDecision)
					.exec(Solicitor_Bails.UploadSignedDecision)
				.exec(Logout.XUILogout)
		}

	/*===============================================================================================
	* XUI Solicitor Probate Scenario
	 ===============================================================================================*/
	val ProbateSolicitorScenario = scenario("***** Probate Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederProbate)
				.exec(_.set("env", s"${env}")
							.set("caseType", "GrantOfRepresentation"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.repeat(2) {
					exec(Solicitor_Probate.CreateProbateCase)
					.exec(Solicitor_Probate.AddDeceasedDetails)
					.exec(Solicitor_Probate.AddApplicationDetails)
					.exec(Solicitor_Probate.ReviewAndSubmitApplication)
				}
				.exec(Logout.XUILogout)
		}

	/*===============================================================================================
	* XUI Solicitor IAC Scenario
	 ===============================================================================================*/
	val ImmigrationAndAsylumSolicitorScenario = scenario("***** IAC Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederIAC)
				.exec(_.set("env", s"${env}")
							.set("caseType", "Asylum"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.repeat(2) {
					exec(Solicitor_IAC.CreateIACCase)
					.exec(Solicitor_IAC.shareacase)
				}
				.exec(Logout.XUILogout)
		}

	/*===============================================================================================
	* XUI Solicitor Divorce Scenario
	 ===============================================================================================*/
	val DivorceSolicitorScenario = scenario("***** Divorce Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederDivorce)
				.exec(_.set("env", s"${env}")
							.set("caseType", "DIVORCE"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.repeat(2) {
					exec(Solicitor_Divorce.CreateDivorceCase)
				}
				.exec(Logout.XUILogout)
		}

	/*===============================================================================================
	* XUI Solicitor NFD Scenario (Sole Application)
	 ===============================================================================================*/
	val NoFaultDivorceSolicitorSoleScenario = scenario("***** NFD Create Case (Sole) *****")
		.exitBlockOnFail {
			//feed two rows of data - applicant1's solicitor and applicant2's solicitor
			feed(UserFeederNFD, 2)
				.exec(_.set("env", s"${env}")
							.set("caseType", "NFD")
							.set("nfdCaseType", "sole")
							.set("NFDLabelsInitialised", nfdSoleLabelsInitialised) //sets the initialised labels for JSON bodies
							.set("NFDLabelsPopulated", nfdSoleLabelsPopulated)) //sets the populated labels for JSON bodies
				//Solicitor 1 - Divorce Application
				.exec(Homepage.XUIHomePage)
				//since two records were grabbed, set 'user'/'password' to the first one (applicant1's solicitor) for login
				.exec(session => session.set("user", session("user1").as[String]).set("password", session("password1").as[String]))
				.exec(Login.XUILogin)
				.exec(Solicitor_NFD.CreateNFDCase)
				.exec(Solicitor_NFD.SignAndSubmitSole)
				.exec(Logout.XUILogout)
				//Caseworker - Issue Application
				.exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-issue-application", "bodies/nfd/CWIssueApplication.json"))
				//set 'user'/'password' to the second one (applicant2's solicitor) for assigning the case and login
				.exec(session => session.set("user", session("user2").as[String]).set("password", session("password2").as[String]))
				//Update the case in CCD to assign it to the second solicitor
				.exec(CCDAPI.AssignCase)
				//Solicitor 2 - Respond to Divorce Application
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.exec(Solicitor_NFD.RespondToNFDCase)
				.exec(Logout.XUILogout)
				//Caseworker - Mark the Case as Awaiting Conditional Order (to bypass 20-week holding)
				.exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-held-case", "bodies/nfd/CWAwaitingConditionalOrder.json"))
				//Solicitor 1 - Apply for Conditional Order
				.exec(Homepage.XUIHomePage)
				//since two records were grabbed, set 'user'/'password' to the first one (applicant1's solicitor) for login
				.exec(session => session.set("user", session("user1").as[String]).set("password", session("password1").as[String]))
				.exec(Login.XUILogin)
				.exec(Solicitor_NFD.ApplyForCO)
				.exec(Logout.XUILogout)
				//Legal Advisor - Grant Conditional Order
				.exec(CCDAPI.CreateEvent("Legal", "DIVORCE", "NFD", "legal-advisor-make-decision", "bodies/nfd/LAMakeDecision.json"))
				//Caseworker - Make Eligible for Final Order
				.exec(
					//link with bulk case
					CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-link-with-bulk-case", "bodies/nfd/CWLinkWithBulkCase.json"),
					//set case hearing and decision dates to a date in the past
					CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-update-case-court-hearing", "bodies/nfd/CWUpdateCaseWithCourtHearing.json"),
					//set judge details, CO granted and issued dates in the past
					CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-amend-case", "bodies/nfd/CWSetCODetails.json"),
					//pronounce case
					CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-pronounce-case", "bodies/nfd/CWPronounceCase.json"),
					//set final order eligibility dates
					CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-amend-case", "bodies/nfd/CWSetFOEligibilityDates.json"),
					//set case as awaiting final order
					CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-case-awaiting-final-order", "bodies/nfd/CWAwaitingFinalOrder.json"))
			//TODO: ADD FINAL ORDER HERE ONCE DEVELOPED
		}

	/*===============================================================================================
	* XUI Solicitor NFD Scenario (Joint Application)
	 ===============================================================================================*/
	val NoFaultDivorceSolicitorJointScenario = scenario("***** NFD Create Case (Joint) *****")
		.exitBlockOnFail {
			//feed two rows of data - applicant1's solicitor and applicant2's solicitor
			feed(UserFeederNFD, 2)
				.exec(_.set("env", s"${env}")
							.set("caseType", "NFD")
							.set("nfdCaseType", "joint")
							.set("NFDLabelsInitialised", nfdJointLabelsInitialised) //sets the initialised labels for JSON bodies
							.set("NFDLabelsPopulated", nfdJointLabelsPopulated)) //sets the populated labels for JSON bodies
				//Solicitor 1 - Divorce Application
				.exec(Homepage.XUIHomePage)
				//since two records were grabbed, set 'user'/'password' to the first one (applicant1's solicitor) for login
				.exec(session => session.set("user", session("user1").as[String]).set("password", session("password1").as[String]))
				.exec(Login.XUILogin)
				.exec(Solicitor_NFD.CreateNFDCase)
				.exec(Solicitor_NFD.JointInviteApplicant2)
				.exec(Logout.XUILogout)
				//set 'user'/'password' to the second one (applicant2's solicitor) for assigning the case and login
				.exec(session => session.set("user", session("user2").as[String]).set("password", session("password2").as[String]))
				//Update the case in CCD to assign it to the second solicitor
				.exec(CCDAPI.AssignCase)
				//Solicitor 2 - Confirm Divorce Application
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.exec(Solicitor_NFD.SubmitJointApplication)
				.exec(Logout.XUILogout)
				//Solicitor 1 - Submit Application
				.exec(Homepage.XUIHomePage)
				//since two records were grabbed, set 'user'/'password' to the first one (applicant1's solicitor) for login
				.exec(session => session.set("user", session("user1").as[String]).set("password", session("password1").as[String]))
				.exec(Login.XUILogin)
				.exec(Solicitor_NFD.SignAndSubmitJoint)
				.exec(Logout.XUILogout)
				//Caseworker - Issue Application
				.exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "caseworker-issue-application", "bodies/nfd/CWIssueApplication.json"))
				//Caseworker - Mark the Case as Awaiting Conditional Order (to bypass 20-week holding)
				.exec(CCDAPI.CreateEvent("Caseworker", "DIVORCE", "NFD", "system-progress-held-case", "bodies/nfd/CWAwaitingConditionalOrder.json"))
			//TODO: ADD CONDITIONAL ORDER HERE ONCE DEVELOPED
			//TODO: ADD FINAL ORDER HERE ONCE DEVELOPED
		}

	/*===============================================================================================
	* XUI Solicitor Financial Remedy (FR) Scenario
	 ===============================================================================================*/
	val FinancialRemedySolicitorScenario = scenario("***** FR Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederFR)
				.exec(_.set("env", s"${env}")
							.set("caseType", "FinancialRemedyMVP2"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.repeat(2) {
					exec(Solicitor_FR.CreateFRCase)
				}
				.exec(Logout.XUILogout)
		}

	/*===============================================================================================
	* XUI Solicitor Family Public Law (FPL) Scenario
	 ===============================================================================================*/
	val FamilyPublicLawSolicitorScenario = scenario("***** FPL Create Case *****")
		.exitBlockOnFail {
			feed(UserFeederFPL)
				.exec(_.set("env", s"${env}")
							.set("caseType", "CARE_SUPERVISION_EPO"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.exec(Solicitor_FPL.CreateFPLCase)
				.exec(Solicitor_FPL.fplOrdersAndDirections)
				.exec(Solicitor_FPL.fplHearingUrgency)
				.exec(Solicitor_FPL.fplGrounds)
				.exec(Solicitor_FPL.fplLocalAuthority)
				.exec(Solicitor_FPL.fplChildDetails)
				.exec(Solicitor_FPL.fplRespondentDetails)
				.exec(Solicitor_FPL.fplAllocationProposal)
				.exec(Solicitor_FPL.fplSubmitApplication)
				.exec(Solicitor_FPL.fplReturnToCase)
				.exec(Logout.XUILogout)
		}

	/*===============================================================================================
	* XUI Caseworker - Search & View Case Scenario
	 ===============================================================================================*/
	val CaseworkerScenario = scenario("***** Caseworker Journey ******")
		.exitBlockOnFail {
			feed(CaseworkerUserFeeder)
				//TODO: UPDATE caseType with something more dynamic
				.exec(_.set("env", s"${env}")
							.set("caseType", "NFD"))
				.exec(Homepage.XUIHomePage)
				.exec(Login.XUILogin)
				.exec(Caseworker_Navigation.ApplyFilter)
				.exec(Caseworker_Navigation.SortByLastModifiedDate)
				.exec(Caseworker_Navigation.LoadPage2)
				//Only continue with the case activities if results were returned
				.doIf(session => session("numberOfResults").as[Int] > 0) {
					exec(Caseworker_Navigation.SearchByCaseNumber)
					.exec(Caseworker_Navigation.ViewCase)
					// .exec(Caseworker_Navigation.NavigateTabs) //Removing as clicking tabs no longer initiates calls
       //   .exec(Caseworker_Navigation.ViewDocument)
				}
				.exec(Caseworker_Navigation.LoadCaseList)
				.exec(Logout.XUILogout)
		}


	/*===============================================================================================
	* Simulation Configuration
	 ===============================================================================================*/

	def simulationProfile(simulationType: String, userPerHourRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
		val userPerSecRate = userPerHourRate / 3600
		simulationType match {
			case "perftest" =>
				if (debugMode == "off") {
					Seq(
						rampUsersPerSec(0.00) to (userPerSecRate) during (rampUpDurationMins minutes),
						constantUsersPerSec(userPerSecRate) during (testDurationMins minutes),
						rampUsersPerSec(userPerSecRate) to (0.00) during (rampDownDurationMins minutes)
					)
				}
				else {
					Seq(atOnceUsers(1))
				}
			case "pipeline" =>
				Seq(rampUsers(numberOfPipelineUsers.toInt) during (2 minutes))
			case _ =>
				Seq(nothingFor(0))
		}
	}

	//defines the test assertions, based on the test type
	def assertions(simulationType: String): Seq[Assertion] = {
		simulationType match {
			case "perftest" | "pipeline" => //currently using the same assertions for a performance test and the pipeline
				if (debugMode == "off") {
					Seq(global.successfulRequests.percent.gte(95),
						details("XUI_PRL_C100_460_SubmitAndPayNow").successfulRequests.percent.gte(80),
						details("XUI_PRL_FL401_490_SOTSubmit").successfulRequests.percent.gte(80),
						details("XUI_Probate_300_ViewCase").successfulRequests.percent.gte(80),
						details("XUI_IAC_310_ShareACaseConfirm").successfulRequests.percent.gte(80),
						details("XUI_FPL_330_ReturnToCase").successfulRequests.percent.gte(80),
						details("XUI_Divorce_270_SubmitPetition").successfulRequests.percent.gte(80),
						details("XUI_000_CCDEvent-system-progress-case-awaiting-final-order").successfulRequests.percent.gte(80), //NFD Sole
						details("XUI_000_CCDEvent-system-progress-held-case").successfulRequests.percent.gte(80), //NFD Joint
						details("XUI_FR_170_SubmitApplication").successfulRequests.percent.gte(80),
						details("XUI_Caseworker_100_CaseList").successfulRequests.percent.gte(80))
				}
				else {
					Seq(global.successfulRequests.percent.is(100))
				}
			case _ =>
				Seq()
		}
	}

	setUp(
		// BailsScenario.inject(simulationProfile(testType, bailsTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 PRLSolicitorScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
			 /*
		 ProbateSolicitorScenario.inject(simulationProfile(testType, probateTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 ImmigrationAndAsylumSolicitorScenario.inject(simulationProfile(testType, iacTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 FamilyPublicLawSolicitorScenario.inject(simulationProfile(testType, fplTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 DivorceSolicitorScenario.inject(simulationProfile(testType, divorceTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 NoFaultDivorceSolicitorSoleScenario.inject(simulationProfile(testType, nfdSoleTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 NoFaultDivorceSolicitorJointScenario.inject(simulationProfile(testType, nfdJointTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 FinancialRemedySolicitorScenario.inject(simulationProfile(testType, frTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
		 CaseworkerScenario.inject(simulationProfile(testType, caseworkerTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)

			  */
	).protocols(httpProtocol)
		.assertions(assertions(testType))
		.maxDuration(75 minutes)


}
