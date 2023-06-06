/**
 *  Microsoft Mail Notifications Driver
 *  https://raw.githubusercontent.com/......
 *
 *  Copyright 2022 Cheetoh
 *  
 *  YOU MUST create an App Registration in Azure AD with the following Microsoft Graph delegate permissions: email, mail.send, offline_access, openid,
 *  and User.Read. Be sure to do this with your microsoft personal account (that messages will come from) at portal.azure.com.   
 *  Using a secret credential, you'll acquire an authorization code and refresh token.  Once you have the refresh token, you can use this driver to
 *  send e-mail from your microsoft account.
 *
 * 
 *  Change History:
 *
 *    Date        Who             	What
 *    ----        ---             	----
 *    2022-02-08  Cheetoh   	Original Creation
 * 
 */

metadata {
    definition (name: "Microsoft Mail Notifications Driver", namespace: "cheetoh", author: "Cheetoh72") {
        capability "Notification"
    }

preferences {
	input name: "emailAddress", type: "string", title: "Sender (Account) Email Address", description: "Your Microsoft Mail e-mail Address", required: true
    input name: "recipientAddress", type: "string", title: "Recipient Email Address", description: "Address to send notification to", required: true
	input name: "appId", type: "string", title: "Azure App ID", description: "Azure App ID for your Microsoft Mail App registration", required: true
	input name: "secretValue", type: "string", title: "Secret", description: "Azure App registration credential secret (DO NOT SHARE)", required: true
    /** Unable to use this imput as the refresh token is too long ( > 255 characters ) *****   
    input name: "refreshToken", type: "string", title: "Refresh Token", description: "Refresh Token (DO NOT SHARE)", required: true
    */
	input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def logsOff(){
    log.warn "Debug logging disabled."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def installed() {
    updated()
}

def updated() {
	if (logEnable) {
		runIn(1800,logsOff)
		log.debug "Updated App Id: $appId"
	}
}

def parse(String description) {
}

def deviceNotification(String msg) {
	if (logEnable) log.debug "sending message: $msg"
	sendMail("$msg")
	}

/** Let's get an OAuth2.0 access token */
def getToken() {
	def dollar = "\$"
	def params = [
		uri: "https://login.live.com/oauth20_token.srf",
		requestContentType: "application/x-www-form-urlencoded",
		contentType: "application/json",
		/** headers: "$Headers", */
		body:  [
			client_id: "$appId",
			client_secret: "$secretValue",
			refresh_token: "<your_token_here>",
			grant_type: "refresh_token"
			]
		]
	try {
		httpPost(params) { resp ->
			resp.headers.each {
				if (logEnable) log.debug "${it.name} : ${it.value}"
			}
			tokenResponse = resp.data.access_token
			/** log.debug "token: $tokenResponse" */
		}
	} 
	catch (e) {
		log.error "something went wrong: $e"
	}
	return tokenResponse
}

def sendMail(String message) {

    /** Construct the HTTP Body */
    Map addresses = [ 
        "emailAddress": [
            "address": "$recipientAddress"
        ]
    ]
    List recipients = [addresses]
    
	Map body = [
		"message": [
			"subject": "Alert - Message from Hubitat",
			"body": [
				"contentType": "Text",
				"content": "$message"
				],
            "toRecipients": recipients
            ]  
		]

		Map headers =  [
		"Content-Type": "application/json",
 	    "Authorization": "Bearer ${getToken()}" 
		]

	Map params = [
		"headers": headers,
		"uri": "https://graph.microsoft.com/v1.0/users/$emailAddress/sendMail",
        "body": body
		]
	
    if (logEnable) {
            for ( w in params ) {
		    log.debug w
		    }
    }
    try {
		httpPostJson(params) { resp ->
			resp.headers.each {
				log.debug "${it.name} : ${it.value}"
			}
			/** log.debug resp.data */
		}
	}
	catch (e) {
        if (logEnable) { 
            log.debug "Sending: something mightah went wrong: $e"
        }
	}
	
}
