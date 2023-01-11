package utils

object Environment {

 val baseURL = "https://manage-case.${env}.platform.hmcts.net"
 val payURL = "https://www.payments.service.gov.uk"
 val prlURL = "https://privatelaw.${env}.platform.hmcts.net"
 val idamURL = "https://idam-web-public.${env}.platform.hmcts.net"
 val idamAPIURL = "https://idam-api.${env}.platform.hmcts.net"
 val rpeAPIURL = "http://rpe-service-auth-provider-${env}.service.core-compute-${env}.internal"
 val ccdAPIURL = "http://ccd-data-store-api-${env}.service.core-compute-${env}.internal"

 val minThinkTime = 5
 val maxThinkTime = 7

}
