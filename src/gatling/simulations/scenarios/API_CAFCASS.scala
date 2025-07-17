package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{Common, CsrfCheck, Environment, Headers}

object API_CAFCASS  {

  val apiMgmtURL = "https://cft-api-mgmt.#{env}.platform.hmcts.net/"
  
  /*====================================================================================
  *API CALL FOR GET DOCUMENT
  *=====================================================================================*/
  
  val UploadDocument =

    exec(http("Upload Document Request")
      .post(apiMgmtURL + "prl-document-api/#{ccdCaseNumber}/document") 
      .headers(Headers.uploadHeader)
      .header("Ocp-Apim-Subscription-Key", "dfe42a77a68d4c86a207ad4b202d6f5a")
      .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiI4cDJpajg2S0pTeENKeGcveUovV2w3TjcxMXM9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjYWZjYXNzQGhtY3RzLm5ldCIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJiODc1MjdiNy0wYTM0LTQ0MjQtYjlkYy05MzNjYjVmMzFkNDYtNDY1OTQ4NTYiLCJzdWJuYW1lIjoiY2FmY2Fzc0BobWN0cy5uZXQiLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLXBlcmZ0ZXN0LmludGVybmFsOjg0NDMvb3BlbmFtL29hdXRoMi9yZWFsbXMvcm9vdC9yZWFsbXMvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJRXzdCR2ZNUnVwX3VudXFhbVQwekZSMzREWkUiLCJhdWQiOiJjYWZjYWFzLWlkYW0taWQiLCJuYmYiOjE3MzQwNzY0MjksImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE3MzQwNzY0MjksInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNzM0MTA1MjI5LCJpYXQiOjE3MzQwNzY0MjksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJ0MzVxWWpieTI3SHkzb013a3pkSUxvRXVDa0UifQ.keVNgGVWsurIq1FSBBCK4KazquKCza4RhkZ3dcMhWZJMzf6yROCOXiDr9pyh17RPy54JkSx_8cfA1_S5BFm3Z3e8Of7SON6KhGpH9N4eXZA6JTojuuz5LGl9jlrW9UJ4Pc5SzR5m9DnC-TzMXxAh2nR-F_D1UzkeA0SKe5xS0NsLgLm5us1wpidOq51zOYlvmO8zuZ6PuzVE7d1g2B-xSsFosHNBjO0_qXlAJHynbdBhN3daXmkA1k8-TBVa3FolEW0vwL1eseqOaTtGLA9EKah3dUd8ehzGpYcf5doDsYMInCD-eUBgC6XQD3lnzebuFr3oz1F_b5EbrOOxISq7gg")
      .bodyPart(StringBodyPart("typeOfDocument", "Safeguarding_Letter")) 
      .bodyPart(RawFileBodyPart("file", "Yellowstone 3738 Safeguarding Letter 22112024170007.pdf")) //src/gatling/resources/
      .asMultipartForm
      .check(status.is(200)))
      

}