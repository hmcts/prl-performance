package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*===============================================================================================================
* Court Admin C100 case progression. Send to local court --> Sent to Gatekeeper --> Add an order --> Serve 
================================================================================================================*/

object Caseworker_PRL_C100_ProgressCase {
  
  val BaseURL = Environment.baseURL
  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth(),
      "JudgeFirstName" -> (Common.randomString(4) + "judgefirst"),
      "JudgeLastName" -> (Common.randomString(4) + "judgeLast"),
      "todayDate" -> Common.getDate(),
      "LegalAdviserName" -> (Common.randomString(4) + " " + Common.randomString(4) + "legAdv")))

  val CourtAdminCheckApplication =

    exec(http("XUI_PRL_XXX_290_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
      .check(jsonPath("$.case_id").is("#{caseId}")))

    .exec(Common.waJurisdictions)
    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.caseActivityGet)
    .exec(Common.isAuthenticated)

    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

    .pause(MinThinkTime, MaxThinkTime)

   /*=====================================================================================
   * Select task tab 
   ======================================================================================*/

    .exec(http("XUI_PRL_XXX_300_SelectCase")
      .get(BaseURL + "/cases/case-details/#{caseId}/task")
      .headers(Headers.xuiHeader)
      .check(substring("HMCTS Manage cases")))

    .exec(http("XUI_PRL_XXX_310_SelectCaseTask")
      .get(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(jsonPath("$[0].id").optional.saveAs("taskId"))
      .check(jsonPath("$[0].type").optional.saveAs("taskType")))

    //Save taskType from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("taskType").asOption[String] match {
        case Some(taskType) => session
        case None => session.set("taskType", "")
      }
    })

    // Loop until the task type matches "checkApplicationC100" or "checkHwfApplicationC100"
    .asLongAs(session => session("taskType").as[String] != "checkApplicationC100" &&
                         session("taskType").as[String] != "checkHwfApplicationC100")
    {
      exec(http("XUI_PRL_XXX_310_SelectCaseTaskRepeat")
        .get(BaseURL + "/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$[0].id").optional.saveAs("taskId"))
        .check(jsonPath("$[0].type").optional.saveAs("taskType")))

      .pause(5, 10) // Wait between retries

    //   // Log task Type
    //   .exec (session => {
    //     println(s"Current Task Type: ${session("taskType").as[String]}")
    //     session
    // })
    } // end asLongAs

  /*=====================================================================================
  * Claim the task
  ======================================================================================*/

  .exec(http("XUI_PRL_XXX_320_ClaimTask")
      .post(BaseURL + "/workallocation/task/#{taskId}/claim")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("""{}"""))
      .check(status.in(200, 204)))

  .pause(MinThinkTime, MaxThinkTime)

  // if help with fee's task, additional steps required 
  .doIf(session => session("taskType").as[String] == "checkHwfApplicationC100") {

    /*====================================================================================
    * Complete the task
    ======================================================================================*/

      exec(http("XUI_PRL_XXX_321_SelectHelpWithFees")
        .post(BaseURL + "/workallocation/task/#{taskId}/complete")
        .headers(Headers.xuiHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{}"""))
        .check(status.in(200, 204)))

    .pause(MinThinkTime, MaxThinkTime)

    //Add Process Urgent help with Fees event

    //   /*=====================================================================================
    //   * Refresh tasks until the next task is available
    //   ======================================================================================*/

    // // Loop until the task type matches "checkApplicationC100"
    //   .asLongAs(session => session("taskType").as[String] != "checkApplicationC100")
    //   {
    //     exec(http("XUI_PRL_XXX_322_SelectCaseTaskRepeat")
    //       .get(BaseURL + "/workallocation/case/task/#{caseId}")
    //       .headers(Headers.xuiHeader)
    //       .header("Accept", "application/json, text/plain, */*")
    //       .header("x-xsrf-token", "#{XSRFToken}")
    //       .check(jsonPath("$[0].id").optional.saveAs("taskId"))
    //       .check(jsonPath("$[0].type").optional.saveAs("taskType")))

    //     .pause(5, 10) // Wait between retries

    //     /*=====================================================================================
    //     * Claim the task
    //     ======================================================================================*/

    //     .exec(http("XUI_PRL_XXX_323_ClaimTask")
    //       .post(BaseURL + "/workallocation/task/#{taskId}/claim")
    //       .headers(Headers.xuiHeader)
    //       .header("Accept", "application/json, text/plain, */*")
    //       .header("x-xsrf-token", "#{XSRFToken}")
    //       .body(StringBody("""{}"""))
    //       .check(status.in(200, 204)))

    //     .pause(MinThinkTime, MaxThinkTime)

    // } //End asLongAs

  } //End of HWF's if

  val IssueAndSendToLocalCourt = 
  /*=====================================================================================
  * Select Issue and send to local Court
  ======================================================================================*/

  exec(http("XUI_PRL_XXX_360_IssueAndSendToLocalCourt")
    .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/issueAndSendToLocalCourtCallback/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
    .headers(Headers.navigationHeader)
    .header("accept", "application/json")
    .check(jsonPath("$.task_required_for_event").is("true")))

  .exec(Common.activity)
  .exec(Common.profile)

  .exec(http("XUI_PRL_XXX_370_IssueAndSendToLocalCourtEventTrigger")  //*** SAVE THE Courtlist response here for use in later post requests **
    .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/issueAndSendToLocalCourtCallback?ignore-warning=false")
    .headers(Headers.xuiHeader)
    .header("Accept", "application/json, text/plain, */*")
    .check(jsonPath("$.event_token").saveAs("event_token"))
    .check(jsonPath("$.id").is("issueAndSendToLocalCourtCallback"))
    .check(status.in(200, 403)))

  .exec(http("XUI_PRL_XXX_380_IssueAndSendToLocalCourtEvent")
    .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/issueAndSendToLocalCourtCallback/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
    .headers(Headers.navigationHeader)
    .header("accept", "application/json"))
    //.check(substring("PRIVATELAW")))
  
  // .exec(Common.caseActivityPost)
  .exec(Common.userDetails)
  .exec(Common.caseActivityOnlyGet)

  .pause(MinThinkTime, MaxThinkTime)

/*=====================================================================================
  * Select Court from dropdown and submit
  ======================================================================================*/

  .exec(http("XUI_PRL_XXX_390_SelectCourt")
    .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=issueAndSendToLocalCourtCallback1")
    .headers(Headers.xuiHeader)
    .header("Accept", "application/json, text/plain, */*")
    .header("x-xsrf-token", "#{XSRFToken}")
    .body(ElFileBody("bodies/prl/courtAdmin/PRLLocalCourt.json"))
    .check(jsonPath("$.data.courtList.value.code").is("234946:")))  //Value does not change for now. 

  .pause(MinThinkTime, MaxThinkTime)

  .exec(Common.activity)
  .exec(Common.userDetails)
  .exec(Common.activity)

  .exec(http("XUI_PRL_XXX_410_SubmitToLocalCourtEvent")
    .post(BaseURL + "/data/cases/#{caseId}/events")
    .headers(Headers.xuiHeader)
    .header("Accept", "application/json, text/plain, */*")
    .header("x-xsrf-token", "#{XSRFToken}")
    .body(ElFileBody("bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"))
    .check(jsonPath("$.data.courtList.value.code").is("234946:")))  //Value does not change for now. 

  .exec(http("XUI_PRL_XXX_420_SubmitToLocalCourtCompleteTask")
    .post(BaseURL + "/workallocation/task/#{taskId}/complete")
    .headers(Headers.navigationHeader)
    .header("Content-Type", "application/json")
    .header("x-xsrf-token", "#{XSRFToken}")
    .header("accept", "application/json") //No check available for this request
    .body(StringBody("""{"actionByEvent":true,"eventName":"Issue and send to local court"}""")))

  .pause(MinThinkTime, MaxThinkTime)

  val CourtAdminSendToGateKeeper = 

    exec(http("XUI_PRL_XXX_430_SelectCase")
      .get(BaseURL + "/cases/case-details/#{caseId}/task")
      .headers(Headers.xuiHeader)
      .check(substring("HMCTS Manage cases")))

    //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))

    .exec(Common.activity)
    .exec(Common.configUI)
    .exec(Common.configJson)
    .exec(Common.userDetails)

    .exec(http("XUI_PRL_XXX_440_SelectCaseTask")
      .get(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(jsonPath("$..[?(@.type=='sendToGateKeeperC100')].id").optional.saveAs("taskId"))
      .check(jsonPath("$[0].type").optional.saveAs("taskType"))
      )

    // Log task Type
      // .exec (session => {
      //   println(s"Current respTaskType: ${session("respTaskType").as[String]}")
      //   println(s"Current respTaskId: ${session("respTaskId").as[String]}")
      //   session
      // })

    //Save taskId from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("taskId").asOption[String] match {
        case Some(taskId) => session
        case None => session.set("taskId", "")
      }
    })

    // Loop until the task type matches "sendToGateKeeperC100"
    .asLongAs(session => session("taskId").asOption[String].forall(_.isEmpty)) {
      exec(http("XUI_PRL_XXX_445_SelectCaseTaskRepeat")
        .get(BaseURL + "/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$..[?(@.type=='sendToGateKeeperC100')].id").optional.saveAs("taskId")))

      .pause(5, 10) // Wait between retries
    
    } // end asLongAs



  .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Click on 'Send to Gate Keeper'
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_450_SendToGateKeeper")
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
      .headers(Headers.navigationHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.id").is("sendToGateKeeper")))

      .exec(Common.userDetails)

      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
      //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Add Gate Keeper
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_460_AddGateKeeper")
      .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLAddGateKeeper.json"))
      .check(substring("isJudgeOrLegalAdviserGatekeeping")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Send to Gate Keeper Submit
  ======================================================================================*/

    .group("XUI_PRL_XXX_470_GateKeeperSubmit") {
      exec(http("XUI_PRL_XXX_470_005_GateKeeperSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLAddGateKeeperSubmit.json"))
        .check(substring("gatekeepingDetails")))

      .exec(http("XUI_PRL_XXX_470_010_GateKeeperSubmitCompleteTask")
        .post(BaseURL + "/workallocation/task/#{taskId}/complete")
        .headers(Headers.xuiHeader)
        .header("accept", "application/json")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(StringBody("""{"actionByEvent":true,"eventName":"Send to Gatekeeper"}""")))

      .exec(http("XUI_PRL_XXX_470_015_SelectCase")
        .get(BaseURL + "/data/internal/cases/#{caseId}")
        .headers(Headers.xuiHeader)
        .check(jsonPath("$.case_type.name").is("C100 & FL401 Applications")))
    } 

    .pause(MinThinkTime, MaxThinkTime)

  val CourtAdminManageOrders = 

  exec(_.setAll(
      "PRLRandomString" -> (Common.randomString(7)),
      "PRLRandomPhone" -> (Common.randomNumber(8)),
      "PRLAppDobDay" -> Common.getDay(),
      "PRLAppDobMonth" -> Common.getMonth(),
      "JudgeFirstName" -> (Common.randomString(4) + "judgefirst"),
      "JudgeLastName" -> (Common.randomString(4) + "judgeLast"),
      "todayDate" -> Common.getDate(),
      "OrderDateYear" -> Common.getCurrentYear(),
      "OrderDateMonth" -> Common.getCurrentMonth(),
      "OrderDateDay" -> Common.getCurrentDay(),
      "LegalAdviserName" -> (Common.randomString(4) + " " + Common.randomString(4) + "legAdv")))

    /*======================================================================================
    * Click on 'Manage Orders'
    ======================================================================================*/

    .group("XUI_PRL_XXX_480_ManageOrders") {
      exec(http("XUI_PRL_XXX_480_005_ManageOrders")
        .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/manageOrders?ignore-warning=false")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("manageOrders")))

        .exec(Common.userDetails)
        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
        //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Create an Order
    ======================================================================================*/

    .group("XUI_PRL_XXX_490_CreateOrder") {
      exec(http("XUI_PRL_XXX_490_005_CreateOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders1")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLCreateOrder.json"))
        .check(substring("isSdoSelected")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select Order - Special guardianship order (C43A)
    ======================================================================================*/

    .group("XUI_PRL_XXX_500_SelectOrder") {
      exec(http("XUI_PRL_XXX_500_005_SelectOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSelectOrder.json"))
        .check(substring("otherPartyInTheCaseRevised")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Details
    ======================================================================================*/

    .group("XUI_PRL_XXX_510_OrderDetails") {
      exec(http("XUI_PRL_XXX_510_005_OrderDetails")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders5")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderDetails.json"))
        .check(jsonPath("$.data.appointedGuardianName[0].id").saveAs("guardianId"))
        .check(substring("isEngDocGen")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Guardian Name
    ======================================================================================*/

    .group("XUI_PRL_XXX_520_GuardianName") {
      exec(http("XUI_PRL_XXX_520_005_GuardianName")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders11")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLGuardianName.json"))
        .check(jsonPath("$.data.previewOrderDoc.document_url").saveAs("document_url"))
        .check(jsonPath("$.data.previewOrderDoc.document_hash").saveAs("document_hash"))
        .check(substring("previewOrderDoc")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Check Your Order
    ======================================================================================*/

    .group("XUI_PRL_XXX_530_CheckYourOrder") {
      exec(http("XUI_PRL_XXX_530_005_CheckYourOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders20")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLCheckOrder.json"))
        .check(substring("previewOrderDoc")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Recipients
    ======================================================================================*/

    .group("XUI_PRL_XXX_540_OrderRecipients") {
      exec(http("XUI_PRL_XXX_540_005_OrderRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders24")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderRecipients.json"))
        .check(substring("amendOrderSelectCheckOptions")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Serve
    ======================================================================================*/

    .group("XUI_PRL_XXX_550_OrderServe") {
      exec(http("XUI_PRL_XXX_550_005_OrderServe")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders26")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderServe.json"))
        .check(jsonPath("$.data.serveOrderDynamicList.value[0].code").saveAs("orderCode"))
        .check(jsonPath("$.data.serveOrderDynamicList.value[0].label").saveAs("orderLabel"))
        .check(substring("orderRecipients")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order To Serve List
    ======================================================================================*/

    .group("XUI_PRL_XXX_560_OrderServe") {
      exec(http("XUI_PRL_XXX_560_005_OrderToServeList")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders27")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderToServeList.json"))
        .check(substring("otherPartyInTheCaseRevised")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Serve to Respondent Options
    ======================================================================================*/

    .group("XUI_PRL_XXX_570_ServeToRespondentOptions") {
      exec(http("XUI_PRL_XXX_570_005_ServeToRespondentOptions")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders28")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderToServeRespondentOptions.json"))
        .check(substring("otherPartyInTheCaseRevised")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Order Submit
    ======================================================================================*/

    .group("XUI_PRL_XXX_580_OrderSubmit") {
      exec(http("XUI_PRL_XXX_580_005_OrderSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderSubmit.json"))
        .check(substring("JUDICIAL_REVIEW")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  val CourtAdminServiceApplication =

    /*======================================================================================
    * Click on 'Service of Application'
    ======================================================================================*/

    group("XUI_PRL_XXX_590_ServiceOfApplication") {
      exec(http("XUI_PRL_XXX_590_005_ServiceOfApplication")
        .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/serviceOfApplication?ignore-warning=false")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.case_fields[7].value.list_items[0].code")saveAs("serviceOfApplicationScreenCode"))
        .check(jsonPath("$.case_fields[7].value.list_items[0].label")saveAs("serviceOfApplicationScreenLabel"))
        .check(jsonPath("$.case_fields[30].value.list_items[0].code")saveAs("serviceOfApplicationApplicantCode"))
        .check(jsonPath("$.case_fields[30].value.list_items[0].label")saveAs("serviceOfApplicationApplicantName"))
        .check(jsonPath("$.case_fields[30].value.list_items[1].code")saveAs("serviceOfApplicationRespondentCode"))
        .check(jsonPath("$.case_fields[30].value.list_items[1].label")saveAs("serviceOfApplicationRespondentName"))
        .check(jsonPath("$.id").is("serviceOfApplication")))

        .exec(Common.userDetails)

        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).withSecure(true).saveAs("XSRFToken")))
        //.exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * PD36Q letter Upload
    ======================================================================================*/

    .group("XUI_PRL_XXX_600_PD36QUpload") {
      exec(http("XUI_PRL_XXX_600_005_PD36QUpload")
        .post(BaseURL + "/documentsv2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
      .header("x-xsrf-token", "#{XSRFToken}")
      .bodyPart(RawFileBodyPart("files", "TestFile.pdf")
        .fileName("TestFile.pdf")
        .transferEncoding("binary"))
      .asMultipartForm
      .formParam("classification", "PUBLIC")
      .formParam("caseTypeId", "PRLAPPS")
      .formParam("jurisdictionId", "PRIVATELAW")
      .check(substring("originalDocumentName"))
      .check(jsonPath("$.documents[0].hashToken").saveAs("documentHashPD36Q"))
      .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLPD36Q")))
     }

    .pause(MinThinkTime, MaxThinkTime)

  .exec(session => {
  for (_ <- 1 to 10) { // Adjust the loop count as needed
    Thread.sleep(500)  // 500ms delay per iteration
    println("Debug: Delaying the script...")
  }
  session
  })

    

  /*======================================================================================
  * Special arrangements letter  Upload
  ======================================================================================*/

    .group("XUI_PRL_XXX_610_SpecialArrangementsUpload") {
      exec(http("XUI_PRL_XXX_610_005_SpecialArrangementsUpload")
        .post(BaseURL + "/documentsv2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "#{XSRFToken}")
        .bodyPart(RawFileBodyPart("files", "TestFile2.pdf")
          .fileName("TestFile2.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "PRLAPPS")
        .formParam("jurisdictionId", "PRIVATELAW")
        .check(substring("originalDocumentName"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("documentHashSpecial"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLSpecial")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application document uploads
  ======================================================================================*/

    .group("XUI_PRL_XXX_620_DocumentUpload") {
      exec(http("XUI_PRL_620_005_DocumentUpload")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplication2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoADocuments.json"))
        .check(substring("additionalDocuments")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application Confirm recipients
  ======================================================================================*/

    .group("XUI_PRL_XXX_630_ServiceRecipients") {
      exec(http("XUI_PRL_XXX_630_005_ServiceRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplication4")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoARecipients.json"))
        .check(substring("otherPartyInTheCaseRevised")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application Submit
  ======================================================================================*/

    .group("XUI_PRL_XXX_640_ServiceSubmit") {
      exec(http("XUI_PRL_XXX_640_005_ServiceSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoASubmit.json"))
        .check(jsonPath("$.data.caseInvites[0].value.accessCode").saveAs("prlAccessCodeApplicant"))
        .check(jsonPath("$.data.caseInvites[1].value.accessCode").saveAs("prlAccessCodeRespondent")))
    }


//Write applicant access code to file
    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("C100caseNumberAndCodeApplicant.csv", true))
      try {
        fw.write(session("caseId").as[String] + "," + session("prlAccessCodeApplicant").as[String] + "\r\n")
      } finally fw.close()
      session
    }
    //Write respondent access code to file
    .exec { session =>
      val fw = new BufferedWriter(new FileWriter("C100caseNumberAndCodeRespondent.csv", true))
      try {
        fw.write(session("caseId").as[String] + "," + session("prlAccessCodeRespondent").as[String] + "\r\n")
      } finally fw.close()
      session
    }


 val CourtAdminServiceApplicationExtract =
   /*======================================================================================
   * Click on 'Service of Application'
   ======================================================================================*/
   group("XUI_PRL_XXX_590_ServiceOfApplication") {
     exec(http("XUI_PRL_XXX_590_005_ServiceOfApplication")
       .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/serviceOfApplication?ignore-warning=false")
       .headers(Headers.navigationHeader)
       .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
       .check(jsonPath("$.event_token").saveAs("event_token"))
       .check(jsonPath("$.case_fields[7].value.list_items[0].code")saveAs("serviceOfApplicationScreenCode"))
       .check(jsonPath("$.case_fields[7].value.list_items[0].label")saveAs("serviceOfApplicationScreenLabel"))
       .check(jsonPath("$.case_fields[30].value.list_items[0].code")saveAs("serviceOfApplicationApplicantCode"))
       .check(jsonPath("$.case_fields[30].value.list_items[0].label")saveAs("serviceOfApplicationApplicantName"))
       .check(jsonPath("$.case_fields[30].value.list_items[1].code")saveAs("serviceOfApplicationRespondentCode"))
       .check(jsonPath("$.case_fields[30].value.list_items[1].label")saveAs("serviceOfApplicationRespondentName"))
       .check(jsonPath("$.id").is("serviceOfApplication")))

  .pause(MinThinkTime, MaxThinkTime)

  }
}
