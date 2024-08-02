package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*===============================================================================================================
* Court Admin C100 case progression. Send to local court --> Sent to Gatekeeper --> Add an order --> Serve 
================================================================================================================*/

object Solicitor_PRL_C100_ProgressCase {
  
  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CourtAdminCheckApplication =

    exec(http("XUI_PRL_XXX_290_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .check(jsonPath("$.case_id").is("#{caseId}")))

    .exec(Common.waJurisdictions)
    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.caseActivityGet)
    .exec(Common.isAuthenticated)

    .pause(MinThinkTime, MaxThinkTime)

   /*=====================================================================================
   * Select task tab 
   ======================================================================================*/

    .exec(http("XUI_PRL_XXX_300_SelectCase")
      .get(BaseURL + "/cases/case-details/#{caseId}/task")
      .headers(Headers.xuiHeader)
      .check(substring("HMCTS Manage cases")))

    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
      
    .exec(Common.activity)
    .exec(Common.configUI)
    .exec(Common.configJson)
    .exec(Common.userDetails)

    .exec(http("XUI_PRL_XXX_310_SelectCaseTask")
      .get(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(regex(""""id":"(.*)","name":""").optional.saveAs("respTaskId"))
      .check(regex(""","type":"(.*)","task_state":""").optional.saveAs("respTaskType")))

    //Save taskType from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("respTaskType").asOption[String] match {
        case Some(taskType) => session
        case None => session.set("respTaskType", "")
      }
    })

    // Loop until the task type matches "checkApplicationC100"
    .asLongAs(session => session("respTaskType").as[String] != "checkApplicationC100") {
      exec(http("XUI_PRL_XXX_310_SelectCaseTaskRepeat")
        .get(BaseURL + "/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(regex(""""id":"(.*)","name":""").optional.saveAs("respTaskId"))
        .check(regex(""","type":"(.*)","task_state":""").optional.saveAs("respTaskType")))

      .pause(5, 10) // Wait between retries

      // Log task Type
      .exec (session => {
        println(s"Current respTaskType: ${session("respTaskType").as[String]}")
        session
    })
  }

    .exec(Common.userDetails)
    .exec(Common.waUsersByServiceName)
    .exec(Common.caseActivityGet)
    .exec(Common.monitoringTools)
    .exec(Common.isAuthenticated)

    .exec(http("XUI_PRL_XXX_320_SelectCase")
      .get(BaseURL + "/data/internal/cases/#{caseId}")
      .headers(Headers.xuiHeader)
      .check(jsonPath("$.case_type.name").is("C100 & FL401 Applications")))

    .exec(Common.activity)

    .pause(MinThinkTime, MaxThinkTime)

/*=====================================================================================
* Select Assign to me
======================================================================================*/

    .exec(http("XUI_PRL_XXX_330_AssignToMeClaim")
      .post(BaseURL + "/workallocation/task/#{respTaskId}/claim")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(status.in(200, 204)))

    .exec(http("XUI_PRL_XXX_340_AssignToMe")
      .post(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("""{"refined":true}"""))
      .check(regex("""","assignee":"(.*)","type":""").saveAs("asigneeUserId"))
      .check(regex("""","task_state":"(.*)","task_system":"""").is("assigned")))

    .exec(Common.isAuthenticated)
    .exec(Common.caseActivityPost)
    .exec(Common.caseActivityOnlyGet)

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Select Issue and send to local Court
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_360_IssueAndSendToLocalCourt")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/issueAndSendToLocalCourtCallback/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json")
      .check(jsonPath("$.task_required_for_event").is("false")))

    .exec(Common.activity)
    .exec(Common.profile)

    .exec(http("XUI_PRL_XXX_370_IssueAndSendToLocalCourtEventTrigger")  //*** SAVE THE Courtlist response here for use in later post requests **
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/issueAndSendToLocalCourtCallback?ignore-warning=false")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.id").is("issueAndSendToLocalCourtCallback"))
      //.check(jsonPath("$.case_fields[1].value.list_items").saveAs("courtListItems"))
      .check(status.in(200, 403)))

    .exec(http("XUI_PRL_XXX_380_IssueAndSendToLocalCourtEvent")
      .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/issueAndSendToLocalCourtCallback/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json"))
      //.check(substring("PRIVATELAW")))
    
    .exec(Common.caseActivityPost)
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

    .exec(http("XUI_PRL_XXX_400_SubmitToLocalCourt")
      .get(BaseURL + "/workallocation/task/#{respTaskId}")
      .headers(Headers.navigationHeader)
      .header("accept", "application/json")
      //.check(jsonPath("$.case_fields[0].formatted_value[0].id").saveAs("gateKeeper_id"))
      .check(regex("""","task_state":"(.*)","task_system":"""").is("assigned")))

    .exec(http("XUI_PRL_XXX_410_SubmitToLocalCourtEvent")
      .post(BaseURL + "/data/cases/#{caseId}/events")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLLocalCourtSubmit.json"))
      .check(jsonPath("$.data.courtList.value.code").is("234946:")))  //Value does not change for now. 

    .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
   * Select Complete task
   ======================================================================================*/

    .exec(http("XUI_PRL_XXX_350_SelectCaseTasks&Complete")
      .post(BaseURL + "/workallocation/case/task/#{caseId}/complete")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("""{"hasNoAssigneeOnComplete":false}""")))

    .exec(Common.isAuthenticated)
    .exec(Common.manageLabellingRoleAssignment)
    .exec(Common.waJurisdictions)
 
    .exec(http("XUI_PRL_XXX_360_MarkAsDone")
      .post(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("""{"refined":true}"""))
      .check(regex("""","assignee":"(.*)","type":""").saveAs("asigneeUserId"))
      .check(regex("""","task_state":"(.*)","task_system":"""").is("assigned")))

    .exec(Common.activity)
    .exec(Common.userDetails)
    .exec(Common.activity)

    .pause(MinThinkTime, MaxThinkTime)

val CourtAdminSendToGateKeeper = 

    exec(http("XUI_PRL_XXX_300_SelectCase")
      .get(BaseURL + "/cases/case-details/#{caseId}/task")
      .headers(Headers.xuiHeader)
      .check(substring("HMCTS Manage cases")))

    .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
      
    .exec(Common.activity)
    .exec(Common.configUI)
    .exec(Common.configJson)
    .exec(Common.userDetails)

    .exec(http("XUI_PRL_XXX_310_SelectCaseTask")
      .get(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(jsonPath("$[1].id").optional.saveAs("respTaskId"))
      .check(jsonPath("$[1].type").optional.saveAs("respTaskType")))

    // Log task Type
    .exec (session => {
      println(s"Current respTaskType: ${session("respTaskType").as[String]}")
      println(s"Current respTaskId: ${session("respTaskId").as[String]}")
      session
    })

    //Save taskType from response
    .exec(session => {
      // Initialise task type in session if it's not already present, ensure the variable exists before entering Loop
      session("respTaskType").asOption[String] match {
        case Some(taskType) => session
        case None => session.set("respTaskType", "")
      }
    })

    // Loop until the task type matches "checkApplicationC100"
    .asLongAs(session => session("respTaskType").as[String] != "sendToGateKeeperC100") {
      exec(http("XUI_PRL_XXX_310_SelectCaseTaskRepeat")
        .get(BaseURL + "/workallocation/case/task/#{caseId}")
        .headers(Headers.xuiHeader)
        .header("Accept", "application/json, text/plain, */*")
        .header("x-xsrf-token", "#{XSRFToken}")
        .check(jsonPath("$[1].id").optional.saveAs("respTaskId"))
        .check(jsonPath("$[1].type").optional.saveAs("respTaskType")))

      .pause(5, 10) // Wait between retries

      // Log task Type
      .exec (session => {
        println(s"Current respTaskType: ${session("respTaskType").as[String]}")
        println(s"Current respTaskId: ${session("respTaskId").as[String]}")
        session
    })
  }

  .pause(MinThinkTime, MaxThinkTime)

  /*=====================================================================================
  * Select Assign to me
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_330_AssignToMeClaim")
      .post(BaseURL + "/workallocation/task/#{respTaskId}/claim")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .check(status.in(200, 204)))

    .exec(http("XUI_PRL_XXX_340_AssignToMe")
      .post(BaseURL + "/workallocation/case/task/#{caseId}")
      .headers(Headers.xuiHeader)
      .header("Accept", "application/json, text/plain, */*")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(StringBody("""{"refined":true}"""))
      // .check(regex("""","assignee":"(.*)","type":""").saveAs("asigneeUserId"))
      .check(jsonPath("$[1].assignee").saveAs("assigneeUserId"))
      // .check(regex("""","task_state":"(.*)","task_system":"""").is("assigned"))
      .check(jsonPath("$[1].task_state").is("assigned"))
      )

    .exec(Common.isAuthenticated)
    .exec(Common.caseActivityPost)
    .exec(Common.caseActivityOnlyGet)

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Click on 'Send to Gate Keeper'
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_420_SendToGateKeeper")
      .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
      .headers(Headers.navigationHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
      //.check(jsonPath("$.case_fields[0].formatted_value[0].id").saveAs("gateKeeper_id"))
      .check(jsonPath("$.event_token").saveAs("event_token"))
      .check(jsonPath("$.id").is("sendToGateKeeper")))

      .exec(Common.userDetails)
      .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Add Gate Keeper
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_430_AddGateKeeper")
      .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLAddGateKeeper.json"))
      .check(substring("gatekeeper")))

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Send to Gate Keeper Submit
  ======================================================================================*/

    .exec(http("XUI_PRL_XXX_440_GateKeeperSubmit")
      .post(BaseURL + "/data/cases/#{caseId}/events")
      .headers(Headers.xuiHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
      .header("x-xsrf-token", "#{XSRFToken}")
      .body(ElFileBody("bodies/prl/courtAdmin/PRLAddGateKeeperSubmit.json"))
      .check(substring("GATE_KEEPING")))

    .pause(MinThinkTime, MaxThinkTime)


  val CourtAdminManageOrders = 

  /*======================================================================================
  * Click on 'Manage Orders'
  ======================================================================================*/

    group("XUI_PRL_XXX_450_ManageOrders") {
      exec(http("XUI_PRL_XXX_450_005_ManageOrders")
        .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/manageOrders?ignore-warning=false")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("manageOrders")))

        .exec(Common.userDetails)
        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Create an Order
  ======================================================================================*/

    .group("XUI_PRL_XXX_460_CreateOrder") {
      exec(http("XUI_PRL_XXX_460_005_reateOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders1")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLCreateOrder.json"))
        .check(substring("SearchCriteria")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Select Order - Special guardianship order (C43A)
  ======================================================================================*/

    .group("XUI_PRL_XXX_470_SelectOrder") {
      exec(http("XUI_PRL_XXX_470_005_SelectOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders2")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin//PRLSelectOrder.json"))
        .check(substring("abductionChildHasPassport")))
    }

   .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Order Details
  ======================================================================================*/

      .group("XUI_PRL_XXX_480_OrderDetails") {
        exec(http("XUI_PRL_XXX_480_005_OrderDetails")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders4")
          .headers(Headers.xuiHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "#{XSRFToken}")
          .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderDetails.json"))
          .check(substring("isEngDocGen")))
      }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Guardian Name
  ======================================================================================*/

      .group("XUI_PRL_XXX_490_GuardianName") {
        exec(http("XUI_PRL_XXX_490_005_GuardianName")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders10")
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

    .group("XUI_PRL_XXX_500_CheckYourOrder") {
      exec(http("XUI_PRL_XXX_500_005_CheckYourOrder")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders16")
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

    .group("XUI_PRL_XXX_510_OrderRecipients") {
      exec(http("XUI_PRL_XXX_510_005_OrderRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders17")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderRecipients.json"))
        .check(substring("orderRecipients")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Order Submit
  ======================================================================================*/

    .group("XUI_PRL_XXX_520_OrderSubmit") {
      exec(http("XUI_PRL_XXX_520_005_OrderSubmit")
        .post(BaseURL + "/data/cases/#{caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLOrderSubmit.json"))
        .check(substring("GATE_KEEPING")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Click on 'Service of Application'
  ======================================================================================*/

    .group("XUI_PRL_XXX_530_ServiceOfApplication") {
      exec(http("XUI_PRL_XXX_530_005_ServiceOfApplication")
        .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/serviceOfApplication?ignore-warning=false")
        .headers(Headers.navigationHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
        .check(jsonPath("$.event_token").saveAs("event_token"))
        .check(jsonPath("$.id").is("serviceOfApplication")))

        .exec(Common.userDetails)

        .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * PD36Q letter Upload
  ======================================================================================*/

    .group("XUI_PRL_XXX_540_PD36QUpload") {
      exec(http("XUI_PRL_XXX_540_005_PD36QUpload")
        .post("/documentsv2")
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

  /*======================================================================================
  * Special arrangements letter  Upload
  ======================================================================================*/

    .group("XUI_PRL_XXX_550_SpecialArrangementsUpload") {
      exec(http("XUI_PRL_XXX_550_005_SpecialArrangementsUpload")
        .post("/documentsv2")
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
  * Additional documents Upload
  ======================================================================================*/

    .group("XUI_PRL_XXX_560_AdditionalDocumentsUpload") {
      exec(http("XUI_PRL_XXX_560_005_AdditionalDocumentsUpload")
        .post("/documentsv2")
        .headers(Headers.xuiHeader)
         .header("accept", "application/json, text/plain, */*")
        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "#{XSRFToken}")
        .bodyPart(RawFileBodyPart("files", "TestFile3.pdf")
          .fileName("TestFile3.pdf")
          .transferEncoding("binary"))
        .asMultipartForm
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "PRLAPPS")
        .formParam("jurisdictionId", "PRIVATELAW")
        .check(substring("originalDocumentName"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("documentHashAdditional"))
        .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLAdditional")))
    }

    .pause(MinThinkTime, MaxThinkTime)
    //Need to change these files when perftest acc works

  /*======================================================================================
  * Service of Application document uploads
  ======================================================================================*/

    .group("XUI_PRL_XXX_570_DocumentUpload") {
      exec(http("XUI_PRL_570_005_DocumentUpload")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplicationorderDetails")
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

    .group("XUI_PRL_XXX_580_ServiceRecipients") {
      exec(http("XUI_PRL_XXX_580_005_ServiceRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplicationconfirmRecipients")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoARecipients.json"))
        .check(substring("confirmRecipients")))
    }

    .pause(MinThinkTime, MaxThinkTime)

  /*======================================================================================
  * Service of Application Submit
  ======================================================================================*/

    .group("XUI_PRL_XXX_590_ServiceSubmit") {
      exec(http("XUI_PRL_XXX_590_005_ServiceSubmit")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.xuiHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "#{XSRFToken}")
        .body(ElFileBody("bodies/prl/courtAdmin/PRLSoASubmit.json"))
        .check(regex("""accessCode":"(\w{8})""").saveAs("prlAccessCode")))
    }
}