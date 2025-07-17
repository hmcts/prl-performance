package scenarios

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._
import io.gatling.http.check.HttpCheck

object CCDAPI {

  val RpeAPIURL = Environment.rpeAPIURL
  val IdamAPIURL = Environment.idamAPIURL
  val CcdAPIURL = Environment.ccdAPIURL
  val CaseDocAPI = Environment.caseDocAPI

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val clientSecret = ConfigFactory.load.getString("auth.clientSecret")
          
  //userType must be "Caseworker", "Legal", "Citizen" or "Solicitor"
  def Auth(userType: String) =

    exec(session => userType match {
      case "Caseworker" => session.set("emailAddressCCD", "ccdloadtest-cw@gmail.com").set("passwordCCD", "Password12").set("microservice", "ccd_data")
      case "Legal" => session.set("emailAddressCCD", "ccdloadtest-la@gmail.com").set("passwordCCD", "Password12").set("microservice", "ccd_data")
      case "Solicitor" => session.set("emailAddressCCD", session("user").as[String]).set("passwordCCD", session("password").as[String]).set("microservice", "ccd_data")
      case "CourtAdmin" => session.set("emailAddressCCD", "prl_pt_ca_swansea@justice.gov.uk").set("passwordCCD", session("password").as[String]).set("microservice", "prl_cos_api")
      case "CourtManager" => session.set("emailAddressCCD", "prl_pt_am_test_hctl@justice.gov.uk").set("passwordCCD", session("password").as[String]).set("microservice", "prl_cos_api")
      case "CourtAdminDocUpload" => session.set("emailAddressCCD", session("user").as[String]).set("passwordCCD", session("password").as[String]).set("microservice", "xui_webapp")
    })


    .exec(http("XUI_000_Auth")
      .post(RpeAPIURL + "/testing-support/lease")
      .body(StringBody("""{"microservice":"#{microservice}"}""")).asJson
      .check(regex("(.+)").saveAs("authToken")))

    .pause(1)

    .exec(http("XUI_000_GetBearerToken")
      .post(IdamAPIURL + "/o/token")
      .formParam("grant_type", "password")
      .formParam("username", "#{emailAddressCCD}")
      .formParam("password", "#{passwordCCD}")
      .formParam("client_id", "ccd_gateway")
      .formParam("client_secret", clientSecret)
      .formParam("scope", "openid profile roles openid roles profile")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .check(jsonPath("$.access_token").saveAs("bearerToken")))

    .pause(1)

    .exec(http("XUI_000_GetIdamID")
      .get(IdamAPIURL + "/details")
      .header("Authorization", "Bearer #{bearerToken}")
      .check(jsonPath("$.id").saveAs("idamId")))

    .pause(1)

  val AssignCase =

    exec(Auth("Solicitor"))

    .exec(http("XUI_000_AssignCase")
      .post(CcdAPIURL + "/case-users")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody("bodies/nfd/AssignCase.json"))
      .check(jsonPath("$.status_message").is("Case-User-Role assignments created successfully")))

    .pause(1)

  // allows the event to be used where the userType = "Caseworker" or "Legal"
  def CreateEvent(userType: String, jurisdiction: String, caseType: String, eventName: String, payloadPath: String, checksEvent: Seq[HttpCheck] = Seq.empty, checksTrigger: Seq[HttpCheck] = Seq.empty) =

    exec(_.set("eventName", eventName)
          .set("jurisdiction", jurisdiction)
          .set("caseType", caseType))

    .exec { session =>
    println("****CASEID: " + session("caseId").asOption[String].getOrElse("NOT FOUND"))
    session
  }

    .exec(Auth(userType))

    .exec(http("XUI_000_GetCCDEventToken")
      .get(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/cases/#{caseId}/event-triggers/#{eventName}/token")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$.token").saveAs("event_token"))
      .check(checksTrigger: _*)) // Expand the checks if any

    .pause(1)

    .exec(http("XUI_000_CCDEvent-#{eventName}")
      .post(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/cases/#{caseId}/events")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody(payloadPath))
      .check(jsonPath("$.id"))
      .check(checksEvent: _*)) // Expand the checks if any

    .pause(1)


// allows the event to be used where the userType = "Caseworker" or "Legal"
  def CreateCaseFL401(userType: String, jurisdiction: String, caseType: String, eventName: String, payloadPath: String) =

    exec(_.set("eventName", eventName)
          .set("jurisdiction", jurisdiction)
          .set("caseType", caseType))

    .exec(Auth(userType))

    .exec(http("XUI_000_GetCCDEventToken")
      .get(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/event-triggers/#{eventName}/token")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$.token").saveAs("event_token")))

    .pause(1)

    .exec(http("XUI_000_CCDEvent-#{eventName}")
      .post(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/cases/")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody(payloadPath))
      .check(jsonPath("$.id").saveAs("caseId")))

    .pause(1)

  val GetCaseDetails =

    exec(Auth("CourtAdmin"))

    .exec(http("PRL_000_GetCaseDetails")
      .get(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/PRIVATELAW/case-types/PRLAPPS/cases/#{caseId}")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .check(jsonPath("$.case_data.daApplicant.firstName").saveAs("ApplicantFirstName"))
      .check(jsonPath("$.case_data.daApplicant.lastName").saveAs("ApplicantLastName"))
      .check(jsonPath("$.case_data.daRespondent.firstName").saveAs("RespondentFirstName"))
      .check(jsonPath("$.case_data.daRespondent.lastName").saveAs("RespondentLastName")))

    .pause(1)

  def UploadDocument(userType: String, jurisdiction: String, caseType: String, docName: String, checksUpload: Seq[HttpCheck] = Seq.empty) =

    exec(_.set("userType", userType)
      .set("jurisdiction", jurisdiction)
      .set("caseType", caseType)
      .set("docName", docName))

    .exec(Auth(userType))

    .exec(http("XUI_000_UploadDocument")
      .post(CaseDocAPI + "/cases/documents")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("accept", "application/json")
      .header("Content-Type", "multipart/form-data")
      .formParam("classification", "PUBLIC")
      .formParam("caseTypeId", "#{caseType}")
      .formParam("jurisdictionId", "#{jurisdiction}")
      .bodyPart(RawFileBodyPart("files", "#{docName}")
        .fileName("#{docName}")
        .transferEncoding("binary"))
      .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURL"))
      .check(jsonPath("$.documents[0].hashToken").saveAs("documentHash"))
      .check(jsonPath("$.documents[0].hashToken").saveAs("documentHashSoN"))
      .check(jsonPath("$.documents[0]._links.self.href").saveAs("DocumentURLSoN"))
      .check(checksUpload: _*)) // Expand the checks if any

    .pause(1)

  def EventAndUploadDocument(userType: String, jurisdiction: String, caseType: String, eventName: String, docName: String, payloadPath: String) =

    exec(_.set("userType", userType)
          .set("jurisdiction", jurisdiction)
          .set("caseType", caseType)
          .set("eventName", eventName)
          .set("docName", docName))

    .exec(Auth(userType))

    .exec(http("XUI_000_GetCCDEventToken")
        .get(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/cases/#{caseId}/event-triggers/#{eventName}/token")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("ServiceAuthorization", "#{authToken}")
        .header("Content-Type","application/json")
        .check(jsonPath("$.token").saveAs("event_token")))

     // .exec(session => {
     //   session.set("FileName1", "1MB.pdf")
     // })

//      .exec(Auth(userType))

      .exec(http("XUI_000_UploadDocument")
        .post(CaseDocAPI + "/cases/documents")
        .header("Authorization", "Bearer #{bearerToken}")
        .header("ServiceAuthorization", "#{authToken}")
        .header("accept", "application/json")
        .header("Content-Type", "multipart/form-data")
        .formParam("classification", "PUBLIC")
        .formParam("caseTypeId", "#{caseType}")
        .formParam("jurisdictionId", "#{jurisdiction}")
        .bodyPart(RawFileBodyPart("files", "#{docName}")
          .fileName("#{docName}")
          .transferEncoding("binary"))
        .check(regex("""documents/([0-9a-z-]+?)/binary""").saveAs("Document_ID"))
        .check(jsonPath("$.documents[0].hashToken").saveAs("hashToken")))

        
      .exec(http("XUI_000_CCDEvent-#{eventName}")
        .post(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/cases/#{caseId}/events")
        .header("ServiceAuthorization", "Bearer #{bearerToken}")
        .header("Authorization", "Bearer #{authToken}")
        .header("Content-Type","application/json")
        .body(ElFileBody(payloadPath))
        .check(jsonPath("$.id")))

      .pause(1)

  def ValidateAndExtract(jurisdiction: String, caseType: String, pageId: String, payloadPath: String) =

    exec(_.set("pageId", pageId)
      .set("jurisdiction", jurisdiction)
      .set("caseType", caseType))

    .exec(http("XUI_000_CCDValidate-#{pageId}")
      .post(CcdAPIURL + "/caseworkers/#{idamId}/jurisdictions/#{jurisdiction}/case-types/#{caseType}/cases/#{caseId}/events")
      .header("Authorization", "Bearer #{bearerToken}")
      .header("ServiceAuthorization", "#{authToken}")
      .header("Content-Type", "application/json")
      .body(ElFileBody(payloadPath))
      .check(jsonPath("$.data.previewOrderDoc.document_url").optional.saveAs("document_url"))
      .check(jsonPath("$.data.previewOrderDoc.document_filename").optional.saveAs("document_filename"))
      .check(jsonPath("$.data.previewOrderDoc.document_hash").optional.saveAs("document_hash"))
      .check(jsonPath("$.data.ordersHearingDetails[0].id").optional.saveAs("hearingId"))
      .check(jsonPath("$.data.serveOrderDynamicList.value[0].code").optional.saveAs("orderCode"))
      .check(jsonPath("$.data.serveOrderDynamicList.value[0].label").optional.saveAs("orderLabel"))
      .check(jsonPath("$.id")))
}