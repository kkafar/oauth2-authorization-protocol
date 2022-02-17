# Quick start

First you must generate a certificate. 
To do so, go to resources/pl/edu/agh/dp/oauth2server.
Then edit ssl_config to include server IP (last line of the file - replace A.B.C.D with your local IP).
Next, run generate_key.sh to generate a certificate.
Finally, make your system trust your generated certificate ([Windows](https://docs.microsoft.com/en-us/skype-sdk/sdn/articles/installing-the-trusted-root-certificate)).

If you use [Postman](https://www.postman.com/downloads/), you can alternatively add certificate (cert.pem) only in postman
(File -> Settings -> Certificates -> CA Certificates).