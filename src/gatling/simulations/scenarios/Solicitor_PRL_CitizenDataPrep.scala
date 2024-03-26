package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common,CsrfCheck, Environment, Headers, CsrfCheck2}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_CitizenDataPrep {
  
  val BaseURL = Environment.baseURL
  val prlURL = "https://privatelaw.#{env}.platform.hmcts.net"
  val IdamUrl = Environment.idamURL
  val payUrl = "https://card.payments.service.gov.uk"



  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime





  val CompleteDataPrep =


  /*======================================================================================
  * Create Case Start
  ======================================================================================*/

    group("XUI_PRL_030_CreateCaseStart") {

      exec(_.setAll(
        "PRLRandomString" -> (Common.randomString(7)),
        "PRLAppDobDay" -> Common.getDay(),
        "PRLAppDobMonth" -> Common.getMonth(),
        "PRLAppDobYear" -> Common.getDobYear()))


        .exec(http("XUI_PRL_030_005_CreateCaseStart")
          .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/testingSupportDummySolicitorCreate?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("event_token"))
          .check(substring("TS-Solicitor application")))

    }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Type of application - C100
    ======================================================================================*/

      .group("XUI_PRL_031_Type_Of_Application") {

          exec(http("XUI_PRL_031_005_Type_Of_Application")
            .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=testingSupportDummySolicitorCreate2")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
            .body(ElFileBody("bodies/prl/PRLDataPrep/PRLTypeOfApplication.json"))
            .check(substring("C100")))

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Check Your Answers
======================================================================================*/

      .group("XUI_PRL_032_ApplicationCheckYourAnswers") {

        exec(http("XUI_PRL_032_005_ApplicationCheckYourAnswers")
          .post(BaseURL + "/data/case-types/PRLAPPS/cases?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-case.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/PRLApplicationCheckYourAnswers.json"))
      /*    .check(jsonPath("$.data.applicants[0].id").saveAs("applicant1Id"))
          .check(jsonPath("$.data.applicants[0].value.solicitorOrgUuid").saveAs("applicant1solicitorOrgUuid"))
          .check(jsonPath("$.data.applicants[0].value.solicitorPartyId").saveAs("applicant1solicitorPartyId"))
          .check(jsonPath("$.data.applicants[1].id").saveAs("applicant2Id"))
          .check(jsonPath("$.data.applicants[1].value.solicitorOrgUuid").saveAs("applicant2solicitorOrgUuid"))
          .check(jsonPath("$.data.applicants[1].value.solicitorPartyId").saveAs("applicant2solicitorPartyId"))
          .check(jsonPath("$.data.applicants[2].id").saveAs("applicant3Id"))
          .check(jsonPath("$.data.applicants[2].value.solicitorOrgUuid").saveAs("applicant3solicitorOrgUuid"))
          .check(jsonPath("$.data.applicants[2].value.solicitorPartyId").saveAs("applicant3solicitorPartyId"))

       */
          .check(jsonPath("$.id").saveAs("caseId")))

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Select Application Details
======================================================================================*/

      .group("XUI_PRL_033_ApplicationDetails") {

        exec(http("XUI_PRL_033_005_ApplicationDetails")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/applicantsDetails?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .check(jsonPath("$.event_token").saveAs("event_token"))
          .check(substring("Details of the applicants in the case")))

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Enter Application Details
======================================================================================*/

      .group("XUI_PRL_034_EnterApplicationDetails") {

        exec(http("XUI_PRL_034_005_EnterApplicationDetails")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=applicantsDetails1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/PRLApplicantDetails.json"))
          .check(substring("applicants")))

      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
* Submit Application Details
======================================================================================*/

      .group("XUI_PRL_035_SubmitApplicationDetails") {

        exec(http("XUI_PRL_035_005_SubmitApplicationDetails")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/PRLApplicantDetailsSubmit.json"))
          .check(substring("CALLBACK_COMPLETED")))

      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
* Submit and pay
======================================================================================*/

      .group("XUI_PRL_036_SubmitAndPay") {

        exec(http("XUI_PRL_036_005_SubmitAndPay")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/submitAndPay?ignore-warning=false")
          .headers(Headers.navigationHeader)
          .check(jsonPath("$.event_token").saveAs("event_token"))
        //  .check(jsonPath("$.case_fields[6].formatted_value.document_hash").saveAs("Document_HashWelsh"))
        //  .check(jsonPath("$.case_fields[6].formatted_value.document_url").saveAs("Document_urlWelsh"))
          .check(jsonPath("$.case_fields[11].formatted_value.document_hash").saveAs("Document_HashApp"))
          .check(jsonPath("$.case_fields[11 ].formatted_value.document_url").saveAs("Document_urlApp")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Confidentiality Statement
======================================================================================*/

      .group("XUI_PRL_037_ConfidentialityStatement") {

        exec(http("XUI_PRL_037_005_ConfidentialityStatement")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=submitAndPay1")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/ConfidentialityStatement.json"))
          .check(substring("applicantSolicitorEmailAddress")))
      }

      .pause(MinThinkTime, MaxThinkTime)




      /*======================================================================================
* Declaration
======================================================================================*/

        .group("XUI_PRL_038_Declaration") {

        exec(http("XUI_PRL_038_005_Declaration")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=submitAndPay2")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/Declaration.json"))
          .check(substring("feeAmount")))
      }

      .pause(MinThinkTime, MaxThinkTime)




      /*======================================================================================
* Continue
======================================================================================*/

      .group("XUI_PRL_039_Continue") {

        exec(http("XUI_PRL_039_005_Continue")
          .post(BaseURL + "/data/case-types/PRLAPPS/validate?pageId=submitAndPay3")
      //    .disableFollowRedirect
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .header("content-type", "application/vnd.uk.gov.hmcts.ccd-data-store-api.case-data-validate.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/Continue.json"))
          .check(substring("paymentServiceRequestReferenceNumber")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Continue
======================================================================================*/

      .group("XUI_PRL_040_Continue") {

        exec(http("XUI_PRL_039_005_Continue")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .header("content-type", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/PRLDataPrep/paymentServiceRequestReferenceNumber.json"))
          .check(jsonPath("$.data.paymentServiceRequestReferenceNumber").saveAs("paymentServiceRequestReferenceNumber"))
        )
      }

      .pause(MinThinkTime, MaxThinkTime)




    //  .group("XUI_PRL_041_Continue") {

     //   exec(http("XUI_PRL_040_005_Continue")
     //     .get(BaseURL + "/payments/cases/#{caseId}/paymentgroups")
     //     .headers(Headers.commonHeader)
     //     .header("accept", "application/json, text/plain, */*")
     //     .check(substring("payment_group_reference"))
       //   .check(jsonPath("$.payment_groups[0].payment_group_reference").saveAs("payment_group_reference"))
     //   )
     // }

      //.pause(MinThinkTime, MaxThinkTime)






      /*======================================================================================
* Dummy Payment Confirmation
======================================================================================*/

      .group("XUI_PRL_041_DummyPaymentConfirmation ") {

        exec(http("XUI_PRL_041_005_DummyPaymentConfirmation ")
          .get(BaseURL + "/payments/pba-accounts")
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
        )
      //    .check(jsonPath("$.event_token").saveAs("event_token")))
      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Choose Payment option
======================================================================================*/

      .group("XUI_PRL_042_PaymentOption") {

        exec(http("XUI_PRL_042_005_PaymentOption")
          .post(BaseURL + "/payments/service-request/#{paymentServiceRequestReferenceNumber}/card-payments")
          .disableFollowRedirect
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
          .body(ElFileBody("bodies/prl/PRLDataPrep/MakeThePayment.json"))
          .check(regex("""card.payments.service.gov.uk\/secure\/(.{8}-.{4}-.{4}-.{4}-.{12})"""").saveAs("address"))
          )
         // .check(substring("Enter card details")))

          .exec(getCookieValue(CookieKey("XSRF-TOKEN").withDomain(BaseURL.replace("https://", "")).saveAs("XSRFToken")))
      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
* Payment option redirect
======================================================================================*/

      .group("XUI_PRL_043_PaymentOption") {

        exec(http("XUI_PRL_043_005_PaymentOption")
          .get(payUrl + "/secure/#{address}")
       //   .disableFollowRedirect
          .headers(Headers.navigationHeader)
          .check(CsrfCheck2.save)
          .check(regex("""\/card_details\/(.{26})""").saveAs("paymentId"))
     /*     .check(
            headerRegex("location", """\/card_details\/(.{26})""")
              .ofType[(String)]
              .saveAs("paymentId")
          )

      */
        )

      }

      .pause(MinThinkTime, MaxThinkTime)





      /*======================================================================================
* Enter Card Details
======================================================================================*/

      .group("XUI_PRL_044_CardDetails") {

        exec(http("XUI_PRL_044_005_CardDetails")
          .post(payUrl + "/card_details/#{paymentId}")
          .headers(Headers.uploadHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .header("Content-Type", "application/x-www-form-urlencoded")
          .formParam("chargeId", "#{paymentId}")
          .formParam("csrfToken", "#{csrf}")
          .formParam("cardNo", "4444333322221111")
          .formParam("expiryMonth", "#{PRLAppDobMonth}")
          .formParam("expiryYear", "27")
          .formParam("cardholderName", "#{PRLRandomString} Card Holder")
          .formParam("cvc", "123")
          .formParam("addressCountry", "GB")
          .formParam("addressLine1", "#{PRLRandomString} Address Line 1")
          .formParam("addressLine2", "")
          .formParam("addressCity", "#{PRLRandomString} City")
          .formParam("addressPostcode", "TW3 2HH")
          .formParam("email", "#{PRLRandomString}@gmail.com")
          .check(CsrfCheck2.save)
          .check(substring("Confirm your payment")))
      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
* Confirm Payment
======================================================================================*/

      .group("XUI_PRL_045_ConfirmPayment") {

        exec(http("XUI_PRL_045_005_ConfirmPayment")
          .post(payUrl + "/card_details/#{paymentId}/confirm")
          .headers(Headers.uploadHeader)
          .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
          .header("Content-Type", "application/x-www-form-urlencoded")
          .formParam("csrfToken", "#{csrf}")
          .formParam("chargeId", "#{paymentId}")
          .check(substring("Payment successful")))


          .exec { session =>
            val fw = new BufferedWriter(new FileWriter("cases.csv", true))
            try {
              fw.write(session("caseId").as[String] + "\r\n")
            } finally fw.close()
            session
          }
      }


      .pause(MinThinkTime, MaxThinkTime)




}