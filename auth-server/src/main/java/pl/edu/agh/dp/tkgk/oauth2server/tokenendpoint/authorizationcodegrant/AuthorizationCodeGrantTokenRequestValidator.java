package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Optional;

/**
 * Checks if there is a valid authorization code and code_verifier (that is compared to code_challenge sent during authorization)
 * Not sure yet if redirect_url is needed in our case though so is not included here
 */
public class AuthorizationCodeGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, String> {

    private static final String REDIRECT_URI = "redirect_uri";
    private static final String INVALID_GRANT = "invalid_grant";
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    private static final String CODE = "code";
    private static final String CODE_VERIFIER = "code_verifier";

    private String authorizationCodeString;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!authorizationCodeValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        INVALID_GRANT);
            }

            if (!codeVerifierValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        UNAUTHORIZED_CLIENT); // unauthorized_client was the best choice for me
            }

            if (!uriValidIfAdded(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        INVALID_REQUEST);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        return next.handle(authorizationCodeString);
    }

    private boolean authorizationCodeValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> authorizationCodeString = bodyDecoder.fetchAttribute(CODE);

        if (authorizationCodeString.isPresent()) {
            this.authorizationCodeString = authorizationCodeString.get();
            // check if authorization code has been already used
            return true;
        } else return false;
    }

    private boolean codeVerifierValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> codeVerifierString = bodyDecoder.fetchAttribute(CODE_VERIFIER);

        if (codeVerifierString.isPresent()) {
            // check if codeVerifier matches with codeChallenge sent during authorization of this client
            return true;
        } else return false;
    }

    private boolean uriValidIfAdded(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> redirectUriString = bodyDecoder.fetchAttribute(REDIRECT_URI);

        Database database = AuthorizationDatabaseProvider.getInstance();

        Optional<String> authorizationRedirectUri = database.getAuthorizationRedirectUri(authorizationCodeString);

        if (authorizationRedirectUri.isEmpty()) return true;

        if (redirectUriString.isEmpty()) return false;

        return redirectUriString.get().equals(authorizationRedirectUri.get());
    }
}
