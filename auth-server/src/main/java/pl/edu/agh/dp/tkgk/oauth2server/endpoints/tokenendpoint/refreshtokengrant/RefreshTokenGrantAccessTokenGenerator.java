package pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint.refreshtokengrant;

import com.auth0.jwt.exceptions.JWTCreationException;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

import java.util.logging.Logger;

/**
 * Generates access token and builds response JSON body
 */
public class RefreshTokenGrantAccessTokenGenerator extends BaseHandler<AuthCode, Object> {

    private static final String BEARER = "Bearer";

    private static final int EXPIRE_IN_SECONDS_ACCESS_TOKEN = 86400;
    private static final int EXPIRE_IN_DAYS_ACCESS_TOKEN = 1;


    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private final Logger logger = Logger.getGlobal();

    @Override
    public FullHttpResponse handle(AuthCode authorizationCode) {
        try {
            Database database = AuthorizationDatabaseProvider.getInstance();

            Token accessToken = database.getNewToken(EXPIRE_IN_DAYS_ACCESS_TOKEN, authorizationCode.getScope(),
                    authorizationCode.getCode(), true, BEARER, authorizationCode.getClientId());

            JSONObject responseBody = buildResponseBody(accessToken.getToken(), authorizationCode.getScopeItems());

            return director.constructJsonResponse(responseBuilder, responseBody, HttpResponseStatus.OK, true);
        } catch (JWTCreationException e) {
            logger.warning(e.getMessage());
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
