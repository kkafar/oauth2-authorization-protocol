package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant;

import com.auth0.jwt.exceptions.JWTCreationException;
import io.netty.handler.codec.http.FullHttpResponse;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

/**
 * Generates access token and builds response JSON body
 */
public class RefreshTokenGrantAccessTokenGenerator extends BaseHandler<AuthCode, JSONObject> {

    private static final String BEARER = "Bearer";

    private static final int EXPIRE_IN_SECONDS_ACCESS_TOKEN = 86400;

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    @Override
    public FullHttpResponse handle(AuthCode authorizationCode) {
        try {
            Database database = AuthorizationDatabaseProvider.getInstance();

            Token accessToken = database.getNewToken(EXPIRE_IN_SECONDS_ACCESS_TOKEN, authorizationCode.getScope(),
                    authorizationCode.getCode(), false, BEARER, authorizationCode.getClientId());

            return next.handle(buildResponseBody(accessToken.getToken(), authorizationCode.getScopeItems()));
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }
    }

    private JSONObject buildResponseBody(String accessToken, String scopeItems) {
        JSONObject responseBody = new JSONObject();
        responseBody.put(TokenHint.ACCESS_TOKEN.toString(), accessToken);
        responseBody.put(HttpParameters.TOKEN_TYPE, BEARER);
        responseBody.put(HttpParameters.EXPIRES_IN, EXPIRE_IN_SECONDS_ACCESS_TOKEN);
        responseBody.put(HttpParameters.SCOPE, scopeItems);
        return responseBody;
    }
}
