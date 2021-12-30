package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Optional;

/**
 * Checks whether token is being requested using authorization code grant or refresh token and sends the request
 * to the correct handler. If grant type is invalid - returns response with error and 400 code status
 */
public class TokenGrantTypeDispatcher extends BaseHandler<HttpPostRequestDecoder, HttpPostRequestDecoder> {

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            Optional<String> grantTypeString = bodyDecoder.fetchAttribute("grant_type");

            if (grantTypeString.isPresent()) {

                if (grantTypeString.get().equals("refresh_token")) {
                    return TokenGrantTypesHandlerChainsBuilder.getRefreshTokenGrantTokenRequestHandler().handle(decoder);
                }

                else if (grantTypeString.get().equals("authorization_code")) {
                    return TokenGrantTypesHandlerChainsBuilder.getAuthorizationCodeGrantTokenRequestHandler().handle(decoder);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                "unsupported_grant_type");
    }
}
