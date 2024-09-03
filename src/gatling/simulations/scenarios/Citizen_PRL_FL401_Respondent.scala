package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, CsrfCheck2, Environment, Headers}

import java.io.{BufferedWriter, FileWriter}

/*======================================================================================
* Respond to a FL401 case within PRL CUI
======================================================================================*/

object Citizen_PRL_FL401_Respondent {
  
  val PayURL = Environment.payURL
  val prlURL = Environment.prlURL
  val IdamUrl = Environment.idamURL
  val PRLCitizens = csv("UserDataPRLCitizen.csv").circular
  val postcodeFeeder = csv("postcodes.csv").circular

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime


  // Login 


  // Enter Case ID & Pin


  // Begin required steps

  

  
}