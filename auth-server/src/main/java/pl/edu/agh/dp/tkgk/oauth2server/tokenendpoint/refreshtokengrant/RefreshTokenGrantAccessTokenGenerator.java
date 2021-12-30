package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import io.netty.handler.codec.http.FullHttpResponse;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

/**
 * Generates access token and builds response JSON body (this class could probably be used in both grant type pipelines)
 */
public class RefreshTokenGrantAccessTokenGenerator extends BaseHandler<String, JSONObject> {

    @Override
    public FullHttpResponse handle(String refreshTokenString) {
        // generate new access token and store it in the db
        try {
            Algorithm algorithm = Algorithm.HMAC256(AuthorizationServerUtil.SECRET);
            String accessToken = JWT.create().sign(algorithm); // should have some claim with random string building the token
            JSONObject responseBody = new JSONObject();
            responseBody.put("access_token", accessToken);
            responseBody.put("token_type", "Bearer");
            responseBody.put("expires_in", 86400); // 1 day in seconds
            return next.handle(responseBody);
        } catch (JWTCreationException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }
    }
}
