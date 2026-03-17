package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils._

/*======================================================================================
* Create a new Private Law application as a professional user (e.g. solicitor)
======================================================================================*/

object OS_API_UK {
  
  val BaseURL = Environment.baseURL
  val PayURL = Environment.payURL
  val prlURL = Environment.prlURL
  val pcqURL = Environment.pcqURL
  val IdamUrl = Environment.idamURL
  //val PRLCitizens = csv("UserDataPRLCitizen.csv").circular


  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime



    /*===============================================================================================
    * API.OS.UK
    ===============================================================================================*/
  val osAPIuk =

    group("API.OS.UK") {
      exec(http("API.OS.UK")
        .get("https://api.os.uk/search/places/v1/postcode?postcode=#{postcode}&key=GZhRzQDv5EQ9xQZeebHZcFJWyUMDs2To&maxresults=1")
        .header("content-type", "application/x-www-form-urlencoded")
        .check(substring("DPA"))
        .check(status.in(200)))
    }

    .pause(MinThinkTime, MaxThinkTime)

    }
