package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck2, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object Solicitor_PRL_CaseFlags {
  
  val BaseURL = Environment.baseURL
  val prlURL = "https://privatelaw.#{env}.platform.hmcts.net"
  val IdamUrl = Environment.idamURL
  val payUrl = "https://card.payments.service.gov.uk"



  val postcodeFeeder = csv("postcodes.csv").circular
  val casesFeeder = csv("caseFlagsCases.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime





  val CaseFlagsSol =


  /*======================================================================================
  * Create Case Start
  ======================================================================================*/

    group("XUI_PRL_070_CreateCaseStart") {

      exec(_.setAll(
        "PRLRandomString" -> (Common.randomString(7)),
        "currentDateTime" -> Common.getCurrentDateTime(),
     //   "caseId" -> ("1702986711455195"),
        "env" -> ("perftest"),
        "PRLAppDobDay" -> Common.getDay(),
        "PRLAppDobMonth" -> Common.getMonth(),
        "PRLAppDobYear" -> Common.getDobYear()))

/*
        .exec(http("XUI_PRL_030_005_CreateCaseStart")
          .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/testingSupportDummySolicitorCreate?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.event_token").saveAs("event_token"))
          .check(substring("TS-Solicitor application")))

 */

    }

      .pause(MinThinkTime, MaxThinkTime)

    /*======================================================================================
    * Select 'Request support'
    ======================================================================================*/

      .group("XUI_PRL_070_RequestSupport") {

          exec(http("XUI_PRL_070_005_RequestSupport")
            .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100RequestSupport/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
         //   .check(substring("C100"))
          )

        .exec(http("XUI_PRL_070_010_RequestSupport")
          .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100RequestSupport?ignore-warning=false")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
          .check(jsonPath("$.case_fields[1].formatted_value.partyName").saveAs("partyName"))
          .check(jsonPath("$.case_fields[2].formatted_value.partyName").saveAs("repPartyName"))
          .check(jsonPath("$.case_fields[11].formatted_value.partyName").saveAs("otherPartyName"))
          .check(jsonPath("$.event_token").optional.saveAs("event_token"))
             .check(substring("c100RequestSupport"))
        )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Where should this flag be added?
      ======================================================================================*/

      .group("XUI_PRL_080_WhereFlagAdded") {

        exec(http("XUI_PRL_080_005_WhereFlagAdded")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
             .check(substring("business_area"))
        )


        .exec(http("XUI_PRL_080_010_WhereFlagAdded")
          .get(BaseURL + "/refdata/commondata/caseflags/service-id=ABA5?flag-type=PARTY")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("FlagDetails"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Select flag type - RA
======================================================================================*/

   /*   .group("XUI_PRL_060_SelectFlagType") {

        exec(http("XUI_PRL_060_005_SelectFlagType")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("business_area"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)

    */


/*======================================================================================
* Select flag type - RA
======================================================================================*/

      /*======================================================================================
* Request Support Submit
======================================================================================*/

   .group("XUI_PRL_080_RequestSupportSubmit") {

     exec(http("XUI_PRL_080_005_RequestSupportSubmit")
       .post(BaseURL + "/data/cases/#{caseId}/events")
       .headers(Headers.commonHeader)
       .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
       .body(ElFileBody("bodies/prl/CaseFlags/RequestSupportSubmit.json"))
       .check(substring("caApplicant1ExternalFlags"))
     )

   }

   .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
       * Select 'Request support'
       ======================================================================================*/

      .group("XUI_PRL_090_RequestSupport2") {


        exec(_.setAll(
          "currentDateTime2" -> Common.getCurrentDateTime()))

        .exec(http("XUI_PRL_090_005_RequestSupport2")
          .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100RequestSupport/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          //   .check(substring("C100"))
        )

          .exec(http("XUI_PRL_090_010_RequestSupport2")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100RequestSupport?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .check(jsonPath("$.case_fields[1].formatted_value.details[0].id").saveAs("flagId"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[0].value.path[0].id").saveAs("partyId"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[0].value.path[1].id").saveAs("raId"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[0].value.path[2].id").saveAs("formatId"))
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
            .check(substring("c100RequestSupport"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Where should this flag be added?
      ======================================================================================*/

      .group("XUI_PRL_100_WhereFlagAdded2") {

        exec(http("XUI_PRL_100_005_WhereFlagAdded2")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("business_area"))
        )


          .exec(http("XUI_PRL_100_010_WhereFlagAdded2")
            .get(BaseURL + "/refdata/commondata/caseflags/service-id=ABA5?flag-type=PARTY")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            .check(substring("FlagDetails"))
          )

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Request Support Submit
======================================================================================*/

      .group("XUI_PRL_110_RequestSupportSubmit2") {

        exec(http("XUI_PRL_110_005_RequestSupportSubmit2")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/RequestSupportSubmit2.json"))
          .check(substring("caApplicant1ExternalFlags"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
         * Select 'Request support'
         ======================================================================================*/

      .group("XUI_PRL_120_RequestSupport3") {


        exec(_.setAll(
          "currentDateTime3" -> Common.getCurrentDateTime()))

          .exec(http("XUI_PRL_120_005_RequestSupport3")
            .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100RequestSupport/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            //   .check(substring("C100"))
          )

          .exec(http("XUI_PRL_120_010_RequestSupport3")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100RequestSupport?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .check(jsonPath("$.case_fields[1].formatted_value.details[1].id").saveAs("flagId2"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[1].value.path[0].id").saveAs("partyId2"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[1].value.path[1].id").saveAs("raId2"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[1].value.path[2].id").saveAs("formatId2"))
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
            .check(substring("c100RequestSupport"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Where should this flag be added?
      ======================================================================================*/

      .group("XUI_PRL_130_WhereFlagAdded3") {

        exec(http("XUI_PRL_130_005_WhereFlagAdded3")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("business_area"))
        )


          .exec(http("XUI_PRL_130_010_WhereFlagAdded3")
            .get(BaseURL + "/refdata/commondata/caseflags/service-id=ABA5?flag-type=PARTY")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            .check(substring("FlagDetails"))
          )

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Request Support Submit
======================================================================================*/

      .group("XUI_PRL_140_RequestSupportSubmit3") {

        exec(http("XUI_PRL_140_005_RequestSupportSubmit2")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/RequestSupportSubmit3.json"))
          .check(substring("caApplicant1ExternalFlags"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
         * Select 'Request support'
         ======================================================================================*/

      .group("XUI_PRL_150_RequestSupport4") {


        exec(_.setAll(
          "currentDateTime4" -> Common.getCurrentDateTime()))

          .exec(http("XUI_PRL_150_005_RequestSupport4")
            .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100RequestSupport/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            //   .check(substring("C100"))
          )

          .exec(http("XUI_PRL_150_010_RequestSupport4")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100RequestSupport?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .check(jsonPath("$.case_fields[1].formatted_value.details[2].id").saveAs("flagId3"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[2].value.path[0].id").saveAs("partyId3"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[2].value.path[1].id").saveAs("raId3"))
            .check(jsonPath("$.case_fields[1].formatted_value.details[2].value.path[2].id").saveAs("formatId3"))
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
            .check(substring("c100RequestSupport"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Where should this flag be added?
      ======================================================================================*/

      .group("XUI_PRL_160_WhereFlagAdded4") {

        exec(http("XUI_PRL_160_005_WhereFlagAdded4")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("business_area"))
        )


          .exec(http("XUI_PRL_160_010_WhereFlagAdded4")
            .get(BaseURL + "/refdata/commondata/caseflags/service-id=ABA5?flag-type=PARTY")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            .check(substring("FlagDetails"))
          )

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Request Support Submit
======================================================================================*/

      .group("XUI_PRL_170_RequestSupportSubmit4") {

        exec(http("XUI_PRL_170_005_RequestSupportSubmit4")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/RequestSupportSubmit4.json"))
          .check(substring("caApplicant1ExternalFlags"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
       * Select 'Request support'
       ======================================================================================*/

      .group("XUI_PRL_180_RequestSupport5") {


        exec(_.setAll(
          "currentDateTime5" -> Common.getCurrentDateTime()))

          .exec(http("XUI_PRL_180_005_RequestSupport5")
            .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100RequestSupport/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            //   .check(substring("C100"))
          )

          .exec(http("XUI_PRL_180_010_RequestSupport5")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100RequestSupport?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .check(jsonPath("$.case_fields[2].formatted_value.details[0].id").saveAs("flagId4"))
            .check(jsonPath("$.case_fields[2].formatted_value.details[0].value.path[0].id").saveAs("partyId4"))
            .check(jsonPath("$.case_fields[2].formatted_value.details[0].value.path[1].id").saveAs("raId4"))
            .check(jsonPath("$.case_fields[2].formatted_value.details[0].value.path[2].id").saveAs("formatId4"))
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
            .check(substring("c100RequestSupport"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
      * Where should this flag be added?
      ======================================================================================*/

      .group("XUI_PRL_190_WhereFlagAdded5") {

        exec(http("XUI_PRL_190_005_WhereFlagAdded5")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("business_area"))
        )


          .exec(http("XUI_PRL_190_010_WhereFlagAdded5")
            .get(BaseURL + "/refdata/commondata/caseflags/service-id=ABA5?flag-type=PARTY")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            .check(substring("FlagDetails"))
          )

      }

      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Request Support Submit
======================================================================================*/

      .group("XUI_PRL_200_RequestSupportSubmit5") {

        exec(http("XUI_PRL_200_005_RequestSupportSubmit4")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/RequestSupportSubmit5.json"))
          .check(substring("caApplicant1ExternalFlags"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)


  val NoticeOfChangeSol =


  /*======================================================================================
  * Create Case Start
  ======================================================================================*/

    group("XUI_PRL_030_SearchCase") {

    //  exec(_.setAll(
    //    "caseId" -> "1702894475790759"))
      feed(casesFeeder)


              .exec(http("XUI_PRL_030_005_SearchCase")
                .get(BaseURL + "/data/internal/cases/#{caseId}")
                .headers(Headers.commonHeader)
                .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-case-view.v2+json")
                .check(jsonPath("$.tabs[4].fields[3].formatted_value[0].value.firstName").saveAs("firstName"))
                .check(jsonPath("$.tabs[4].fields[3].formatted_value[0].value.lastName").saveAs("lastName"))
              )



    }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Select 'Notice of Change'
      ======================================================================================*/

  /*    .group("XUI_PRL_040_RequestSupport") {

//isaunteic

      }
      .pause(MinThinkTime, MaxThinkTime)

   */


      /*======================================================================================
    * Enter caseId
    ======================================================================================*/

      .group("XUI_PRL_040_NoticeOfChangeCase") {

          exec(http("XUI_PRL_040_005_NoticeOfChangeCase")
            .get(BaseURL + "/api/noc/nocQuestions?caseId=#{caseId}")
            .headers(Headers.commonHeader)
            .header("accept", "application/json, text/plain, */*")
            .check(substring("questions"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
  * Select 'Enter details'
  ======================================================================================*/

      .group("XUI_PRL_050_EnterDetails") {

        exec(http("XUI_PRL_050_005_EnterDetails")
          .post(BaseURL + "/api/noc/validateNoCQuestions")
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
          .body(ElFileBody("bodies/prl/CaseFlags/EnterDetails.json"))
          .check(substring("Notice of Change answers verified successfully"))
        )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
* Select 'Check and submit'
======================================================================================*/

      .group("XUI_PRL_060_CheckAndSubmitNoC") {

        exec(http("XUI_PRL_060_005_CheckAndSubmitNoC")
          .post(BaseURL + "/api/noc/submitNoCEvents")
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
          .body(ElFileBody("bodies/prl/CaseFlags/CheckAndSubmitNoC.json"))
          .check(substring("The Notice of Change request has been successfully submitted."))
        )

      }
      .pause(MinThinkTime, MaxThinkTime)


  val ManageSupport =


  /*======================================================================================
  * Select Case
  ======================================================================================*/

    group("XUI_PRL_030_CreateCaseStart") {

      exec(_.setAll(
        "currentDateTimeModified" -> Common.getCurrentDateTime()))

      /*
              .exec(http("XUI_PRL_030_005_CreateCaseStart")
                .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/testingSupportDummySolicitorCreate?ignore-warning=false")
                .headers(Headers.commonHeader)
                .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
                .check(jsonPath("$.event_token").saveAs("event_token"))
                .check(substring("TS-Solicitor application")))

       */

    }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Select 'Manage support'
      ======================================================================================*/

      .group("XUI_PRL_210_ManageSupport") {

        exec(http("XUI_PRL_210_005_RequestSupport")
          .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100ManageSupport/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          //   .check(substring("C100"))
        )

          .exec(http("XUI_PRL_210_010_RequestSupport")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100ManageSupport?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .check(jsonPath("$.case_fields[11].formatted_value.details[0].id").saveAs("flagId5"))
            .check(jsonPath("$.case_fields[11].formatted_value.details[0].value.path[0].id").saveAs("partyId5"))
            .check(jsonPath("$.case_fields[11].formatted_value.details[0].value.path[1].id").saveAs("raId5"))
            .check(jsonPath("$.case_fields[11].formatted_value.details[0].value.path[2].id").saveAs("formatId5"))
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
    * Select Case Flag to manage
    ======================================================================================*/

   /*   .group("XUI_PRL_050_SelectFlagSupport") {

//role details

      }
      .pause(MinThinkTime, MaxThinkTime)

    */

      /*======================================================================================
    * Tell us why the support is no longer needed
    ======================================================================================*/

  /*    .group("XUI_PRL_060_SupportNotNeeded") {

//details


      }
      .pause(MinThinkTime, MaxThinkTime)

   */



      /*======================================================================================
* Manage Support submit
======================================================================================*/

      .group("XUI_PRL_220_ManageSupportSubmitSol") {

        exec(http("XUI_PRL_220_005_ManageSupportSubmitSol")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/ManageSupportSubmitSol.json"))
          .check(substring("Inactive"))
        )

      }
      .pause(MinThinkTime, MaxThinkTime)


  val ManageSupportFlag =


  /*======================================================================================
  * Select Case
  ======================================================================================*/

    group("XUI_PRL_030_CreateCaseStart") {

      exec(_.setAll(
        "PRLRandomStringCa" -> (Common.randomString(7)),
     //   "caseId" -> ("1702894819447785"),
        "currentDateTimeCa" -> Common.getCurrentDateTime()))

      /*
              .exec(http("XUI_PRL_030_005_CreateCaseStart")
                .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/testingSupportDummySolicitorCreate?ignore-warning=false")
                .headers(Headers.commonHeader)
                .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
                .check(jsonPath("$.event_token").saveAs("event_token"))
                .check(substring("TS-Solicitor application")))

       */

    }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Select 'Manage Flags'
      ======================================================================================*/

      .group("XUI_PRL_230_ManageSupportFlags") {

        exec(http("XUI_PRL_230_005_ManageSupportFlags")
          .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100ManageFlags/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          //   .check(substring("C100"))
        )

          .exec(http("XUI_PRL_230_010_RequestSupport")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100ManageFlags?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
            .check(jsonPath("$.case_fields[37].formatted_value.partyName").saveAs("res1PartyName"))
            .check(jsonPath("$.case_fields[39].formatted_value.partyName").saveAs("res2PartyName"))
            .check(jsonPath("$.case_fields[41].formatted_value.partyName").saveAs("res3PartyName"))
            .check(jsonPath("$.case_fields[27].formatted_value.details[0].id").saveAs("flagId"))
            .check(jsonPath("$.case_fields[27].formatted_value.details[0].value.flagComment").saveAs("PRLRandomString"))
            .check(jsonPath("$.case_fields[27].formatted_value.details[0].value.path[0].id").saveAs("partyId"))
            .check(jsonPath("$.case_fields[27].formatted_value.details[0].value.path[1].id").saveAs("raId"))
            .check(jsonPath("$.case_fields[27].formatted_value.details[0].value.path[2].id").saveAs("formatId"))
            .check(jsonPath("$.case_fields[27].formatted_value.partyName").saveAs("partyName"))
            .check(jsonPath("$.case_fields[2].formatted_value.partyName").saveAs("repPartyName"))
            .check(jsonPath("$.case_fields[22].formatted_value.partyName").saveAs("otherPartyName"))
            .check(jsonPath("$.case_fields[27].formatted_value.details[0].value.dateTimeCreated").saveAs("currentDateTime")) //remove once full flow works
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
            .check(substring("c100ManageFlags"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
    * Select Flag
    ======================================================================================*/

      /*======================================================================================
* Change details
======================================================================================*/

      //details

      /*======================================================================================
* Submit
======================================================================================*/

      .group("XUI_PRL_240_SubmitSupportChange") {

        exec(http("XUI_PRL_240_005_SubmitSupportChange")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/SubmitSupportChange.json"))
        //  .check(substring("""\"status\": \"Active\"""))
        )

      }
      .pause(MinThinkTime, MaxThinkTime)



  val AssignApplication =


  /*======================================================================================
  * Select Case
  ======================================================================================*/

    group("XUI_PRL_030_CreateCaseStart") {

      exec(_.setAll(
        "PRLRandomString" -> (Common.randomString(7)),
        "currentDateTime" -> Common.getCurrentDateTime(),
        "caseId" -> ("1702894475790759"),
        "env" -> ("perftest"),
        "PRLAppDobDay" -> Common.getDay(),
        "PRLAppDobMonth" -> Common.getMonth(),
        "PRLAppDobYear" -> Common.getDobYear()))

      /*
              .exec(http("XUI_PRL_030_005_CreateCaseStart")
                .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/testingSupportDummySolicitorCreate?ignore-warning=false")
                .headers(Headers.commonHeader)
                .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
                .check(jsonPath("$.event_token").saveAs("event_token"))
                .check(substring("TS-Solicitor application")))

       */

    }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Select 'Tasks' tab
      ======================================================================================*/

      .group("XUI_PRL_040_TasksTab") {

        exec(http("XUI_PRL_040_005_TasksTab")
          .post(BaseURL + "/workallocation/case/task/#{caseId}")
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
          .body(ElFileBody("bodies/prl/CaseFlags/TasksTab.json"))
          .check(jsonPath("$[0].id").saveAs("taskId"))
        )


      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
    * Select 'Assign to me'
    ======================================================================================*/

      .group("XUI_PRL_050_AssignToMe") {

        exec(http("XUI_PRL_050_005_AssignToMe")
          .post(BaseURL + "/workallocation/task/#{taskId}/claim")
          .headers(Headers.commonHeader)
          .header("accept", "application/json, text/plain, */*")
          .body(ElFileBody("bodies/prl/CaseFlags/TasksTab.json"))
        //  .check(jsonPath("$.[0].id").saveAs("taskId"))
        )


      }
      .pause(MinThinkTime, MaxThinkTime)


  val CaseFlagsCa =


  /*======================================================================================
  * Create Case Start
  ======================================================================================*/

    group("XUI_PRL_030_CreateCaseStart") {

      exec(_.setAll(
        "currentDateTimeFlag" -> Common.getCurrentDateTime()))

      /*
              .exec(http("XUI_PRL_030_005_CreateCaseStart")
                .get(BaseURL + "/data/internal/case-types/PRLAPPS/event-triggers/testingSupportDummySolicitorCreate?ignore-warning=false")
                .headers(Headers.commonHeader)
                .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-case-trigger.v2+json;charset=UTF-8")
                .check(jsonPath("$.event_token").saveAs("event_token"))
                .check(substring("TS-Solicitor application")))

       */

    }

      .pause(MinThinkTime, MaxThinkTime)

      /*======================================================================================
      * Select 'Create Flag'
      ======================================================================================*/

      .group("XUI_PRL_240_CreateFlag") {

        exec(http("XUI_PRL_240_005_CreateFlag")
          .get(BaseURL + "/workallocation/case/tasks/#{caseId}/event/c100CreateFlags/caseType/PRLAPPS/jurisdiction/PRIVATELAW")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          //   .check(substring("C100"))
        )

          .exec(http("XUI_PRL_240_010_CreateFlag")
            .get(BaseURL + "/data/internal/cases/#{caseId}/event-triggers/c100CreateFlags?ignore-warning=false")
            .headers(Headers.commonHeader)
            .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-start-event-trigger.v2+json;charset=UTF-8")
           // .check(jsonPath("$.case_fields[1].formatted_value.partyName").saveAs("partyName"))
            .check(jsonPath("$.case_fields[2].formatted_value.partyName").saveAs("repPartyName"))
            //.check(jsonPath("$.case_fields[11].formatted_value.partyName").saveAs("otherPartyName"))
            .check(jsonPath("$.event_token").optional.saveAs("event_token"))
            .check(substring("Create Flag"))
          )

      }
      .pause(MinThinkTime, MaxThinkTime)


      /*======================================================================================
    * Where should this flag be added?
    ======================================================================================*/

      .group("XUI_PRL_250_CreateFlag") {

        exec(http("XUI_PRL_250_005_CreateFlag")
          .get(BaseURL + "/refdata/location/orgServices?ccdCaseType=PRLAPPS")
          .headers(Headers.commonHeader)
          .header("accept", "application/json")
          .check(substring("business_area"))
        )


          .exec(http("XUI_PRL_250_010_CreateFlag")
            .get(BaseURL + "/refdata/commondata/caseflags/service-id=ABA5?flag-type=PARTY&welsh-required=Y")
            .headers(Headers.commonHeader)
            .header("accept", "application/json")
            .check(substring("FlagDetails"))
          )

      }

      .pause(MinThinkTime, MaxThinkTime)



      /*======================================================================================
* Request Support Submit
======================================================================================*/

      .group("XUI_PRL_260_CreateFlag") {

        exec(http("XUI_PRL_260_005_CreateFlag")
          .post(BaseURL + "/data/cases/#{caseId}/events")
          .headers(Headers.commonHeader)
          .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.create-event.v2+json;charset=UTF-8")
          .body(ElFileBody("bodies/prl/CaseFlags/SubmitFlag.json"))
          .check(substring("caApplicant1ExternalFlags"))
        )

      }

      .pause(MinThinkTime, MaxThinkTime)



}