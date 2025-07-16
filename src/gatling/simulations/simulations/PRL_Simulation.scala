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
  val c100AppTargetPerHour: Double = 64 //62
  val reasonableAdjustmentTargetPerHour = 10 //12
  val defaultTargetPerHour: Double = 10 //12
  // Smoke Configuration
  val smokeTarget: Double = 5

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
        .exec(Caseworker_PRL_C100_ProgressCase.IssueAndSendToLocalCourt)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminSendToGateKeeper)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminManageOrders)
        .exec(Caseworker_PRL_C100_ProgressCase.CourtAdminServiceApplication)
      }
      .exec(Logout.XUILogout)
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

  val PRLFL401CreateProgressRespondent = scenario("***** PRL FL401 Create, process and respond to cases *****")
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
     }

  //===============================================================================================
  // Solicitor Create & Progress FL401 Case via CCD API Calls (where applicable/possible)
  //===============================================================================================
  val PRLFL401CreateProgressCase = scenario("***** PRL FL401 Create Case (API) *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
      .exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS")
      //.set("caseId", "1750952108549355") //comment out when running e2e
      )
      .exec(
         _.setAll( // Set session Vars for use in JSON Payload
           "PRLRandomString" -> (Common.randomString(7)),
           "ApplicantFirstName" -> (Common.randomString(4) + "AppFirst"),
           "ApplicantLastName" -> (Common.randomString(4) + "AppLast"),
           "RespondentFirstName" -> (Common.randomString(5) + "respfirst"),
           "RespondentLastName" -> (Common.randomString(5) + "resplast"),
           "PRLAppDobDay" -> Common.getDay(),
           "PRLAppDobMonth" -> Common.getMonth(),
           "todayDate" -> Common.getDate(),
           "LegalAdviserName" -> (Common.randomString(4) + " " + Common.randomString(4) + "legAdv"),
           "JudgeFirstName" -> (Common.randomString(4) + "judgefirst"),
           "JudgeLastName" -> (Common.randomString(4) + "judgeLast"),
           "LARandomString" -> Common.randomString(5),
          "LARandomNumber" -> Common.randomNumber(4),
          "futureDate" -> Common.getFutureDate()))
      .exec(
        CCDAPI.CreateCaseFL401("Solicitor", "PRIVATELAW", "PRLAPPS", "solicitorCreate", "bodies/prl/fl401/PRLFL401CreateNewCase.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "fl401TypeOfApplication", "bodies/prl/fl401/PRLFL401TypeOfApplicationCheckYourAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "withoutNoticeOrderDetails", "bodies/prl/fl401/PRLFL401WithoutNoticeCheckYourAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "applicantsDetails", "bodies/prl/fl401/PRLFL401ApplicantDetails.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "respondentsDetails", "bodies/prl/fl401/PRLFL401RespondentDetailsCheckYourAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "fl401ApplicantFamilyDetails", "bodies/prl/fl401/PRLFL401ApplicantsFamilyDetailsCheckYourAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "respondentRelationship", "bodies/prl/fl401/PRLFL401RelationshipCheckYourAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "respondentBehaviour", "bodies/prl/fl401/PRLFL401BehaviourCheckYourAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "fl401Home", "bodies/prl/fl401/PRLFL401TheHomeCheckYourAnswers.json"),
        CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "3MB.pdf"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "fl401UploadDocuments", "bodies/prl/fl401/PRLFL401SubmitDocuments.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "fl401StatementOfTruthAndSubmit", "bodies/prl/fl401/PRLFL401SOTSubmit.json"))
      //======================================
      //Court Admin Progression
      //======================================
      .feed(UserCourtAdminPRL)
      .exec(
        CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "issueAndSendToLocalCourtCallback", "bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"),
        Homepage.XUIHomePage,
        Login.XUILogin,
          Caseworker_PRL_FL401_ProgressCase.CourtAdminSendToGateKeeper,
          Caseworker_PRL_FL401_ProgressCase.CourtAdminManageOrders,
          Caseworker_PRL_FL401_ProgressCase.CourtAdminServiceApplicationExtract,
        Logout.XUILogout,
        CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "TestFile.pdf"),
        CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "serviceOfApplication", "bodies/prl/courtAdmin/PRLSoASubmitFL401.json",
                          Seq(
                            jsonPath("$.case_data.caseInvites[0].value.accessCode").saveAs("prlAccessCodeApplicant"),
                            jsonPath("$.case_data.caseInvites[1].value.accessCode").saveAs("prlAccessCodeRespondent"))))
      //======================================
      // Court Manager Progression
      //======================================
      .exec(flushHttpCache)
      .exec(flushCookieJar)
      .feed(UserCaseManagerPRL)
      .exec(
        Homepage.XUIHomePage,
        Login.XUILogin,
          CaseManager_PRL_FL401_ProgressCase.CaseManagerConfidentialityCheck,
        Logout.XUILogout)
      //================================
      //Request and List Hearings x 2
      //================================
    .repeat(2) {
     exec(
        API_HMCHearings.Auth("ccdUser"),
        API_HMCHearings.GetCaseDetailsFL401,
        API_HMCHearings.Auth("hmcHearingRequest"),
        API_HMCHearings.RequestHearing("FL401"),
        //List the hearing (Mimic request back from List Assist)
        API_HMCHearings.Auth("hmcHearingList"),
        API_HMCHearings.ListHearing("FL401"))
    }
    //Write codes to file for Citizen UI Flows
   .exec(Caseworker_PRL_FL401_ProgressCase.WriteAccessCodesToFile)
  }

  //===============================================================================================
  // Solicitor Create & Progress C100 Case via CCD API Calls (where applicable/possible)
  //===============================================================================================
  val PRLC100CreateProgressCase = scenario("***** PRL C100 Create Case (API) *****")
    .exitBlockOnFail {
      feed(UserFeederPRL)
      .exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS")
      //.set("caseId", "1752586714982192") //comment out when running e2e
      )
      .exec(_.setAll(
        "C100ApplicantFirstName1" -> ("App" + Common.randomString(5)),
        "C100ApplicantLastName1" -> ("Test" + Common.randomString(5)),
        "C100ApplicantFirstName2" -> ("App" + Common.randomString(5)),
        "C100ApplicantLastName2" -> ("Test" + Common.randomString(5)),
        "C100RespondentFirstName" -> ("Resp" + Common.randomString(5)),
        "C100RespondentLastName" -> ("Test" + Common.randomString(5)),
        "C100ChildFirstName" -> ("Child" + Common.randomString(5)),
        "C100ChildLastName" -> ("Test" + Common.randomString(5)),
        "C100RepresentativeFirstName" -> ("Rep" + Common.randomString(5)),
        "C100RepresentativeLastName" -> ("Test" + Common.randomString(5)),
        "C100SoleTraderName" -> ("Sole" + Common.randomString(5)),
        "C100SolicitorName" -> ("Soli" + Common.randomString(5)),
        "C100AppDobDay" -> Common.getDay(),
        "C100AppDobMonth" -> Common.getMonth(),
        "C100AppDobYear" -> Common.getDobYear(),
        "C100AppDobDay2" -> Common.getDay(),
        "C100AppDobMonth2" -> Common.getMonth(),
        "C100AppDobYear2" -> Common.getDobYear(),
        "C100ChildAppDobDay" -> Common.getDay(),
        "C100ChildAppDobMonth" -> Common.getMonth(),
        "C100ChildDobYear" -> Common.getDobYearChild(),
        "C100RespDobDay" -> Common.getDay(),
        "C100RespDobMonth" -> Common.getMonth(),
        "C100RespDobYear" -> Common.getDobYear(),
        "LARandomString" -> Common.randomString(5),
        "LARandomNumber" -> Common.randomNumber(4),
        "futureDate" -> Common.getFutureDate(),
        "todayDate" -> Common.getDate()))
      .exec(
        CCDAPI.CreateCaseFL401("Solicitor", "PRIVATELAW", "PRLAPPS", "solicitorCreate", "bodies/prl/c100/PRLC100CreateNewCase.json"),
        CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "3MB.pdf"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "selectApplicationType", "bodies/prl/c100/PRLCheckYourAnswersTypeOfApplication.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "hearingUrgency", "bodies/prl/c100/PRLHearingUrgencyAnswers.json"),
          //Homepage.XUIHomePage,
          //Login.XUILogin,
          Common.postcodeLookupDirect,
          //Logout.XUILogout,
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "applicantsDetails", "bodies/prl/c100/PRLApplicantDetailsAnswers.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "respondentsDetails", "bodies/prl/c100/PRLRespondentDetailsSubmit.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "otherPeopleInTheCaseRevised", "bodies/prl/c100/PRLOtherPeopleSubmit.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "childDetailsRevised", "bodies/prl/c100/PRLChildDetailsEvent.json",
                          checksTrigger= Seq(
                              jsonPath("$.case_details.case_data.newChildDetails[*].value.whoDoesTheChildLiveWith.list_items[*].code").findAll.saveAs("childLiveWithCode"),
                              jsonPath("$.case_details.case_data.newChildDetails[*].value.whoDoesTheChildLiveWith.list_items[*].label").findAll.saveAs("childLiveWithLabel"),
                              jsonPath("$.case_details.id").saveAs("childLiveWithId"))),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "allegationsOfHarmRevised", "bodies/prl/c100/PRLAreThereAllegationsOfHarmSubmit.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "miam", "bodies/prl/c100/PRLMIAMDetailsSubmit.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "otherChildNotInTheCase", "bodies/prl/c100/PRLOtherChildrenSubmit.json"),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "childrenAndApplicants", "bodies/prl/c100/PRLChildrenAndApplicantSubmit.json",
                          checksTrigger= Seq(
                              jsonPath("$.case_details.case_data.buffChildAndApplicantRelations[*].value.applicantId").findAll.saveAs("applicantId"),
                              jsonPath("$.case_details.case_data.buffChildAndApplicantRelations[*].value.childId").findAll.saveAs("childId"),
                              jsonPath("$.case_details.case_data.buffChildAndApplicantRelations[*].id").findAll.saveAs("additionalApplicantId"))),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "childrenAndRespondents", "bodies/prl/c100/PRLChildrenAndRespondentsSubmit.json",
                          checksTrigger= Seq(
                              jsonPath("$.case_details.case_data.buffChildAndRespondentRelations[*].value.respondentId").findAll.saveAs("respondentId"),
                              jsonPath("$.case_details.case_data.buffChildAndRespondentRelations[*].value.childId").findAll.saveAs("childId"),
                              jsonPath("$.case_details.case_data.buffChildAndRespondentRelations[*].value.respondentId").findAll.saveAs("additionalRespondentId"))),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "childrenAndOtherPeople", "bodies/prl/c100/PRLChildrenAndOtherPeopleSubmit.json",
                          checksTrigger= Seq(
                              jsonPath("$.case_details.case_data.buffChildAndOtherPeopleRelations[*].id").findAll.saveAs("otherPeopleAdditionalId"),
                              jsonPath("$.case_details.case_data.buffChildAndOtherPeopleRelations[*].value.otherPeopleId").findAll.saveAs("otherPeopleId"),
                              jsonPath("$.case_details.case_data.buffChildAndOtherPeopleRelations[0].value.childFullName").findAll.saveAs("childFullName"))),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "submitAndPay", "bodies/prl/c100/PRLSubmitAndPayNow.json",
                          checksTrigger= Seq(
                              jsonPath("$.case_details.case_data.submitAndPayDownloadApplicationLink.document_url").saveAs("DocumentUrl"),
                              jsonPath("$.case_details.case_data.submitAndPayDownloadApplicationLink.document_filename").saveAs("DocumentFileName"),
                              jsonPath("$.case_details.case_data.submitAndPayDownloadApplicationLink.document_hash").saveAs("DocumentHash"),
                              jsonPath("$.case_details.case_data.feeAmount").saveAs("feeAmount"))),
        CCDAPI.CreateEvent("Solicitor", "PRIVATELAW", "PRLAPPS", "testingSupportPaymentSuccessCallback", "bodies/prl/c100/PRLDummyPaymentSubmit.json"))
      //==========================
      //Court Admin Progression
      //==========================
      //.exec(flushHttpCache)
      //.exec(flushCookieJar)
      .feed(UserCourtAdminPRL)
      .exec(
        Homepage.XUIHomePage,
        Login.XUILogin,
          Caseworker_PRL_C100_ProgressCase.CourtAdminCheckApplication,
          CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "issueAndSendToLocalCourtCallback", "bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"),
          Caseworker_PRL_C100_ProgressCase.CourtAdminSendToGateKeeper,
          Caseworker_PRL_C100_ProgressCase.CourtAdminManageOrders,
          Caseworker_PRL_C100_ProgressCase.CourtAdminServiceApplicationExtract,
          CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "TestFile.pdf",
                          Seq(
                            jsonPath("$.documents[0].hashToken").saveAs("documentHashPD36Q"),
                            jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLPD36Q"))),
        CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "TestFile2.pdf",
                          Seq(
                            jsonPath("$.documents[0].hashToken").saveAs("documentHashSpecial"),
                            jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLSpecial"))),
        CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "serviceOfApplication", "bodies/prl/courtAdmin/PRLSoASubmit.json",
                          Seq(
                            jsonPath("$.case_data.caseInvites[0].value.accessCode").saveAs("prlAccessCodeApplicant"),
                            jsonPath("$.case_data.caseInvites[1].value.accessCode").saveAs("prlAccessCodeRespondent"))),
        Logout.XUILogout)
      //================================
      //Request and List Hearings x 2
      //================================
      .repeat(2) {
       exec(
          API_HMCHearings.Auth("ccdUser"),
          API_HMCHearings.GetCaseDetails,
          API_HMCHearings.Auth("hmcHearingRequest"),
          API_HMCHearings.RequestHearing("C100"),
          //List the hearing (Mimic request back from List Assist)
          API_HMCHearings.Auth("hmcHearingList"),
          API_HMCHearings.ListHearing("C100"))
      }
      //Write codes to file for Citizen UI Flows
    .exec(Caseworker_PRL_C100_ProgressCase.WriteAccessCodesToFile)
    }


  /*===============================================================================================
  * PRL Citizen C100 Create & Progress by Caseworker  ** OLD DELETE SOON **
  ===============================================================================================*/

   val PRLC100CitizenCreateProgressCase = scenario("***** PRL Citizen Journey & Case Progression *****")
    .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseType", "PRLAPPS")
      set("caseId", "1751622484620398")) //comment out when running e2e
      .repeat(1) {
        // Create Citizen User
        exec(API_IDAM.CreateUserInIdam("App"))
        // Citizen CUI Create Steps
        //.exec(Citizen_PRL_C100_Applicant.C100Case)
        //.exec(Citizen_PRL_C100_Applicant.C100Case2)
        //.exec(Logout.CUILogout)
        // CaseWorker Progression XUI Steps
       .feed(UserCourtAdminPRL)
       .exec(
          Homepage.XUIHomePage,
          Login.XUILogin,
            //  Caseworker_PRL_C100_ProgressCase.CourtAdminCheckApplication,
            //  CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "issueAndSendToLocalCourtCallback", "bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"),
            //  Caseworker_PRL_C100_ProgressCase.CourtAdminSendToGateKeeper,
            //  Caseworker_PRL_C100_ProgressCase.CourtAdminManageOrders,
            //  CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "TestFile.pdf",
            //          Seq(
            //            jsonPath("$.documents[0].hashToken").saveAs("documentHashPD36Q"),
            //            jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLPD36Q"))),
            //  CCDAPI.UploadDocument("CourtAdmin", "PRIVATELAW", "PRLAPPS", "TestFile2.pdf",
            //          Seq(
            //           jsonPath("$.documents[0].hashToken").saveAs("documentHashSpecial"),
            //           jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLSpecial"))),
            //  Caseworker_PRL_C100_ProgressCase.CourtAdminServiceApplicationExtract,
            //  CCDAPI.CreateEvent("CourtAdmin", "PRIVATELAW", "PRLAPPS", "serviceOfApplication", "bodies/prl/courtAdmin/PRLSoASubmit.json",
            //          Seq(
            //            jsonPath("$.case_data.caseInvites[0].value.accessCode").saveAs("prlAccessCodeApplicant"),
            //            jsonPath("$.case_data.caseInvites[1].value.accessCode").saveAs("prlAccessCodeRespondent"))),
            // Caseworker_PRL_C100_ProgressCase.WriteAccessCodesToFile,
            Caseworker_PRL_C100_ProgressCase.ListHearing,
          Logout.XUILogout)
    } // end of repeat
    }

    // ===========================
    // TEST HEARINGS
    // ===========================
    
    val testHearings = scenario("***** TEST HEARINGS *****")
     .exitBlockOnFail {
      exec(_.set("env", s"${env}")
      .set("caseId", "1752609822768756") //comment out when running e2e
      .set("caseType", "PRLAPPS"))
        //set session variables
      .exec(_.setAll(
      "LARandomString" -> Common.randomString(5),
      "LARandomNumber" -> Common.randomNumber(4),
      "futureDate" -> Common.getFutureDate(),
      "todayDate" -> Common.getDate()))
      .exec(
        API_HMCHearings.Auth("ccdUser"),
        API_HMCHearings.GetCaseDetailsFL401,
        API_HMCHearings.Auth("hmcHearingRequest"),
        API_HMCHearings.RequestHearing("FL401"), 
          //List the hearing (Mimic request back from List Assist)
          API_HMCHearings.Auth("hmcHearingList"),
          API_HMCHearings.ListHearing("FL401"))
        //View hearings tab once listed 
        //CourtAdmin_PRL_C100.CourtAdminHearingsTab
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
//  PRLC100CitizenScenario.inject(simulationProfile(testType, c100AppTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLC100RespondentScenario.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLFL401RespondentScenario.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLReasonableAdjustmentsAdd.inject(simulationProfile(testType, reasonableAdjustmentTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLReasonableAdjustmentsModify.inject(simulationProfile(testType, reasonableAdjustmentTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLC100ApplicantDashboardScenario.inject(simulationProfile(testType, c100AppTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLFL401ApplicantDashboardScenario.inject(simulationProfile(testType, defaultTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),
//  PRLCitizenApplicationGuidance.inject(simulationProfile(testType, c100AppTargetPerHour, numberOfPipelineUsers)).pauses(pauseOption),

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
  //PRLFL401CreateProgressCase.inject(atOnceUsers(1)),
  //PRLCreateAndProcessCases.inject(atOnceUsers(1)),
  //PrlFL401Create.inject(atOnceUsers(6)),
  //PRLFL401CreateProgressRespondent.inject(atOnceUsers(1)),
  //PRLC100CaseworkerScenario.inject(atOnceUsers(9)),
    //  PRLCitizenApplicationGuidance.inject(atOnceUsers(3)),
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
    PRLFL401CreateProgressCase.inject(atOnceUsers(1))
    //testHearings.inject(atOnceUsers(1))
    //PRLC100CaseworkerScenario.inject(atOnceUsers(1))
    //PRLFL401CreateProgressRespondent.inject(atOnceUsers(1))

  ).protocols(httpProtocol)
    .assertions(assertions(testType))
    .maxDuration(75 minutes) //75

}
