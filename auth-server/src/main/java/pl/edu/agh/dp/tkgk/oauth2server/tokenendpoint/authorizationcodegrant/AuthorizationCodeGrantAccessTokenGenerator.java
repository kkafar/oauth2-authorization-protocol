package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import com.auth0.jwt.exceptions.JWTCreationException;
import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

public class AuthorizationCodeGrantAccessTokenGenerator extends BaseHandler<AuthCode, JSONObject> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String TOKEN_TYPE = "token_type";
    private static final String BEARER = "Bearer";
    private static final String EXPIRES_IN = "expires_in";
    private static final String SCOPE = "scope";

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

            return next.handle(buildResponseBody(accessToken.getToken(), refreshToken.getToken(), authorizationCode));
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }
    }

    private JSONObject buildResponseBody(String accessToken, String refreshToken, AuthCode authorizationCode) {
        JSONObject responseBody = new JSONObject();
        responseBody.put(ACCESS_TOKEN, accessToken);
        responseBody.put(TOKEN_TYPE, BEARER);
        responseBody.put(EXPIRES_IN, EXPIRE_IN_SECONDS_ACCESS_TOKEN);
        responseBody.put(REFRESH_TOKEN, refreshToken);
        responseBody.put(SCOPE, authorizationCode.getScopeItems());
        return responseBody;
    }
}
