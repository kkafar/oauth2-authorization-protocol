package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Optional;

/**
 * Checks if there is a valid authorization code and code_verifier (that is compared to code_challenge sent during authorization)
 * Not sure yet if redirect_url is needed in our case though so is not included here
 */
public class AuthorizationCodeGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, String> {

    private String authorizationCodeString;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!authorizationCodeValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        "invalid_grant");
            }

            if (!codeVerifierValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        "unauthorized_client"); // unauthorized_client was the best choice for me
            }

        } catch (IOException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        return next.handle(authorizationCodeString);
    }

    private boolean authorizationCodeValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> authorizationCodeString = bodyDecoder.fetchAttribute("code");

        if (authorizationCodeString.isPresent()) {
            this.authorizationCodeString = authorizationCodeString.get();
            // check if authorization code has been already used
            return true;
        } else return false;
    }

    private boolean codeVerifierValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> codeVerifierString = bodyDecoder.fetchAttribute("code_verifier");

        if (codeVerifierString.isPresent()) {
            // check if codeVerifier matches with codeChallenge sent during authorization of this client
            return true;
        } else return false;
    }
}
