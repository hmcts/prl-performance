package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.http.Predef._
import scenarios._
import utils._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class PRL_Simulation extends Simulation {
  
  val UserFeederPRL = csv("UserDataPRL.csv").circular
  val UserCitizenPRL = csv("UserDataPRLCitizen.csv").circular
  val UserCourtAdminPRL = csv("UserDataCourtAdmin.csv").circular
  val UserCaseManagerPRL = csv("UserDataCaseManager.csv")
  val UserFeederPRLRespondent = csv("UserDataRespondent.csv").circular
  val fl401caseFeeder = csv("FL401CourtAdminData.csv")
  val c100CaseFeeder = csv("C100CourtAdminData.csv")
  val c100RespondentData = csv("C100RespondentData.csv")
  val fl401RespondentData = csv("FL401RespondentData.csv")
  val RAData_Add = csv("ReasonableAdjustments_Add.csv")
  val RAData_Modify = csv("ReasonableAdjustments_Modify.csv").circular

  val WaitTime = Environment.waitTime
  
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
  val prlTargetPerHour: Double = 30
  val caseworkerTargetPerHour: Double = 30
  //val c100AppTargetPerHour: Double = 62
  //val defaultTargetPerHour: Double = 10

  // *** Smoke test config ****
  val c100AppTargetPerHour: Double = 31
  val defaultTargetPerHour: Double = 5
  
  //This determines the percentage split of PRL journeys, by C100 or FL401
  val prlC100Percentage = 100 //Percentage of C100s (the rest will be FL401s) - should be 66 for the 2:1 ratio
  
  val rampUpDurationMins = 5
  val rampDownDurationMins = 5
  val testDurationMins = 60 //60
  
  val numberOfPipelineUsers = 5
  val pipelinePausesMillis: Long = 3000 //3 seconds
  
  //Determine the pause pattern to use:
  //Performance test = use the pauses defined in the scripts
  //Pipeline = override pauses in the script with a fixed value (pipelinePauseMillis)
  //Debug mode = disable all pauses
  val pauseOption: PauseType = debugMode match {
    case "off" if testType == "perftest" => constantPauses
    case "off" if testType == "pipeline" => customPauses(pipelinePausesMillis)
    case _ => customPauses(pipelinePausesMillis) //disabledPauses
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
* PRL Citizen Journey - Create C100 Case
===============================================================================================*/

  val PRLC100CitizenScenario = scenario("***** PRL Citizen Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCitizenPRL)
      .repeat(1) {
        exec(Citizen_PRL_C100_Applicant.C100Case)
        .exec(Citizen_PRL_C100_Applicant.C100Case2)
      }
    }

/*===============================================================================================
* PRL Caseworker Journey - Progress C100 Case for Respondent
===============================================================================================*/

  val PRLC100CaseworkerScenario = scenario("***** PRL C100 Caseworker Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCourtAdminPRL)
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
      .repeat(1) {
        feed(c100CaseFeeder)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminCheckApplication)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminSendToGateKeeper)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminManageOrders)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminServiceApplication)
      }
    }

/*===============================================================================================
* PRL Caseworker Journey - Progress FL401 Case
===============================================================================================*/

  val PRLFL401CaseworkerScenario = scenario("***** PRL FL401 Caseworker Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCourtAdminPRL)
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
      .repeat(1) {
        feed(fl401caseFeeder)
        .exec(Caseworker_PRL_FL401_ProgressCase.CourtAdminCheckApplication)
        .exec(Caseworker_PRL_FL401_ProgressCase.CourtAdminSendToGateKeeper)
        .exec(Caseworker_PRL_FL401_ProgressCase.CourtAdminManageOrders)
        .exec(Caseworker_PRL_FL401_ProgressCase.CourtAdminServiceApplication)
      }
    }

/*===============================================================================================
* PRL CaseManager Journey - Progress FL401 Case
===============================================================================================*/

  val PRLFL401CaseManagerScenario = scenario("***** PRL FL401 CaseManager Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCaseManagerPRL)
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
      .repeat(1) {
        feed(fl401caseFeeder)
        .exec(CaseManager_PRL_FL401_ProgressCase.CaseManagerConfidentialityCheck)
      }
    }

/*===============================================================================================
* PRL Citizen Journey
===============================================================================================*/

  val PrlDataPrep = scenario("***** PRL Case DataPrep Journey *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
      .exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
      .repeat(1) {
          exec(Solicitor_PRL_CitizenDataPrep.CompleteDataPrep)
      }
    }

  /*===============================================================================================
  * PRL Solicitor FL401 Create
  ===============================================================================================*/

    val PrlFL401Create = scenario("***** PRL FL401 DataPrep *****")
      .exitBlockOnFail {
        feed(UserFeederPRL)
        .exec(_.set("env", s"${env}")
        .set("caseType", "PRLAPPS"))
        .exec(Homepage.XUIHomePage)
        .exec(Login.XUILogin)
        .repeat(1) {
         exec(Solicitor_PRL_FL401_CaseCreate.CreatePrivateLawCase)
        .exec(Solicitor_PRL_FL401_CaseCreate.TypeOfApplication)
        .exec(Solicitor_PRL_FL401_CaseCreate.WithoutNoticeOrder)
        .exec(Solicitor_PRL_FL401_CaseCreate.ApplicantDetails)
        .exec(Solicitor_PRL_FL401_CaseCreate.RespondentDetails)
        .exec(Solicitor_PRL_FL401_CaseCreate.ApplicantsFamily)
        .exec(Solicitor_PRL_FL401_CaseCreate.Relationship)
        .exec(Solicitor_PRL_FL401_CaseCreate.Behaviour)
        .exec(Solicitor_PRL_FL401_CaseCreate.TheHome)
        .exec(Solicitor_PRL_FL401_CaseCreate.UploadDocuments)
        .exec(Solicitor_PRL_FL401_CaseCreate.ViewPDF)
        .exec(Solicitor_PRL_FL401_CaseCreate.StatementOfTruth)
        }
      }

  /*===============================================================================================
  * PRL Citizen C100 Respondent Journey 
  ===============================================================================================*/
  
  val PRLC100RespondentScenario = scenario("***** PRL Citizen C100 Respondent Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserFeederPRLRespondent)
      .exec(Homepage.PRLHomePage)
      .exec(Login.PrlLogin)
      .repeat(1) {
        feed(c100RespondentData)
        .exec(Citizen_PRL_C100_Respondent.RetrieveCase)
        .exec(Citizen_PRL_C100_Respondent.GetCase)
        .exec(Citizen_PRL_C100_Respondent.KeepDetailsPrivate)
        .exec(Citizen_PRL_C100_Respondent.ContactPreferences)
        .exec(Citizen_PRL_C100_Respondent.SupportYouNeed)
        .exec(Citizen_PRL_C100_Respondent.RespondToApplication)
        .exec(Citizen_PRL_C100_Respondent.CheckApplication)
        .exec(Logout.CUILogout)
      }
    }

  /*===============================================================================================
  * PRL Citizen Reasonable Adjustments Journey - Add 
  ===============================================================================================*/
  
  val PRLReasonableAdjustmentsAdd = scenario("***** PRL Citizen Reasonable Adjustments Journey - Add *****")
    .exitBlockOnFail {
     repeat(1) {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(RAData_Add)
      .exec(Homepage.PRLHomePage)
      .exec(Login.PrlLogin)
        .exec(Citizen_ReasonableAdjustments.GetCase)
        .exec(Citizen_ReasonableAdjustments.ReasonableAdjustmentsAdd)
      .exec(Logout.CUILogout)
      }
    }

  /*===============================================================================================
  * PRL Citizen Reasonable Adjustments Journey - Modify 
  ===============================================================================================*/
  
  val PRLReasonableAdjustmentsModify = scenario("***** PRL Citizen Reasonable Adjustments Journey - Modify *****")
    .exitBlockOnFail {
     repeat(1) {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(RAData_Modify)
      .exec(Homepage.PRLHomePage)
      .exec(Login.PrlLogin)
        .exec(Citizen_ReasonableAdjustments.GetCase)
        .exec(Citizen_ReasonableAdjustments.ReasonableAdjustmentsModify)
      }
      .exec(Logout.CUILogout)
    }

  /*===============================================================================================
  * PRL Citizen FL401 Respondent Journey 
  ===============================================================================================*/
  
  val PRLFL401RespondentScenario = scenario("***** PRL Citizen FL401 Respondent Journey *****")
    .exitBlockOnFail {
      repeat(1) {
       exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserFeederPRLRespondent)
      .exec(Homepage.PRLHomePage)
      .exec(Login.PrlLogin)
      .repeat(1) {
        feed(fl401RespondentData)
        .exec(Citizen_PRL_FL401_Respondent.RetrieveCase)
        .exec(Citizen_PRL_FL401_Respondent.GetCase)
        .exec(Citizen_PRL_FL401_Respondent.KeepDetailsPrivate)
        .exec(Citizen_PRL_FL401_Respondent.ContactDetails)
        .exec(Citizen_PRL_FL401_Respondent.SupportYouNeed)
        .exec(Citizen_PRL_FL401_Respondent.CheckApplication)
      }
    }
  }

  /*===============================================================================================
  * Simulation Configuration
   ===============================================================================================*/
  
  def simulationProfile (simulationType: String, userPerHourRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
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
  def assertions (simulationType: String): Seq[Assertion] = {
    simulationType match {
      case "perftest" | "pipeline" => //currently using the same assertions for a performance test and the pipeline
        if (debugMode == "off") {
          Seq(global.successfulRequests.percent.gte(95))
            //details("XUI_PRL_045_ConfirmPayment").successfulRequests.percent.gte(80),
            //details("PRL_CitizenC100_813_FinalSubmitRedirect3").successfulRequests.percent.gte(80))
        }
        else {
          Seq(global.successfulRequests.percent.is(100))
        }
      case _ =>
        Seq()
    }
  }
  
  setUp(
  //=================================================
  //C100 & CUIRA Release Scenarios
  //=================================================
  PRLC100CitizenScenario.inject(simulationProfile(testType, c100AppTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLC100RespondentScenario.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLFL401RespondentScenario.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLReasonableAdjustmentsAdd.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLReasonableAdjustmentsModify.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),

  //=================================================
  //Case creation/progression Scenarios:
  //=================================================
  //CafcasDownloadByDocScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
  //PRLC100CaseworkerScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  //PRLFL401CaseworkerScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  //PRLFL401CaseManagerScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  //PrlFL401Create.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)

  //=================================================
  //Closed workload model scenarios for DataPrep:
  //=================================================
  //PRLC100RespondentScenario.inject(constantConcurrentUsers(41).during(10)),
   //PRLFL401RespondentScenario.inject(constantConcurrentUsers(41).during(10)),
   //PRLC100CitizenScenario.inject(constantConcurrentUsers(40).during(10)),
   //PRLC100CaseworkerScenario.inject(constantConcurrentUsers(27).during(10)),
   //PRLReasonableAdjustmentsAdd.inject(constantConcurrentUsers(14).during(10)),

  ).protocols(httpProtocol)
    .assertions(assertions(testType))
    .maxDuration(75 minutes)
  
}
