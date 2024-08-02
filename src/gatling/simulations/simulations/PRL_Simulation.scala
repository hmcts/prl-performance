package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.http.Predef._
import scenarios._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class PRL_Simulation extends Simulation {
  
  val UserFeederPRL = csv("UserDataPRL.csv").circular
  val UserCitizenPRL = csv("UserDataPRLCitizen.csv").circular
  val UserCourtAdminPRL = csv("UserDataCourtAdmin.csv").circular
  val caseFeeder = csv("CourtAdminData.csv")

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
* PRL Citizen Journey
===============================================================================================*/

  val PRLCitizenScenario = scenario("***** PRL Citizen Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCitizenPRL)
      .repeat(1) {
        exec(Citizen_PRL_C100_Applicant.C100Case)
        .exec(Citizen_PRL_C100_Respondent.C100Case2)
      }
    }

  val PRLCaseworkerScenario = scenario("***** PRL Caseworker Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCourtAdminPRL)
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
      .repeat(1) {
        feed(caseFeeder)
        // .exec(Solicitor_PRL_C100_ProgressCase.CourtAdminCheckApplication)
        .exec(Solicitor_PRL_C100_ProgressCase.CourtAdminSendToGateKeeper)
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
          Seq(global.successfulRequests.percent.gte(95),
            details("XUI_PRL_045_ConfirmPayment").successfulRequests.percent.gte(80),
            details("PRL_CitizenC100_813_FinalSubmitRedirect3").successfulRequests.percent.gte(80))
        }
        else {
          Seq(global.successfulRequests.percent.is(100))
        }
      case _ =>
        Seq()
    }
  }
  
  setUp(
    PRLCitizenScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  //  CafcasDownloadByDocScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
  // PRLCaseworkerScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
  ).protocols(httpProtocol)
    .assertions(assertions(testType))
    .maxDuration(75 minutes)
  
  
}
