package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.io.{BufferedWriter, FileWriter}
import utils._
import scala.concurrent.duration._

object API_IDAM {

  val userFeeder = csv("UserFeeder.csv").shuffle

  val headers_1 = Map( //ServiceAuthorization token can be called from http://rpe-service-auth-provider-perftest.service.core-compute-perftest.internal/testing-support/lease
    "Content-Type" -> "application/json",
    "Accept" -> "application/json")
 
  def CreateUserInIdam(userType: String) =
    exec { session =>
    session.set("userType", userType)
  }
    .exec(_.setAll(
    "PRLRandomString" -> (Common.randomString(7)),
    "PRLRandomStringLast" -> (Common.randomString(5))))

    .feed(userFeeder)

    .exec(http("CreateUser:#{userType}_#{email}")
      .post(Environment.idamAPIURL + "/testing-support/accounts")
      .header("Content-Type", "application/json")
      .body(ElFileBody("bodies/prl/idam/Idam_CreateUserBody.json"))
      .check(jsonPath("$.id").saveAs("idamNewId"))
      .check(jsonPath("$.email").saveAs("email"))
      .check(status.saveAs("statusvalue")))

    .exec { session =>
     println(s"Saved status value: ${session("statusvalue").as[Int]}")
     session
    }

    // Save user and pass to session for use later
    .doIf(session => session.contains("statusvalue") && session("statusvalue").as[Int]== 201) {
     exec { session =>
      val email = session("email").as[String]
      val password = "CitizenPassword1"
      session
        .set("user", email)
        .set("password", password)
      }
     }


    // //Outputs the user email and idam id to a CSV, can be commented out if not needed  
    // .doIf(session=>session("statusvalue").as[String].contains("201")) {
    //   exec {
        // session =>
        //   val fw = new BufferedWriter(new FileWriter("CreatedIdamUsers.csv", true))
        //   try {
            // fw.write(session("email").as[String] + "," + "Password12,IA,Asylum," + session("idamNewId").as[String] + "\r\n")
        //   }
        //   finally fw.close()
        //   session
    //   }
    // }

    .pause(1.seconds)

  val DeleteUserInIdam =

    exec(http("DeleteUser:#{email}")
      .delete(Environment.idamAPIURL + "/testing-support/accounts/#{email}")
      .header("Content-Type", "application/json")
      .header("Accept", "application/json"))
      
    .pause(1.seconds)

}