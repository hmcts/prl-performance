package utils

object Environment {

 val baseURL = "https://manage-case.#{env}.platform.hmcts.net"
 val payURL = "https://card.payments.service.gov.uk"
 val prlURL = "https://privatelaw.#{env}.platform.hmcts.net"
 val idamURL = "https://idam-web-public.perftest.platform.hmcts.net"
 val idamAPIURL = "https://idam-api.#{env}.platform.hmcts.net"
 val rpeAPIURL = "http://rpe-service-auth-provider-#{env}.service.core-compute-#{env}.internal"
 val ccdAPIURL = "http://ccd-data-store-api-#{env}.service.core-compute-#{env}.internal"
 val prlCafcasURL="http://prl-cos-#{env}.service.core-compute-#{env}.internal"
 val cuiRaURL = "https://cui-ra.#{env}.platform.hmcts.net"
 val apiMgmtURL = "https://cft-api-mgmt.#{env}.platform.hmcts.net/"
 val pcqURL = "https://pcq.#{env}.platform.hmcts.net"
 val caseDocAPI = "http://ccd-case-document-am-api-#{env}.service.core-compute-#{env}.internal"

 val minThinkTime = 2
 val maxThinkTime = 4
 val waitTime = 60

}
