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
  val UserCaseManagerPRL = csv("UserDataCaseManager.csv").circular
  val UserFeederPRLRespondent = csv("UserDataRespondent.csv").circular
  val fl401caseFeeder = csv("FL401CourtAdminData.csv")
  val c100CaseFeeder = csv("C100CourtAdminData.csv")
  val c100RespondentData = csv("C100RespondentData.csv")
  val c100ApplicantDashData = csv("C100ApplicantDashData.csv")
  val fl401ApplicantDashData = csv("FL401ApplicantDashData.csv")
  val fl401RespondentData = csv("FL401RespondentData.csv")
  val RAData_Add = csv("ReasonableAdjustments_Add.csv")
  val RAData_Modify = csv("ReasonableAdjustments_Modify.csv") //.circular
  val cafcassCaseFeeder = csv("CasesForDocUpload.csv").queue

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
  val prlTargetPerHour: Double = 32 //30
  val caseworkerTargetPerHour: Double = 30 //30
  val c100AppTargetPerHour: Double = 65 //62
  val reasonableAdjustmentTargetPerHour = 12 //12
  val defaultTargetPerHour: Double = 12 //12
  // Smoke Configuration
  val smokeTarget: Double = 5

  //This determines the percentage split of PRL journeys, by C100 or FL401
  val prlC100Percentage = 100 //Percentage of C100s (the rest will be FL401s) - should be 66 for the 2:1 ratio

  val rampUpDurationMins = 10
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
* API CAFCASS - UploadDocument
===============================================================================================*/

  val PRLAPICAFCASSGetDocument = scenario("***** API CAFCASS Get Document *****")
      .exitBlockOnFail {
        exec(_.set("env", s"${env}"))
          .feed(cafcassCaseFeeder)
          .exec(API_CAFCASS.UploadDocument)
      }

/*===============================================================================================
* Citizen Application Guidance Screens
===============================================================================================*/

  val PRLCitizenApplicationGuidance = scenario("***** PRL Citizen Application Guidance *****")
      .exitBlockOnFail {
        exec(_.set("env", s"${env}"))
        .repeat(1) {
          exec(Citizen_PRL_ApplicationGuidance.CompleteApplicationGuidance)
        }
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
      .repeat(10) {
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
        //.exec(Citizen_PRL_C100_Respondent.GetCase)                              // Not needed as case retrieve opens the linked case, use for debugging
        .exec(Citizen_PRL_C100_Respondent.ConfirmEditContactDetails)              // New for R6.0
        .exec(Citizen_PRL_C100_Respondent.ContactPreferences)
        .exec(Citizen_PRL_C100_Respondent.KeepDetailsPrivate)
        .exec(Citizen_PRL_C100_Respondent.SupportYouNeed)
        .exec(Citizen_PRL_C100_Respondent.CheckApplication)
        .exec(Citizen_PRL_C100_Respondent.CheckHarmViolenceAllegations)           //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.MakeRequestToCourtAboutCase)            //New for R6.0/7.0
        .exec(Citizen_PRL_C100_Respondent.RespondToApplication)
        .exec(Citizen_PRL_C100_Respondent.UploadDocumentsApplicationsStatements)  //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.ViewAllDocuments)
        .exec(Citizen_PRL_C100_Respondent.ViewServedAppPack)                      //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.ViewAllDocuments)                       //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.ViewRespondentsDocuments)               //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.ViewAllDocuments)                       //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.ViewCourtHearings)                      //New for R6.0
        .exec(Citizen_PRL_C100_Respondent.WriteDataToFile)
        .exec(Logout.CUILogout)
      }
    }


  /*===============================================================================================
  * PRL Citizen C100 ApplicantDashboard Journey
  ===============================================================================================*/

  val PRLC100ApplicantDashboardScenario = scenario("***** PRL Citizen C100 Applicant Dashboard Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCitizenPRL)
      .exec(Homepage.PRLHomePage)
      .exec(Login.PrlLogin)
      .repeat(1) {
        feed(c100ApplicantDashData)
        .exec(Citizen_PRL_C100_ApplicantDashboard.RetrieveCase)
        //.exec(Citizen_PRL_C100_ApplicantDashboard.GetCase)                                // Not needed in this journey as once linked you are redirected to the dashboard (Use for script dev and debugging)
        .exec(Citizen_PRL_C100_ApplicantDashboard.YourApplication)
        .exec(Citizen_PRL_C100_ApplicantDashboard.CheckHarmViolenceAllegations)           //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.MakeRequestToCourtAboutCase)            //New for R6.0/7.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.UploadDocumentsApplicationsStatements)  //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.ViewAllDocuments)                       //New for R6.0
        //.exec(Citizen_PRL_C100_ApplicantDashboard.ViewServedAppPack)                      //16/04/2025 - Not available within CUI anymore - commenting out
        //.exec(Citizen_PRL_C100_ApplicantDashboard.ViewAllDocuments)                       //As above not needed if ServedAppPack is not available
        //.exec(Citizen_PRL_C100_ApplicantDashboard.ViewRespondentsDocuments)             //Not needed in this journey
        .exec(Citizen_PRL_C100_ApplicantDashboard.ViewApplicantsDocuments)                //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.ViewAllDocuments)                       //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.ViewOrdersFromTheCourt)                 //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.ViewAllDocuments)                       //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.ViewCourtHearings)                      //New for R6.0
        .exec(Citizen_PRL_C100_ApplicantDashboard.WriteDataToFile)
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
        //.exec(Citizen_PRL_FL401_Respondent.GetCase)                             // Not needed as case link takes you through to open the case
        .exec(Citizen_PRL_FL401_Respondent.ConfirmEditContactDetails)             // New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.ContactPreferences)                    // New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.KeepDetailsPrivate)
        //.exec(Citizen_PRL_FL401_Respondent.ContactDetails)                      // No longer visible
        .exec(Citizen_PRL_FL401_Respondent.SupportYouNeed)
        .exec(Citizen_PRL_FL401_Respondent.CheckApplication)
        .exec(Citizen_PRL_FL401_Respondent.MakeRequestToCourtAboutCase)           //New for R6.0/7.0
        .exec(Citizen_PRL_FL401_Respondent.UploadDocumentsApplicationsStatements) //New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.ViewAllDocuments)                      //New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.ViewApplicantsDocuments)               //New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.ViewAllDocuments)                      //New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.ViewCourtHearings)                     //New for R6.0
        .exec(Citizen_PRL_FL401_Respondent.WriteDataToFile)
        .exec(Logout.CUILogout)

      }
    }
  }

 /*===============================================================================================
  * PRL Citizen FL401 ApplicantDashboard Journey
  ===============================================================================================*/

  val PRLFL401ApplicantDashboardScenario = scenario("***** PRL Citizen FL401 Applicant Dashboard Journey *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCitizenPRL)
      .exec(Homepage.PRLHomePage)
      .exec(Login.PrlLogin)
      .repeat(1) {
        feed(fl401ApplicantDashData)
        .exec(Citizen_PRL_FL401_ApplicantDashboard.RetrieveCase)
        //.exec(Citizen_PRL_FL401_ApplicantDashboard.GetCase)                                // Not needed in this journey as once linked you are redirected to the dashboard (Use for script deb and debugging)
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ConfirmEditContactDetails)               // New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ContactPreferences)
        .exec(Citizen_PRL_FL401_ApplicantDashboard.KeepDetailsPrivate)
        .exec(Citizen_PRL_FL401_ApplicantDashboard.SupportYouNeed)
        .exec(Citizen_PRL_FL401_ApplicantDashboard.YourApplication)
        .exec(Citizen_PRL_FL401_ApplicantDashboard.MakeRequestToCourtAboutCase)             //New for R6.0/7.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.UploadDocumentsApplicationsStatements)   //New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ViewAllDocuments)                        //New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ViewOrdersFromTheCourt)                  //New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ViewAllDocuments)                        //New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ViewApplicantsDocuments)                 //New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.ViewCourtHearings)                       //New for R6.0
        .exec(Citizen_PRL_FL401_ApplicantDashboard.WriteDataToFile)
        .exec(Logout.CUILogout)
        //.exec(Citizen_PRL_FL401_ApplicantDashboard.RespondToApplication)
        //.exec(Citizen_PRL_FL401_ApplicantDashboard.CheckHarmViolenceAllegations) //New for R6.0
        //.exec(Citizen_PRL_FL401_ApplicantDashboard.UploadDocumentsApplicationsStatements) //New for R6.0
        //.exec(Citizen_PRL_FL401_ApplicantDashboard.ViewServedAppPack) //Not needed in this journey
        //.exec(Citizen_PRL_FL401_ApplicantDashboard.ViewRespondentsDocuments) //Not needed in this journey
      }
    }


  /*==================================================================================================================================================
  * PRL FL401 Create Case (Solicitor), Progress Case (CourtAdmin, CaseManager), FL401 Respondent, FL401 Applicant (Split 50/50 by VuserID modulo 2)
  ===================================================================================================================================================*/

  val PRLFL401CreateProgressRespondent = scenario("***** PRL FL401 Create, Process and respond to cases *****")
    .exitBlockOnFail {
        feed(UserFeederPRL)
        .exec(_.set("env", s"${env}")
        .set("caseType", "PRLAPPS"))
        // Solicitor XUI FL401 Create
        .exec(
          Homepage.XUIHomePage,
          Login.XUILogin,
            Solicitor_PRL_FL401_CaseCreate.CreatePrivateLawCase,
            Solicitor_PRL_FL401_CaseCreate.TypeOfApplication,
            Solicitor_PRL_FL401_CaseCreate.WithoutNoticeOrder,
            Solicitor_PRL_FL401_CaseCreate.ApplicantDetails,
            Solicitor_PRL_FL401_CaseCreate.RespondentDetails,
            Solicitor_PRL_FL401_CaseCreate.ApplicantsFamily,
            Solicitor_PRL_FL401_CaseCreate.Relationship,
            Solicitor_PRL_FL401_CaseCreate.Behaviour,
            Solicitor_PRL_FL401_CaseCreate.TheHome,
            Solicitor_PRL_FL401_CaseCreate.UploadDocuments,
            Solicitor_PRL_FL401_CaseCreate.ViewPDF,
            Solicitor_PRL_FL401_CaseCreate.StatementOfTruth,
          Logout.XUILogout)
        // CourtAdmin - Progress Case - Issue & Send to Local Court
        .feed(UserCourtAdminPRL)
        .exec(
          CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "issueAndSendToLocalCourtCallback", "bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"))
        // CourtAdmin - Progress Case - Send to GateKeeper
        .exec(
          Homepage.XUIHomePage,
          Login.XUILogin,
            Caseworker_PRL_FL401_ProgressCase.CourtAdminSendToGateKeeper,
          Logout.XUILogout)
        //====================================================================================================================================
        // // CourtAdmin - Progress Case - Send to GateKeeper   (NOT WORKING SO LEAVING COMMENTED OUT FOR NOW)
        // .exec(
        //   CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "sendToGateKeeper", "bodies/prl/courtAdmin/PRLAddGateKeeperSubmit.json"))
        // //====================================================================================================================================
        // CourtAdmin - Progress Case - Submit Order
        // .exec(
        //   _.setAll( // Set session Vars for use in JSON Payload
        //     "PRLRandomString" -> (Common.randomString(7)),
        //     "JudgeFirstName" -> (Common.randomString(4) + "judgefirst"),
        //     "JudgeLastName" -> (Common.randomString(4) + "judgeLast"),
        //     "PRLAppDobDay" -> Common.getDay(),
        //     "PRLAppDobMonth" -> Common.getMonth(),
        //     "todayDate" -> Common.getDate(),
        //     "LegalAdviserName" -> (Common.randomString(4) + " " + Common.randomString(4) + "legAdv")),
        //   CCDAPI.GetCaseDetails,
        //   CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "manageOrders", "bodies/prl/courtAdmin/PRLOrderSubmitFL401.json"))
        // // CourtAdmin = Progress Case - Upload Document pre Service of Application
        // .exec(
        //   CCDAPI.EventAndUploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "serviceOfApplication", "TestFile.pdf", "bodies/prl/courtAdmin/PRLSoASubmitFL401.json"))
    }

  /*===============================================================================================
  * PRL Citizen C100 Create & Progress by Caseworker
  ===============================================================================================*/

   val PRLC100CitizenCreateAndProgressCase = scenario("***** PRL Citizen Journey & Case Progression *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS"))
      .feed(UserCitizenPRL)
      .repeat(1) {
        // Citizen CUI Create Steps
        exec(Citizen_PRL_C100_Applicant.C100Case)
        .exec(Citizen_PRL_C100_Applicant.C100Case2)
        .exec(Logout.CUILogout)
      .feed(UserCourtAdminPRL)
      // CaseWorker Progression XUI Steps
      .exec(Homepage.XUIHomePage)
      .exec(Login.XUILogin)
        .feed(c100CaseFeeder) //** This needs to be removed so the caseid is passed through from the Citizen CUI flow
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminCheckApplication)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminSendToGateKeeper)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminManageOrders)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminServiceApplication)
      }
    }


  /*===============================================================================================
  * Simulation Configuration
   ===============================================================================================*/

  def simulationProfile (simulationType: String, userPerHourRate: Double, numberOfPipelineUsers: Double): Seq[OpenInjectionStep] = {
    val userPerSecRate = userPerHourRate / 3600 //Remember to change back to 3600
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
              details("PRL_Citizen_050_CompleteApplicationLegalRepContinue").successfulRequests.percent.gte(80),
              details("PRL_FL401Respondent_470_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_C100Respondent_480_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_C100ApplicantDashboard_460_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_FL401ApplicantDashboard_470_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_CitizenC100_790_Logout").successfulRequests.percent.gte(80),
              details("PRL_RA-Add_180_SubmitReasonableAdjustments").successfulRequests.percent.gte(80),
              details("PRL_RA-Modify_110_ConfirmRAModify").successfulRequests.percent.gte(80))
        }
        else {
          Seq(global.successfulRequests.percent.is(100),
              details("PRL_Citizen_050_CompleteApplicationLegalRepContinue").successfulRequests.percent.gte(80),
              details("PRL_FL401Respondent_470_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_C100Respondent_480_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_C100ApplicantDashboard_460_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_FL401ApplicantDashboard_470_ViewCourtHearings").successfulRequests.percent.gte(80),
              details("PRL_CitizenC100_790_Logout").successfulRequests.percent.gte(80),
              details("PRL_RA-Add_180_SubmitReasonableAdjustments").successfulRequests.percent.gte(80),
              details("PRL_RA-Modify_110_ConfirmRAModify").successfulRequests.percent.gte(80))
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
  PRLReasonableAdjustmentsAdd.inject(simulationProfile(testType, reasonableAdjustmentTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLReasonableAdjustmentsModify.inject(simulationProfile(testType, reasonableAdjustmentTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLC100ApplicantDashboardScenario.inject(simulationProfile(testType, c100AppTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLFL401ApplicantDashboardScenario.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
  PRLCitizenApplicationGuidance.inject(simulationProfile(testType, c100AppTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),

    //=================================================
  //C100 & CUIRA Release Scenarios - SMOKE TEST
  //=================================================
  // PRLC100CitizenScenario.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),
  // PRLC100RespondentScenario.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),
  // PRLFL401RespondentScenario.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),
  // PRLReasonableAdjustmentsAdd.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),
  // PRLReasonableAdjustmentsModify.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),
  // PRLC100ApplicantDashboardScenario.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),
  // PRLFL401ApplicantDashboardScenario.inject(simulationProfile(testType, smokeTarget, numberOfPipelineUsers)).pauses(pauseOption),

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
  //PrlDataPrep.inject(atOnceUsers(1)),
  //PRLFL401CaseworkerScenario.inject(atOnceUsers(4)),
  //PRLFL401CaseManagerScenario.inject(atOnceUsers(7)),
  //PRLC100CitizenCreateAndProgressCase.inject(atOnceUsers(1)),
  //PRLCreateAndProcessCases.inject(atOnceUsers(1)),
  //PrlFL401Create.inject(atOnceUsers(6)),
  //PRLFL401CreateProgressRespondent.inject(atOnceUsers(1)),
  //PRLC100CaseworkerScenario.inject(atOnceUsers(11)),
     // PRLCitizenApplicationGuidance.inject(atOnceUsers(3)),
    //  PRLC100RespondentScenario.inject(atOnceUsers(3)),
    //  PRLC100ApplicantDashboardScenario.inject(atOnceUsers(3)),
    //  PRLFL401RespondentScenario.inject(atOnceUsers(3)),
    //  PRLFL401ApplicantDashboardScenario.inject(atOnceUsers(3)),
    //  PRLC100CitizenScenario.inject(atOnceUsers(3)),
    //  PRLReasonableAdjustmentsAdd.inject(atOnceUsers(3)),
    //  PRLReasonableAdjustmentsModify.inject(atOnceUsers(3)),

   //=========================================================
   // At Once Users - For API Tests
   //=========================================================
   //PRLAPICAFCASSGetDocument.inject(atOnceUsers(100)),

  ).protocols(httpProtocol)
    .assertions(assertions(testType))
    .maxDuration(75 minutes) //75

}
