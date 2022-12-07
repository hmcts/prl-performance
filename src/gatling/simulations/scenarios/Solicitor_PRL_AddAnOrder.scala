package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}
import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_AddAnOrder {
  
  val BaseURL = Environment.baseURL
  val prlURL = "https://privatelaw.${env}.platform.hmcts.net"
  val IdamUrl = Environment.idamURL
  val PRLcases = csv("cases.csv").circular


  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime



  val AddAnOrder =


  /*======================================================================================
  * Select case
  ======================================================================================*/

    group("XUI_PRL_030_SelectCase") {

      exec(_.setAll(
        "PRLRandomString" -> (Common.randomString(7)),
        "PRLAppDobDay" -> Common.getDay(),
        "PRLAppDobMonth" -> Common.getMonth(),
        "PRLAppDobYear" -> Common.getDobYear()))

        .feed(PRLcases)

        .exec(http("XUI_PRL_030_005_SelectCase")
          .get(BaseURL + "/data/internal/cases/${caseId}")
          .headers(Headers.navigationHeader)
          .header("accept", "application/json"))
        //  .check(substring("PRIVATELAW")))

        .exec(Common.userDetails)
    }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Select 'Issue and send to local court'
      ======================================================================================*/

      .group("XUI_PRL_040_SelectIssue") {
        exec(http("XUI_PRL_040_005_SelectIssue")
          .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/issueAndSendToLocalCourtCallback?ignore-warning=false")
          .headers(Headers.navigationHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("event_token"))
          //   .check(jsonPath("$.case_fields[0].formatted_value[0].id").saveAs("local_Court_Id"))
          .check(jsonPath("$.id").is("issueAndSendToLocalCourtCallback")))

          .exec(Common.userDetails)

          .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
      }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      *Add local court Admin
      ======================================================================================*/

      .group("XUI_PRL_050_LocalCourt") {
        exec(http("XUI_PRL_050_005_LocalCourt")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=issueAndSendToLocalCourtCallback1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLLocalCourt.json"))
          .check(substring("localCourtAdmin")))

          .exec(Common.userDetails)
      }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Issue and send to local court submit
      ======================================================================================*/

      .group("XUI_PRL_060_LocalCourtSubmit") {
        exec(http("XUI_PRL_060_005_LocalCourtSubmit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLLocalCourtSubmit.json"))
          .check(substring("CASE_ISSUE")))

          .exec(Common.userDetails)
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Click on 'Send to Gate Keeper'
      ======================================================================================*/

      .group("XUI_PRL_070_SendToGateKeeper") {
        exec(http("XUI_PRL_070_005_SendToGateKeeper")
          .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/sendToGateKeeper?ignore-warning=false")
          .headers(Headers.navigationHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("event_token"))
          .check(jsonPath("$.case_fields[0].formatted_value[0].id").saveAs("gateKeeper_id"))
          .check(jsonPath("$.id").is("sendToGateKeeper")))

          .exec(Common.userDetails)

          .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
      }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Add Gate Keeper
      ======================================================================================*/

      .group("XUI_PRL_080_AddGateKeeper") {
        exec(http("XUI_PRL_080_005_AddGateKeeper")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=sendToGateKeeper1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLAddGateKeeper.json"))
          .check(substring("gatekeeper")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Send to Gate Keeper Submit
======================================================================================*/

      .group("XUI_PRL_090_GateKeeperSubmit") {
        exec(http("XUI_PRL_090_005_GateKeeperSubmit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLAddGateKeeperSubmit.json"))
          .check(substring("GATE_KEEPING")))
      }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Click on 'Manage Orders'
      ======================================================================================*/

      .group("XUI_PRL_100_ManageOrders") {
        exec(http("XUI_PRL_100_005_ManageOrders")
          .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/manageOrders?ignore-warning=false")
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

      .group("XUI_PRL_110_CreateOrder") {
        exec(http("XUI_PRL_110_005_reateOrder")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLCreateOrder.json"))
          .check(substring("SearchCriteria")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Select Order - Special guardianship order (C43A)
======================================================================================*/

      .group("XUI_PRL_120_SelectOrder") {
        exec(http("XUI_PRL_120_005_SelectOrder")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders2")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLSelectOrder.json"))
          .check(substring("abductionChildHasPassport")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Order Details
======================================================================================*/

      .group("XUI_PRL_130_OrderDetails") {
        exec(http("XUI_PRL_130_005_OrderDetails")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders4")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLOrderDetails.json"))
          .check(substring("isEngDocGen")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Guardian Name
======================================================================================*/

      .group("XUI_PRL_140_GuardianName") {
        exec(http("XUI_PRL_140_005_GuardianName")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders10")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLGuardianName.json"))
          .check(jsonPath("$.data.previewOrderDoc.document_url").saveAs("document_url"))
          .check(jsonPath("$.data.previewOrderDoc.document_hash").saveAs("document_hash"))
          .check(substring("previewOrderDoc")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Check Your Order
======================================================================================*/

      .group("XUI_PRL_150_CheckYourOrder") {
        exec(http("XUI_PRL_150_005_CheckYourOrder")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders16")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLCheckOrder.json"))
          .check(substring("previewOrderDoc")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Order Recipients
======================================================================================*/

      .group("XUI_PRL_160_OrderRecipients") {
        exec(http("XUI_PRL_160_005_OrderRecipients")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=manageOrders17")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLOrderRecipients.json"))
          .check(substring("orderRecipients")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Order Submit
======================================================================================*/

      .group("XUI_PRL_170_OrderSubmit") {
        exec(http("XUI_PRL_170_005_OrderSubmit")
          .post(BaseURL + "/data/cases/${caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("x-xsrf-token", "${XSRFToken}")
          .body(ElFileBody("bodies/prl/c100Continued/PRLOrderSubmit.json"))
          .check(substring("GATE_KEEPING")))
      }

      .pause(MinThinkTime, MaxThinkTime)




      /*======================================================================================
    * Click on 'Service of Application'
    ======================================================================================*/

      .group("XUI_PRL_180_ServiceOfApplication") {
        exec(http("XUI_PRL_180_005_ServiceOfApplication")
          .get(BaseURL + "/data/internal/cases/${caseId}/event-triggers/serviceOfApplication?ignore-warning=false")
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

      .group("XUI_PRL_190_PD36QUpload") {
        exec(http("XUI_PRL_190_005_PD36QUpload")
          .post("/documentsv2")
          .headers(Headers.commonHeader)


          .header("accept", "application/json, text/plain, */*")
          .header("content-type", "multipart/form-data")

        .header("x-xsrf-token", "${XSRFToken}")
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

    .group("XUI_PRL_200_SpecialArrangementsUpload") {
      exec(http("XUI_PRL_200_005_SpecialArrangementsUpload")
        .post("/documentsv2")
        .headers(Headers.commonHeader)


        .header("accept", "application/json, text/plain, */*")

        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "${XSRFToken}")
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

    .group("XUI_PRL_210_AdditionalDocumentsUpload") {
      exec(http("XUI_PRL_210_005_AdditionalDocumentsUpload")
        .post("/documentsv2")
        .headers(Headers.commonHeader)


         .header("accept", "application/json, text/plain, */*")

        .header("content-type", "multipart/form-data")
        .header("x-xsrf-token", "${XSRFToken}")
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
* Service of APplication document uploads
======================================================================================*/

    .group("XUI_PRL_220_DocumentUpload") {
      exec(http("XUI_PRL_220_005_DocumentUpload")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplicationorderDetails")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100Continued/PRLSoADocuments.json"))
        .check(substring("additionalDocuments")))
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
* Service of APplication Confirm recipients
======================================================================================*/

    .group("XUI_PRL_230_ServiceRecipients") {
      exec(http("XUI_PRL_230_005_ServiceRecipients")
        .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=serviceOfApplicationconfirmRecipients")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100Continued/PRLSoARecipients.json"))
        .check(substring("confirmRecipients")))
    }

    .pause(MinThinkTime, MaxThinkTime)


    /*======================================================================================
* Service of Application Submit
======================================================================================*/

    .group("XUI_PRL_240_ServiceSubmit") {
      exec(http("XUI_PRL_240_005_ServiceSubmit")
        .post(BaseURL + "/data/cases/${caseId}/events")
        .headers(Headers.commonHeader)
        .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
        .header("x-xsrf-token", "${XSRFToken}")
        .body(ElFileBody("bodies/prl/c100Continued/PRLSoASubmit.json"))
        .check(regex("""accessCode":"(\w{8})""").saveAs("prlAccessCode"))
    . check(substring("CASE_HEARING")))

        .exec { session =>
          val fw = new BufferedWriter(new FileWriter("accessCode.csv", true))
          try {
            fw.write(session("prlAccessCode").as[String] + "\r\n")
          } finally fw.close()
          session
        }
}

.pause(MinThinkTime, MaxThinkTime)






}