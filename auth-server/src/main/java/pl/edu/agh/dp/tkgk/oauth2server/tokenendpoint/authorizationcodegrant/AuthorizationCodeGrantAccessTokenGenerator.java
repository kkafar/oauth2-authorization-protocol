package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import io.netty.handler.codec.http.FullHttpResponse;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

public class AuthorizationCodeGrantAccessTokenGenerator extends BaseHandler<String, JSONObject> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String TOKEN_TYPE = "token_type";
    private static final String BEARER = "Bearer";
    private static final String EXPIRES_IN = "expires_in";
    private static final String SCOPE = "scope";

    private static final int ONE_DAY_IN_SECONDS = 86400;

    @Override
    public FullHttpResponse handle(String authorizationCodeString) {
        // generate new access token and store it in the db
        try {
            Algorithm algorithm = Algorithm.HMAC256(AuthorizationServerUtil.SECRET);
            String accessToken = JWT.create().sign(algorithm); // should have some claim with random string building the token
            String refreshToken = JWT.create().sign(algorithm); // same as above
            JSONObject responseBody = new JSONObject();
            // set authorizationCode as used in the db
            responseBody.put(ACCESS_TOKEN, accessToken);
            responseBody.put(TOKEN_TYPE, BEARER);
            responseBody.put(EXPIRES_IN, ONE_DAY_IN_SECONDS);
            responseBody.put(REFRESH_TOKEN, refreshToken);
            responseBody.put(SCOPE, "something");
            return next.handle(responseBody);
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }
    }
}
