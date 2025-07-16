package scenarios

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import io.gatling.http.check.HttpCheck

object API_HMCHearings {

  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL
  val HmcAPIURL = Environment.hmcAPIURL
  val HmcHmiInboundURL = Environment.hmcHmiInboundURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")
  val clientSecretHMC = ConfigFactory.load.getString("auth.hmcHmiInboundAdapter.clientSecret")
          
  //userType must be "Caseworker", "Legal", "Citizen" or "Solicitor"
  def Auth(userType: String) =

//******* SET THESE PROPERLY *******  What is actually used/needed
    exec(session => userType match {
      case "ccdUser" => session.set("emailAddressCCD", "prl_pt_am_test_hctl@justice.gov.uk").set("passwordCCD", "Nagoya0102").set("microservice", "ccd_data").set("clientId", "ccd_gateway").set("clientSecret", clientSecret)
      case "hmcHearingRequest" => session.set("emailAddressCCD", "prl_pt_ca_swansea@justice.gov.uk").set("passwordCCD", "Nagoya0102").set("microservice", "hmc_hmi_inbound_adapter").set("clientId", "hmc_hmi_inbound_adapter").set("clientSecret", clientSecretHMC)
      case "hmcHearingList" => session.set("emailAddressCCD", "prl_pt_ca_swansea@justice.gov.uk").set("passwordCCD", "Nagoya0102").set("microservice", "api_gw").set("clientId", "hmc_hmi_inbound_adapter").set("clientSecret", clientSecretHMC)
    })

    .exec(http("XUI_000_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"#{microservice}"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(3)

    .exec(http("XUI_000_GetBearerToken")
      .post(IdamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "#{emailAddressCCD}")
      .formParam("password", "#{passwordCCD}")
      .formParam("client_id", "#{clientId}")
      .formParam("client_secret", "#{clientSecret}")
      .formParam("scope", "openid profile roles openid roles profile")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("bearerToken")))

    .pause(3)

    .exec(http("XUI_000_GetIdamID")
      .get(IdamAPIURL + "/details")
      .header("Authorization", "Bearer #{bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))

    .pause(1)

val GetCaseDetails =

    exec(http("PRL_000_GetCaseDetails")
      .get(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/PRIVATELAW/case-types/PRLAPPS/cases/#{caseId}") //#{caseId}
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      //Case
      //.check(jsonPath("$.case_data.applicantOrRespondentCaseName").saveAs("caseName"))
      .check(jsonPath("$.case_data.applicantCaseName").saveAs("caseName"))
      //Applicant
        .check(jsonPath("$.case_data.applicantTable[*].id").findAll.saveAs("applicantId"))
        .check(jsonPath("$.case_data.applicantTable[*].value.lastName").findAll.saveAs("applicantLastName"))
        .check(jsonPath("$.case_data.applicantTable[*].value.firstName").findAll.saveAs("applicantFirstName"))
        .check(jsonPath("$.case_data.applicantTable[*].value.email").findAll.saveAs("applicantEmail"))
        .check(jsonPath("$.case_data.applicants[*].value.phoneNumber").findAll.saveAs("applicantPhoneNo"))
      //App Solicitor
        .check(jsonPath("$.case_data.applicants[*].value.solicitorPartyId").findAll.saveAs("solicitorPartyId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationID").findAll.saveAs("solicitorOrgId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationName").findAll.saveAs("solicitorOrgName"))
        .check(jsonPath("$.case_data.applicants[*].value.solicitorOrgUuid").findAll.saveAs("solicitorOrgUuid"))
        .check(jsonPath("$.case_data.applicants[*].value.representativeLastName").findAll.saveAs("solicitorLastName"))
        .check(jsonPath("$.case_data.applicants[*].value.representativeFirstName").findAll.saveAs("solicitorFirstName"))
        .check(jsonPath("$.case_data.applicants[0].value.solicitorEmail").findAll.saveAs("solicitorEmail"))
      //Respondents
        .check(jsonPath("$.case_data.respondentTable[*].id").findAll.saveAs("respondentId"))
        .check(jsonPath("$.case_data.respondentTable[*].value.firstName").findAll.saveAs("respondentFirstName"))
        .check(jsonPath("$.case_data.respondentTable[*].value.lastName").findAll.saveAs("respondentLastName"))
        .check(jsonPath("$.case_data.respondentTable[*].value.email").findAll.saveAs("respondentEmail"))
        .check(jsonPath("$.case_data.respondentTable[*].value.phoneNumber").findAll.saveAs("respondentPhoneNo"))
        //Resp Solicitor
         .check(jsonPath("$.case_data.respondents[*].value.solicitorPartyId").findAll.saveAs("respSolicitorPartyId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationID").findAll.saveAs("solicitorOrgId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationName").findAll.saveAs("solicitorOrgName"))
        .check(jsonPath("$.case_data.respondents[*].value.solicitorOrgUuid").findAll.saveAs("respSolicitorOrgUuid"))
        .check(jsonPath("$.case_data.respondents[*].value.representativeLastName").findAll.saveAs("respSolicitorLastName"))
        .check(jsonPath("$.case_data.respondents[*].value.representativeFirstName").findAll.saveAs("respSolicitorFirstName"))
        .check(jsonPath("$.case_data.respondents[0].value.solicitorEmail").findAll.saveAs("respSolicitorEmail"))
        .check(status.is(200)))

    .pause(3)

val GetCaseDetailsFL401 =

    exec(http("PRL_000_GetCaseDetails")
      .get(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/PRIVATELAW/case-types/PRLAPPS/cases/#{caseId}") //#{caseId}
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      //Case
      //.check(jsonPath("$.case_data.applicantOrRespondentCaseName").saveAs("caseName"))
      .check(jsonPath("$.case_data.caseNameHmctsInternal").saveAs("caseName"))
      //Applicant
        .check(jsonPath("$.case_data.applicantsFL401.partyId").saveAs("applicantPartyId"))
        .check(jsonPath("$.case_data.applicantsFL401.lastName").saveAs("applicantLastName"))
        .check(jsonPath("$.case_data.applicantsFL401.firstName").saveAs("applicantFirstName"))
        .check(jsonPath("$.case_data.applicantsFL401.email").saveAs("applicantEmail"))
        .check(jsonPath("$.case_data.applicantsFL401.phoneNumber").saveAs("applicantPhoneNo"))
      //App Solicitor
        .check(jsonPath("$.case_data.applicantsFL401.solicitorPartyId").findAll.saveAs("solicitorPartyId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationID").findAll.saveAs("solicitorOrgId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationName").findAll.saveAs("solicitorOrgName"))
        .check(jsonPath("$.case_data.applicantsFL401.solicitorOrgUuid").findAll.saveAs("solicitorOrgUuid"))
        .check(jsonPath("$.case_data.applicantsFL401.representativeLastName").findAll.saveAs("solicitorLastName"))
        .check(jsonPath("$.case_data.applicantsFL401.representativeFirstName").findAll.saveAs("solicitorFirstName"))
        .check(jsonPath("$.case_data.applicantsFL401.solicitorEmail").findAll.saveAs("solicitorEmail"))
      //Respondents
        .check(jsonPath("$.case_data.respondentsFL401.partyId").saveAs("respondentPartyId"))
        .check(jsonPath("$.case_data.respondentsFL401.lastName").saveAs("respondentFirstName"))
        .check(jsonPath("$.case_data.respondentsFL401.firstName").saveAs("respondentLastName"))
        .check(jsonPath("$.case_data.respondentsFL401.email").saveAs("respondentEmail"))
        .check(jsonPath("$.case_data.respondentsFL401.phoneNumber").saveAs("respondentPhoneNo"))
        //Resp Solicitor
         .check(jsonPath("$.case_data.respondentsFL401.solicitorPartyId").findAll.saveAs("respSolicitorPartyId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationID").findAll.saveAs("solicitorOrgId"))
        //.check(jsonPath("$.case_data.applicants[0].value.solicitorOrg.OrganisationName").findAll.saveAs("solicitorOrgName"))
        .check(jsonPath("$.case_data.respondentsFL401.solicitorOrgUuid").findAll.saveAs("respSolicitorOrgUuid"))
        .check(jsonPath("$.case_data.respondentsFL401.representativeLastName").findAll.saveAs("respSolicitorLastName"))
        .check(jsonPath("$.case_data.respondentsFL401.representativeFirstName").findAll.saveAs("respSolicitorFirstName"))
        .check(jsonPath("$.case_data.respondentsFL401.solicitorEmail").findAll.saveAs("respSolicitorEmail"))
        .check(status.is(200)))

    .pause(3)

val RequestHearingC100 = 

    exec(_.setAll(
        "todayDay" -> Common.getDay(),
        "todayMonth" -> Common.getMonth(),
        "futureYear" -> (Common.getCurrentYear() + 1),
        "dateNow" -> Common.getDate()))

    .exec(http("RequestHearingC100")
      .post(HmcAPIURL + "/hearing/")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody("bodies/prl/hmc/RequestHearingC100.Json"))
      .check(jsonPath("$.hearingRequestID").saveAs("hearingRequestId"))
      .check(status.saveAs("statusvalue")))

    .pause(90)

// hearing request for case types "C100" or "FL401"
def RequestHearing(caseType: String) = 

    exec(session => caseType match {
      case "C100" => session.set("requestHearingBody", "bodies/prl/hmc/RequestHearingC100.Json")
      case "FL401" => session.set("requestHearingBody", "bodies/prl/hmc/RequestHearingFL401.Json")
    })

    .exec(_.setAll(
        "todayDay" -> Common.getDay(),
        "todayMonth" -> Common.getMonth(),
        "futureYear" -> (Common.getCurrentYear() + 1),
        "dateNow" -> Common.getDate()))

    .exec(http("RequestHearing#{caseType}")
      .post(HmcAPIURL + "/hearing/")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody("#{requestHearingBody}"))
      .check(jsonPath("$.hearingRequestID").saveAs("hearingRequestId"))
      .check(status.saveAs("statusvalue")))

    // needs to be a delay after hearing request before listing hearing
    .pause(90)

// List hearing for case types "C100" or "FL401"
def ListHearing(caseType: String) = 

    exec(session => caseType match {
        case "C100" => session.set("listHearingBody", "bodies/prl/hmc/ListHearingC100.json")
        case "FL401" => session.set("listHearingBody", "bodies/prl/hmc/ListHearingFL401.json")
      })

    .exec(http("ListHearing#{caseType}")
      .put(HmcHmiInboundURL + "/listings/#{hearingRequestId}")
      .header("Authorization", "Bearer #{bearerToken}") 
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .header("Accept","*/*")
      .header("Accept-Encoding","gzip, deflate, br")
      .body(ElFileBody("#{listHearingBody}"))
      .check(status.is(202)))

  .pause(3) 

val ListHearingC100 =

  exec(http("XUI_000_Auth")
    .post(RpeAPIURL + "/testing-support/lease")
    .body(StringBody("""{"microservice":"api_gw"}""")).asJson
    .check(regex("(.+)").saveAs("authTokenListHearing")))

  .exec(http("XUI_000_GetBearerToken")
    .post(IdamAPIURL + "/o/token")
    .formParam("grant_type", "password")
    .formParam("username", "prl_pt_ca_swansea@justice.gov.uk") 
    .formParam("password", "Nagoya0102")
    .formParam("client_id", "hmc_hmi_inbound_adapter")
    .formParam("client_secret", clientSecretHMC)
    .formParam("scope", "openid profile roles openid roles profile")
    .header("Content-Type", "application/x-www-form-urlencoded")
    .check(jsonPath("$.access_token").saveAs("bearerTokenListHearing")))

  .exec(http("ListHearingC100")
    .put(HmcHmiInboundURL + "/listings/#{hearingRequestId}")
    .header("Authorization", "Bearer #{bearerTokenListHearing}") 
    .header("ServiceAuthorization", "#{authTokenListHearing}")
    .header("Content-Type", "application/json")
    .header("Accept","*/*")
    .header("Accept-Encoding","gzip, deflate, br")
    .body(ElFileBody("bodies/prl/hmc/ListHearingC100.json"))
    .check(status.is(200)))

  .pause(3)

}