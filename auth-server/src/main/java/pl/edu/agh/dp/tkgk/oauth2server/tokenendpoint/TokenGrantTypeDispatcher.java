package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.util.Optional;

/**
 * Checks whether token is being requested using authorization code grant or refresh token and sends the request
 * to the correct handler. If grant type is invalid - returns response with error and 400 code status
 */
public class TokenGrantTypeDispatcher extends BaseHandler<HttpPostRequestDecoder, HttpPostRequestDecoder> {

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        InterfaceHttpData grantTypeData = decoder.getBodyHttpData("grant_type");
        Optional<String> grantTypeString = bodyDecoder.getStringFromData(grantTypeData);

        if (grantTypeString.isPresent()) {

            if (grantTypeString.get().equals("refresh_token")) {
                return TokenGrantTypesHandlerChainsBuilder.getRefreshTokenGrantTokenRequestHandler().handle(decoder);
            }

            else if (grantTypeString.get().equals("authorization_code")) {
                return TokenGrantTypesHandlerChainsBuilder.getAuthorizationCodeGrantTokenRequestHandler().handle(decoder);
            }

        }

        return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                new JSONObject().put("error", "unsupported_grant_type"));
    }
}
