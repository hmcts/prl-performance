package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, Environment, Headers}

object GetDocument  {
  
  /*====================================================================================
  *API CALL FOR GET DOCUMENT
  *=====================================================================================*/
  
  val UploadDocumentScenario =

    exec(http("Upload Document Request")
      .post("https://cft-api-mgmt.perftest.platform.hmcts.net/prl-document-api/#{caseId}/document") 
      .headers(Headers.uploadHeader)
      .header("Ocp-Apim-Subscription-Key", "dfe42a77a68d4c86a207ad4b202d6f5a")
      .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiI4cDJpajg2S0pTeENKeGcveUovV2w3TjcxMXM9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjYWZjYXNzQGhtY3RzLm5ldCIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJiODc1MjdiNy0wYTM0LTQ0MjQtYjlkYy05MzNjYjVmMzFkNDYtNDY0MTE0OTIiLCJzdWJuYW1lIjoiY2FmY2Fzc0BobWN0cy5uZXQiLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLXBlcmZ0ZXN0LmludGVybmFsOjg0NDMvb3BlbmFtL29hdXRoMi9yZWFsbXMvcm9vdC9yZWFsbXMvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJlY080Rko3V2ZONmZHVjdfR1J6QWg1ZTFKQzAiLCJhdWQiOiJjYWZjYWFzLWlkYW0taWQiLCJuYmYiOjE3MzM5OTkxMTUsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE3MzM5OTkxMTUsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNzM0MDI3OTE1LCJpYXQiOjE3MzM5OTkxMTUsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJ0d19RTWlSaXR1Q21sTFJHR1FKS0pDTXlWZjAifQ.fh9qmig2ZdBZ_igOa3T_w769Vtt59gNmLIdGumw1ALV68TJY3K-WHmkOweb1NWqmF2EQczhBa8zAKJWB85VNaNl-QqKLqv8rLDS-IvfQyLWSEBNEcDVNB5BDYQ1b89FQQ_7LNSczp6gfbn2_ocId7pG1tGQmurUWXQ_2vNokGQdbD1HyfbW4d9uRbLmHuQPlO6SCrtlo2Jbx4Nu2ETGc4bUpnE2LJijt5m4x4Nrr9slvityVBaM8gRmYoYNTL0l0ZTbmYHioNpRIxZ_L-MAut3XJbOLbwnrQKGp1Gv6kkysezRVWzdDExK6_LV4SItqqVs5mi1M_6C8k91e19rQZ_Q")
      .bodyPart(StringBodyPart("typeOfDocument", "Safeguarding_Letter")) 
      .bodyPart(RawFileBodyPart("file", "Yellowstone 3738 Safeguarding Letter 22112024170007.pdf")) //src/gatling/resources/
      .asMultipartForm
      .check(status.is(200), // Validate response status
      jsonPath("$.success").is("true")))
      

}