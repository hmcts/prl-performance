package simulations

import io.gatling.commons.stats.assertion.Assertion
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.pause.PauseType
import io.gatling.http.Predef._
import scenarios.CafcasAPI.prlCafcasURL
import scenarios._

import scala.concurrent.duration._
import scala.util.Random

class PRL_Simulation extends Simulation {
  
  val UserFeederPRL = csv("UserDataPRL.csv").circular
  val UserFeederPRLca = csv("UserDataPRLca.csv").circular
  val UserFeederPRL2 = csv("UserDataPRL2.csv").circular
  val UserCitizenPRL = csv("UserDataPRLCitizen.csv").circular
  val UserSSCS = csv("UserDataSSCS.csv").circular
  val casesFeeder = csv("caseFlagsCases.csv").circular
  
  
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
  val prlTargetPerHour: Double = 36
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
    .baseUrl(prlCafcasURL.replace("${env}", s"${env}"))
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

    //  .repeat(1) {
    .exitBlockOnFail {
      feed(UserFeederPRL)
    //    feed(UserCitizenPRL)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(Homepage.XUIHomePage)
        .exec(Login.XUILogin)
        .feed(randomFeeder)
//        .doIfOrElse(session => session("prl-percentage").as[Int] < prlC100Percentage) {
          .repeat(1) {
            exitBlockOnFail {
              //C100 Journey

             		exec(Solicitor_PRL_C100.CreatePrivateLawCase)
                .exec(Solicitor_PRL_C100.TypeOfApplication)
                .exec(Solicitor_PRL_C100.HearingUrgency)
                .exec(Solicitor_PRL_C100.ApplicantDetails)
                .exec(Solicitor_PRL_C100.ChildDetails)
                .exec(Solicitor_PRL_C100.RespondentDetails)
                .exec(Solicitor_PRL_C100.MIAM)
            //    .exec(Solicitor_PRL_C100.AllegationsOfHarm)
                .exec(Solicitor_PRL_C100.ViewPdfApplication)

                    .exec(Solicitor_PRL_C100.OtherPeopleInTheCase)
                    .exec(Solicitor_PRL_C100.OtherChildrenNotInTheCase)
                    .exec(Solicitor_PRL_C100.ChildrenAndApplicants)
                    .exec(Solicitor_PRL_C100.ChildrenAndRespondents)
                    .exec(Solicitor_PRL_C100.ChildrenAndOtherPeople)


                .exec(Solicitor_PRL_C100.SubmitAndPay)







        //            exec(Solicitor_PRL_Citizen_Dashboard.DashBoard)
         //     exec(Solicitor_PRL_CitizenDataPrep.CompleteDataPrep)

             //     exec(Solicitor_PRL_AddAnOrder.AddAnOrder)


       //          .exec(Solicitor_PRL_C100_Citizen.C100Case)
       //           .exec(Solicitor_PRL_C100_Citizen2.C100Case2)

                   .exec {
                     session =>
                       println(session)
                       session
                   }


              //	exec(Solicitor_PRL_AddAnOrder.AddAnOrder)
              //			.exec(Solicitor_PRL_Continued.PRL)


            }
          }/* {
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
          */

         //   .exec(Logout.XUILogout)

    }
    .exec {
      session =>
        println(session)
        session
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
          exec(Solicitor_PRL_C100_Citizen.C100Case)
          .exec(Solicitor_PRL_C100_Citizen2.C100Case2)
        }
    }


  /*===============================================================================================
* PRL Citizen Journey
===============================================================================================*/

  val SSCSscenario = scenario("***** SSCS Hearing *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
        .set("caseType", "SSCS"))
        .feed(UserSSCS)
      .exec(Homepage.XUIHomePage)
        .exec(Login.XUILogin)
        .repeat(2) {
          exec(SSCShearing.SendToHearing)
        }
    }



  /*===============================================================================================
* PRL Citizen Journey
===============================================================================================*/

  val PrlHearingDataPrep = scenario("***** PRL Citizen Journey *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
      .exec(_.set("env", s"${env}")
        .set("caseType", "PRLAPPS"))
        .exec(Homepage.XUIHomePage)
        .exec(Login.XUILogin)
        //when doing the add an order comment out the xuihomepage and xui login
        .feed(randomFeeder)
        .repeat(30) {
          exec(Solicitor_PRL_CitizenDataPrep.CompleteDataPrep)
       //   feed(UserFeederPRLca)
       //   .exec(Homepage.XUIHomePage)
       //   .exec(Login.XUILogin)
        //  .exec(Solicitor_PRL_AddAnOrder.AddAnOrder)
        }
    }


  /*===============================================================================================
* PRL Citizen Journey
===============================================================================================*/

  val PrlCaseFlags = scenario("***** PRL Case Flags 2.1 Journey *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
        .exec(_.set("env", s"${env}")
          .set("caseType", "PRLAPPS"))
        .exec(Homepage.XUIHomePage)
        .exec(Login.XUILogin)
        .repeat(1) {
          feed(casesFeeder)
          //  exec(Solicitor_PRL_CitizenDataPrep.CompleteDataPrep)
          //   exec(Solicitor_PRL_CaseFlags.NoticeOfChangeSol)
          .exec(Solicitor_PRL_CaseFlags.ViewAllTabs)

             .exec(Solicitor_PRL_CaseFlags.CaseFlagsSol)
             .exec(Solicitor_PRL_CaseFlags.ManageSupport)

          .exec(Solicitor_PRL_CaseFlags.ViewAllTabs)

            .exec(Homepage.XUIHomePage)
            .exec(Login.XUILoginCa)

          .exec(Solicitor_PRL_CaseFlags.ViewAllTabsCa)

         //   exec(Solicitor_PRL_CaseFlags.AssignApplication)
          .exec(Solicitor_PRL_CaseFlags.ManageSupportFlag)
            .exec(Solicitor_PRL_CaseFlags.CaseFlagsCa)
            .exec(Solicitor_PRL_CaseFlags.ViewAllTabsCa)

        }

    }

    .exec {
      session =>
        println(session)
        session
    }

  /*===============================================================================================
* Cafcas API Scenario which runs CafcasDownloadByDocScenario, CafcasCasesByDatesScenario and CafcasDownloadByDocScenario
===============================================================================================*/

  val CafcasScenario = scenario("***** Cafcas Full Test *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
        .set("caseType", "Cafcas"))
        .repeat(1) {
          exec(CafcasAPI.getCasesBetweenDates)
            .repeat(15) {
              exec(CafcasAPI.downloadByDocId)
            }
        //    .repeat(15) {
        //      exec(CafcasAPI.uploadDocToCase)
          //  }
        }
    }
  
  /*===============================================================================================
  * Cafcas API Scenario which will be calling every 15 mins while running the PRL Test
   ===============================================================================================*/
  val CafcasCasesByDatesScenario = scenario("***** Cafcas Case data By Dates*****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
        .set("caseType", "Cafcas"))
          //.repeat(1) {
          .repeat(10) {
            exec(CafcasAPI.getCasesBetweenDates)
        }
    }
    .exec {
      session =>
        println(session)
        session
    }
  
  /*===============================================================================================
* Cafcas API Scenario which will be calling to download document based on doc ID
 ===============================================================================================*/
  val CafcasDownloadByDocScenario = scenario("***** Cafcas Download By Doc ID *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
        .set("caseType", "Cafcas"))
        .exec(CafcasAPI.downloadByDocId)
    }
  
  /*===============================================================================================
* Cafcas API Scenario which will be calling to upload doc based on the case ID
 ===============================================================================================*/
  val CafcasUploadByCaseScenario = scenario("***** Cafcas Upload By Case ID *****")
    .exitBlockOnFail {
      repeat(1) {
        exec(_.set("env", s"${env}")
          .set("caseType", "Cafcas"))
          .exec(CafcasAPI.uploadDocToCase)
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
            details("XUI_PRL_C100_460_SubmitAndPayNow").successfulRequests.percent.gte(80),
            details("XUI_PRL_FL401_490_SOTSubmit").successfulRequests.percent.gte(80),
            details("PRL_CitizenC100_810_005_FinalSubmit").successfulRequests.percent.gte(80))
        }
        else {
          Seq(global.successfulRequests.percent.is(100))
        }
      case _ =>
        Seq()
    }
  }
  
  setUp(
    //PrlCaseFlags.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
    CafcasCasesByDatesScenario.inject(simulationProfile(testType, prlTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption)
  ).protocols(httpProtocol)
    .assertions(assertions(testType))
    .maxDuration(75 minutes)
  
  
}
