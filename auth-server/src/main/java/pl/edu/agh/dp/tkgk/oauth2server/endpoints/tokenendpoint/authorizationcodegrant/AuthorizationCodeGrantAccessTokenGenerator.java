package pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint.authorizationcodegrant;

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

public class AuthorizationCodeGrantAccessTokenGenerator extends BaseHandler<AuthCode, Object> {

    private static final String BEARER = "Bearer";

    private static final int EXPIRE_IN_DAYS_ACCESS_TOKEN = 1;
    private static final int EXPIRE_IN_SECONDS_ACCESS_TOKEN = 86400;
    private static final int EXPIRE_IN_DAYS_REFRESH_TOKEN = 7;

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private final Database database = AuthorizationDatabaseProvider.getInstance();

    @Override
    public FullHttpResponse handle(AuthCode authorizationCode) {
        try {
            Token accessToken = database.getNewTokenFromAuthCode(EXPIRE_IN_DAYS_ACCESS_TOKEN,
                    authorizationCode, true, BEARER);

            Token refreshToken = database.getNewToken(EXPIRE_IN_DAYS_REFRESH_TOKEN, authorizationCode.getScope(),
                    authorizationCode.getCode(), false, BEARER, authorizationCode.getClientId());

            JSONObject responseBody = buildResponseBody(accessToken.getToken(), refreshToken.getToken(), authorizationCode);

            return director.constructJsonResponse(responseBuilder, responseBody, HttpResponseStatus.OK, true);
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }
    }

    private JSONObject buildResponseBody(String accessToken, String refreshToken, AuthCode authorizationCode) {
        JSONObject responseBody = new JSONObject();
        responseBody.put(TokenHint.ACCESS_TOKEN.toString(), accessToken);
        responseBody.put(HttpParameters.TOKEN_TYPE, BEARER);
        responseBody.put(HttpParameters.EXPIRES_IN, EXPIRE_IN_SECONDS_ACCESS_TOKEN);
        responseBody.put(TokenHint.REFRESH_TOKEN.toString(), refreshToken);
        responseBody.put(HttpParameters.SCOPE, authorizationCode.getScopeItems());
        return responseBody;
    }
}
