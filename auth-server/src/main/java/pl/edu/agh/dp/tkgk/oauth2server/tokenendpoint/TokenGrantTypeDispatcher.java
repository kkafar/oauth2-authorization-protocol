package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

import java.io.IOException;
import java.util.Optional;

/**
 * Checks whether token is being requested using authorization code grant or refresh token and sends the request
 * to the correct handler. If grant type is invalid - returns response with error and 400 code status
 */
public class TokenGrantTypeDispatcher extends BaseHandler<HttpPostRequestDecoder, HttpPostRequestDecoder> {

    private static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String GRANT_TYPE = "grant_type";

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            Optional<String> grantTypeString = bodyDecoder.fetchAttribute(GRANT_TYPE);

            if (grantTypeString.isPresent()) {

                if (grantTypeString.get().equals(REFRESH_TOKEN)) {
                    return TokenGrantTypesHandlerChainsBuilder.getRefreshTokenGrantTokenRequestHandler().handle(decoder);
                }

                else if (grantTypeString.get().equals(AUTHORIZATION_CODE)) {
                    return TokenGrantTypesHandlerChainsBuilder.getAuthorizationCodeGrantTokenRequestHandler().handle(decoder);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }

        return director.constructJsonBadRequestErrorResponse(responseBuilder, UNSUPPORTED_GRANT_TYPE, true);
    }
}
