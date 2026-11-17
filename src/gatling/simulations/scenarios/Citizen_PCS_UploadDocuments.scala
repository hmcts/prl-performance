package scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.{CsrfCheck, Environment, Headers}

import io.gatling.commons.validation._

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/*======================================================================================
* PCS TEMP - TEMPORARY FILE: delete this whole file once PCS testing is complete
* (see the "PCS TEMP" removal checklist at the top of PRL_Simulation.scala)
*======================================================================================*/

object Citizen_PCS_UploadDocuments {
  
  val PcsURL = "https://pcs.#{env}.platform.hmcts.net"
  val IdamUrl = Environment.idamURL
  
  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime
  
  // Default set of files uploaded per iteration - override by passing your own Seq
  val defaultFiles = Seq("7PageDoc.pdf", "120KB.pdf", "3MB.pdf")
  
  /*====================================================================================
  * Test files - real files only, from src/gatling/resources/pcs/ (checked first) or src/gatling/resources/
  *=====================================================================================*/
  
  /*
  *    1MB.pdf, 1MB_2.pdf, 1MB_3.pdf, 1MB_4.pdf, 2MB.pdf, 10MB.pdf, 10MB_2.pdf
  * The bytes always come from the original file.
  */
  private val ListEntry = """(.+?\.[A-Za-z0-9]+)\s*(?::\s*(\d+))?""".r
  private val uploadAliases = new ConcurrentHashMap[String, String]()   // upload name -> source file
  
  def filesFromList(list: String): Seq[String] = {
    val entries = list.split(",").map(_.trim).filter(_.nonEmpty).toSeq.map {
      case ListEntry(name, count) => (name.trim, Option(count).map(_.toInt).getOrElse(1))
      case other => throw new IllegalArgumentException(s"Invalid PCS file entry '$other' - expected e.g. 1MB.pdf or 1MB.pdf:4")
    }
    val used = scala.collection.mutable.Set[String]()
    entries.flatMap { case (source, count) =>
      fileBytes(source) // fail fast at start-up if the file is missing
      val dot = source.lastIndexOf('.')
      val (base, ext) = (source.substring(0, dot), source.substring(dot))
      (1 to count).map { n =>
        var uploadName = if (n == 1) source else s"${base}_$n$ext"
        var extra = n
        while (used.contains(uploadName)) { extra += 1; uploadName = s"${base}_$extra$ext" }
        used += uploadName
        if (uploadName != source) uploadAliases.put(uploadName, source)
        uploadName
      }
    }
  }
  
  private val resourceCache = new ConcurrentHashMap[String, Array[Byte]]()
  
  // Bytes for an upload name (repeated files map back to their original), loaded once and cached
  def fileBytes(fileName: String): Array[Byte] = {
    val source = Option(uploadAliases.get(fileName)).getOrElse(fileName)
    resourceCache.computeIfAbsent(source, (name: String) => loadResource(name))
  }
  
  // Looks in resources/pcs/ first, then resources/ (classpath, then the source folder as a fallback)
  private def loadResource(name: String): Array[Byte] = {
    val candidates = Seq(s"pcs/$name", name)
    val fromClasspath = candidates.iterator
      .flatMap(c => Option(getClass.getClassLoader.getResourceAsStream(c)))
      .map(in => try in.readAllBytes() finally in.close())
    if (fromClasspath.hasNext) fromClasspath.next()
    else candidates.map(c => Paths.get("src/gatling/resources", c)).find(p => Files.exists(p)) match {
      case Some(path) => Files.readAllBytes(path)
      case None => throw new IllegalArgumentException(s"PCS upload file not found in src/gatling/resources/pcs or src/gatling/resources: $name")
    }
  }
  
  // Same encoding as the front end's J() function: base64url, no padding
  private def encodeDocument(index: Int, id: String, fileName: String, size: Long, contentType: String): String = {
    val json = s"""{"index":$index,"id":"$id","document_filename":"$fileName","sizeInBytes":$size,"content_type":"$contentType"}"""
    Base64.getUrlEncoder.withoutPadding.encodeToString(json.getBytes(StandardCharsets.UTF_8))
  }
  
  // Size bucket used in the upload request name, so the report shows response times per file size
  // e.g. 967,038 bytes -> "1MB", 10,762,150 bytes -> "10MB", 120 KB -> "120KB"
  def sizeLabel(bytes: Int): String =
    if (bytes >= 512 * 1024) s"${math.max(1L, math.round(bytes / (1024.0 * 1024.0)))}MB"
    else s"${math.max(1L, math.round(bytes / 1024.0))}KB"
  
  private def contentTypeFor(fileName: String): String = fileName.toLowerCase match {
    case f if f.endsWith(".pdf")                         => "application/pdf"
    case f if f.endsWith(".jpg") || f.endsWith(".jpeg")  => "image/jpeg"
    case f if f.endsWith(".png")                         => "image/png"
    case f if f.endsWith(".docx")                        => "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    case f if f.endsWith(".doc")                         => "application/msword"
    case _                                               => "application/octet-stream"
  }
  
  /*====================================================================================
  * Login (new session) - /claims -> IDAM login page -> sign in -> back to PCS
  *=====================================================================================*/
  
  val PCSLogin =
    
    exec(flushHttpCache)
      .exec(flushSessionCookies)
      .exec(flushCookieJar)
      
      .group("PCS_UploadDocs_010_Homepage") {
        exec(http("PCS_UploadDocs_010_005_Homepage")
          .get(PcsURL + "/claims")                      // 302 -> /login -> IDAM /o/authorize -> IDAM /login
          .headers(Headers.navigationHeader)
          .check(CsrfCheck.save)
          .check(css("form[name='loginForm']", "action").transform(_.replace("&amp;", "&")).saveAs("pcsLoginAction"))
          .check(substring("Sign in or create an account")))
      }
      
      .pause(MinThinkTime, MaxThinkTime)
      
      .group("PCS_UploadDocs_020_Login") {
        exec(http("PCS_UploadDocs_020_005_Login")
          .post(IdamUrl + "#{pcsLoginAction}")           // carries client_id, nonce, code_challenge from the page
          .headers(Headers.navigationHeader)
          .headers(Headers.postHeader)
          .formParam("username", "#{user}")
          .formParam("password", "#{password}")
          .formParam("selfRegistrationEnabled", "true")
          .formParam("_csrf", "#{csrf}")
          .check(substring("Possession Claims")))      // follows /oauth2/callback back into PCS
      }
      
      .pause(MinThinkTime, MaxThinkTime)
  
  /*====================================================================================
  * Case dashboard
  *=====================================================================================*/
  
  val PCSDashboard =
    
    group("PCS_UploadDocs_030_Dashboard") {
      exec(http("PCS_UploadDocs_030_005_Dashboard")
        .get(PcsURL + "/case/#{caseId}/dashboard")
        .headers(Headers.navigationHeader)
        .check(substring("Possession Claims Dashboard"))
        .check(substring("upload-additional-documents/start-evidence-upload")))
    }
      
      .pause(MinThinkTime, MaxThinkTime)
  
  /*====================================================================================
  * Upload additional documents - multiple files
  *=====================================================================================*/
  def UploadDocuments(files: Seq[String] = defaultFiles, relatedTo: String = "MAIN_CLAIM") =
    
    exec(_.set("pcsFiles", files))
      .exec(UploadDocumentsFromSession(relatedTo))
  
  // Same journey, but the file list is already in the session as "pcsFiles" (e.g. set per user from a profile)
  def UploadDocumentsFromSession(relatedTo: String = "MAIN_CLAIM") =
    
    exec(_.set("pcsUploadedDocs", Vector.empty[String]).set("pcsRelatedTo", relatedTo.toUpperCase))
      
      .group("PCS_UploadDocs_040_StartEvidenceUpload") {
        exec(http("PCS_UploadDocs_040_005_StartEvidenceUpload")
          .get(PcsURL + "/case/#{caseId}/upload-additional-documents/start-evidence-upload")
          .headers(Headers.navigationHeader)
          .check(CsrfCheck.save)
          .check(substring("Upload additional documents")))
      }
      
      .pause(MinThinkTime, MaxThinkTime)
      
      .group("PCS_UploadDocs_050_StartEvidenceUploadSubmit") {
        exec(http("PCS_UploadDocs_050_005_StartEvidenceUploadSubmit")
          .post(PcsURL + "/case/#{caseId}/upload-additional-documents/start-evidence-upload")
          .headers(Headers.navigationHeader)
          .headers(Headers.postHeader)
          .formParam("_csrf", "#{csrf}")
          .check(CsrfCheck.save)
          .check(currentLocation.saveAs("pcsCurrentUrl"))
          .check(css("input[name='relatedApplicationId']", "value").findAll.optional.saveAs("pcsRelatedOptions"))
          // Claims with a counterclaim/application get the "Confirm if these documents relate..." page first
          .check(regex("Upload your documents|Confirm if these documents relate to an existing application").exists))
      }
      
      .pause(MinThinkTime, MaxThinkTime)
      
      // Only when PCS asks which application the documents relate to
      .doIf(session => session("pcsCurrentUrl").as[String].contains("confirm-if-these-documents-relate-to-an-application")) {
        
        exec(session => {
          val options = session("pcsRelatedOptions").asOption[Seq[String]].getOrElse(Seq.empty)
          val choice = session("pcsRelatedTo").as[String] match {
            case "COUNTERCLAIM" => options.find(_ != "MAIN_CLAIM").getOrElse("MAIN_CLAIM")
            case other          => if (options.contains(other)) other else "MAIN_CLAIM"
          }
          session.set("pcsRelatedApplicationId", choice)
        })
          
          .group("PCS_UploadDocs_055_ConfirmRelatedApplication") {
            exec(http("PCS_UploadDocs_055_005_ConfirmRelatedApplication")
              .post(PcsURL + "/case/#{caseId}/upload-additional-documents/confirm-if-these-documents-relate-to-an-application")
              .headers(Headers.navigationHeader)
              .headers(Headers.postHeader)
              .formParam("relatedApplicationId", "#{pcsRelatedApplicationId}")
              .formParam("_csrf", "#{csrf}")
              .check(CsrfCheck.save)
              .check(substring("Upload your documents")))
          }
          
          .pause(MinThinkTime, MaxThinkTime)
      }
      
      // One AJAX upload per file, exactly as the browser does after "Choose files"
      .foreach("#{pcsFiles}", "pcsFile", "pcsFileIndex") {
        exec(session => session
          .set("pcsFileContentType", contentTypeFor(session("pcsFile").as[String]))
          .set("pcsFileSizeLabel", sizeLabel(fileBytes(session("pcsFile").as[String]).length)))
          
          .group("PCS_UploadDocs_060_UploadFile") {
            exec(http("PCS_UploadDocs_060_005_UploadFile_#{pcsFileSizeLabel}")   // e.g. ..._UploadFile_10MB
              .post(PcsURL + "/case/#{caseId}/upload-additional-documents/upload-your-documents/upload")
              .header("accept", "*/*")
              .header("origin", PcsURL)
              .header("sec-fetch-dest", "empty")
              .header("sec-fetch-mode", "cors")
              .header("sec-fetch-site", "same-origin")
              .header("x-csrf-token", "#{csrf}")
              .bodyPart(ByteArrayBodyPart("documents", (session: Session) => fileBytes(session("pcsFile").as[String]).success)
                .fileName("#{pcsFile}")
                .contentType("#{pcsFileContentType}")
                .transferEncoding("binary"))
              .asMultipartForm
              .check(status.is(200))
              .check(jsonPath("$.success.messageText").exists)
              .check(jsonPath("$.document.index").ofType[Int].saveAs("pcsDocIndex"))
              .check(jsonPath("$.document.id").saveAs("pcsDocId"))
              .check(jsonPath("$.document.document_filename").saveAs("pcsDocFileName"))
              .check(jsonPath("$.document.sizeInBytes").ofType[Long].saveAs("pcsDocSize"))
              .check(jsonPath("$.document.content_type").saveAs("pcsDocContentType")))
          }
          
          // Build the hidden uploadedDocuments[] value the page would add for this file
          .exec(session => {
            val encoded = encodeDocument(
              session("pcsDocIndex").as[Int],
              session("pcsDocId").as[String],
              session("pcsDocFileName").as[String],
              session("pcsDocSize").as[Long],
              session("pcsDocContentType").as[String])
            session.set("pcsUploadedDocs", session("pcsUploadedDocs").as[Vector[String]] :+ encoded)
          })
          
          .pause(1)
      }
      
      .group("PCS_UploadDocs_070_UploadYourDocumentsContinue") {
        exec(http("PCS_UploadDocs_070_005_UploadYourDocumentsContinue")
          .post(PcsURL + "/case/#{caseId}/upload-additional-documents/upload-your-documents")
          .headers(Headers.navigationHeader)
          .headers(Headers.postHeader)
          .formParam("documents", "")
          .multivaluedFormParam("uploadedDocuments[]", "#{pcsUploadedDocs}")
          .formParam("action", "continue")
          .formParam("_csrf", "#{csrf}")
          .check(CsrfCheck.save)
          .check(substring("Check your answers"))
          .check(substring("#{pcsDocFileName}")))   // last uploaded file is listed on CYA
      }
      
      .pause(MinThinkTime, MaxThinkTime)
      
      .group("PCS_UploadDocs_080_CheckYourAnswersSubmit") {
        exec(http("PCS_UploadDocs_080_005_CheckYourAnswersSubmit")
          .post(PcsURL + "/case/#{caseId}/upload-additional-documents/check-your-answers")
          .headers(Headers.navigationHeader)
          .headers(Headers.postHeader)
          .formParam("_csrf", "#{csrf}")
          .check(substring("Documents uploaded")))
      }
      
      .pause(MinThinkTime, MaxThinkTime)
  
  /*====================================================================================
  * Logout
  *=====================================================================================*/
  
  val PCSLogout =
    
    group("PCS_UploadDocs_090_Logout") {
      exec(http("PCS_UploadDocs_090_005_Logout")
        .get(PcsURL + "/logout")
        .headers(Headers.navigationHeader)
        .check(status.in(200, 302)))
    }
  
}