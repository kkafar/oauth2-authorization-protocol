# Quick start

First you must generate a certificate to do so go to resources/pl/edu/agh/dp/tkgk/oauth2server. Then edit ssl_config to 
include server ip (Change 192.168.55.109 to your local ip). Next run generate_key.sh to generate a certificate. Finally,
make your system trust generated certificate ([Windows](https://docs.microsoft.com/en-us/skype-sdk/sdn/articles/installing-the-trusted-root-certificate)).  

If you use [Postman](https://www.postman.com/downloads/), you can the final step and add cert only in postman 
(File -> Settings -> Certificates -> CA Certificates) [**Please use it**].  

To check if everything is working go to https://server_ip:port/ping, default port = 8080, if cert was added system-wide
you can check on browser, if cert was added only in postman use postman.


