# HubitatMSMail
Send email notifications from Hubitat using Microsoft Mail via Microsoft Graph

- Created so Hubitat can send e-mail alerts without SMTP and to any e-mail address from a personal Microsoft mail account.
- Requires an Azure App Registration, a secret credential and a refresh token.  Once in hand, this driver can be used to send e-mail notifications.  
- Note the 255-character limit prevents using a variable for the refresh token, so it must be in the code.  See "<your_refresh_token_here>" in the driver code.
