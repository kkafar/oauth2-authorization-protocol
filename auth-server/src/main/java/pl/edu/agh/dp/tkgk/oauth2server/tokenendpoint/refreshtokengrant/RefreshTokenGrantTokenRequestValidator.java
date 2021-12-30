package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Optional;


/**
 * Checks if refresh token and scope (if attached to the request) are valid so that the token request can be served
 */
public class RefreshTokenGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, String> {

    private String refreshTokenString;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {

        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!refreshTokenValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        "invalid_grant");
            }

            if (!scopeValidIfAdded(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        "invalid_scope");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        // dunno if refresh token will be needed in the next handler to make sure that access token is assigned to the
        // correct authorization code or something else for now
        return next.handle(refreshTokenString);
    }

    private boolean scopeValidIfAdded(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> scopeString = bodyDecoder.fetchAttribute("scope");

        if (scopeString.isPresent()) {
            // check if attached scope does not exceed scope assigned in the authorization process to the refresh token
            // sent with this request
            return true;
        } else return true;
    }

    private boolean refreshTokenValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> refreshTokenString = bodyDecoder.fetchAttribute("refresh_token");

        if (refreshTokenString.isPresent()) {
            this.refreshTokenString = refreshTokenString.get();

            // check if refresh token is valid -> exists in the db, did not expire, was not revoked etc.

            return true;
        } else return false;
    }
}
