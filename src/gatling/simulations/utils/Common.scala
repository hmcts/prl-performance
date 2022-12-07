package utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.jsonpath.JsonPathCheckType
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object Common {

  /*======================================================================================
  * Common Utility Functions
  ======================================================================================*/

  val rnd = new Random()
  val now = LocalDate.now()
  val patternDay = DateTimeFormatter.ofPattern("dd")
  val patternMonth = DateTimeFormatter.ofPattern("MM")
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternReference = DateTimeFormatter.ofPattern("d MMM yyyy")

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomNumber(length: Int) = {
    rnd.alphanumeric.filter(_.isDigit).take(length).mkString
  }

  def getDay(): String = {
    (1 + rnd.nextInt(28)).toString.format(patternDay).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  def getMonth(): String = {
    (1 + rnd.nextInt(12)).toString.format(patternMonth).reverse.padTo(2, '0').reverse //pads single-digit dates with a leading zero
  }

  //Date of Marriage >= 30 years
  def getMarriageYear(): String = {
    now.minusYears(30 + rnd.nextInt(30)).format(patternYear)
  }
  //Date of Separation >= 6 years < 30 years
  def getSeparationYear(): String = {
    now.minusYears(6 + rnd.nextInt(23)).format(patternYear)
  }
  //Reference Date = 5 years and 6 months before the current date in the format 8 May 2016
  def getReferenceDate(): String = {
    now.minusYears(5).minusMonths(6).format(patternReference)
  }
  //Date of Birth >= 35 years
  def getDobYear(): String = {
    now.minusYears(35 + rnd.nextInt(70)).format(patternYear)
  }
  //Date of Birth <= 18 years
  def getDobYearChild(): String = {
    now.minusYears(2 + rnd.nextInt(15)).format(patternYear)
  }
  //Date of Death <= 21 years
  def getDodYear(): String = {
    now.minusYears(1 + rnd.nextInt(20)).format(patternYear)
  }
  //Saves partyId
  def savePartyId: CheckBuilder[JsonPathCheckType, JsonNode, String] = jsonPath("$.case_fields[*].value[*].value.party.partyId").saveAs("partyId")

  //Saves user ID
  def saveId: CheckBuilder[JsonPathCheckType, JsonNode, String] = jsonPath("$.case_fields[*].value[0].id").saveAs("id")

  /*======================================================================================
  * Common XUI Calls
  ======================================================================================*/

  val postcodeFeeder = csv("postcodes.csv").random

  val postcodeLookup =
    feed(postcodeFeeder)
      .exec(http("XUI_Common_000_PostcodeLookup")
        .get("/api/addresses?postcode=${postcode}")
        .headers(Headers.commonHeader)
        .header("accept", "application/json")
        .check(jsonPath("$.header.totalresults").ofType[Int].gt(0))
        .check(regex(""""(?:BUILDING|ORGANISATION)_.+" : "(.+?)",(?s).*?"(?:DEPENDENT_LOCALITY|THOROUGHFARE_NAME)" : "(.+?)",.*?"POST_TOWN" : "(.+?)",.*?"POSTCODE" : "(.+?)"""")
          .ofType[(String, String, String, String)].findRandom.saveAs("addressLines")))

  def healthcheck(path: String) =
    exec(http("XUI_Common_000_Healthcheck")
      .get(s"/api/healthCheck?path=${path}")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(substring("""{"healthState":true}""")))

  val activity =
    exec(http("XUI_Common_000_ActivityOptions")
      .options("/activity/cases/${caseId}/activity")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .check(status.in(200, 304, 403)))

  val caseActivityGet =
    exec(http("XUI_Common_000_ActivityOptions")
      .options("/activity/cases/${caseId}/activity")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .check(status.in(200, 304, 403)))

    .exec(http("XUI_Common_000_ActivityGet")
      .get("/activity/cases/${caseId}/activity")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .check(status.in(200, 304, 403)))

  val caseActivityPost =
    exec(http("XUI_Common_000_ActivityOptions")
      .options("/activity/cases/${caseId}/activity")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .check(status.in(200, 304, 403)))

    .exec(http("XUI_Common_000_ActivityPost")
      .post("/activity/cases/${caseId}/activity")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .header("sec-fetch-site", "same-site")
      .body(StringBody("{\n  \"activity\": \"view\"\n}"))
      .check(status.in(200, 201, 304, 403)))

  val configurationui =
    exec(http("XUI_Common_000_ConfigurationUI")
      .get("/external/configuration-ui/")
      .headers(Headers.commonHeader)
      .header("accept", "*/*")
      .check(substring("ccdGatewayUrl")))

  val configJson =
    exec(http("XUI_Common_000_ConfigJson")
      .get("/assets/config/config.json")
      .header("accept", "application/json, text/plain, */*")
      .check(substring("caseEditorConfig")))

  val TsAndCs =
    exec(http("XUI_Common_000_TsAndCs")
      .get("/api/configuration?configurationKey=termsAndConditionsEnabled")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(substring("false")))

  val userDetails =
    exec(http("XUI_Common_000_UserDetails")
      .get("/api/user/details")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*"))

  val configUI =
    exec(http("XUI_Common_000_ConfigUI")
      .get("/external/config/ui")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(substring("ccdGatewayUrl")))

  val isAuthenticated =
    exec(http("XUI_Common_000_IsAuthenticated")
      .get("/auth/isAuthenticated")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(regex("true|false")))

  val profile =
    exec(http("XUI_Common_000_Profile")
      .get("/data/internal/profile")
      .headers(Headers.commonHeader)
      .header("accept", "application/vnd.uk.gov.hmcts.ccd-data-store-api.ui-user-profile.v2+json;charset=UTF-8")
      .check(jsonPath("$.user.idam.id").notNull))

  val monitoringTools =
    exec(http("XUI_Common_000_MonitoringTools")
      .get("/api/monitoring-tools")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(jsonPath("$.key").notNull))

  val caseShareOrgs =
    exec(http("XUI_Common_000_CaseShareOrgs")
      .get("/api/caseshare/orgs")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(jsonPath("$.name").notNull))

  val orgDetails =
    exec(http("XUI_Common_000_OrgDetails")
      .get("/api/organisation")
      .headers(Headers.commonHeader)
      .header("accept", "application/json, text/plain, */*")
      .check(regex("name|Organisation route error"))
      .check(status.in(200, 304, 403)))

}