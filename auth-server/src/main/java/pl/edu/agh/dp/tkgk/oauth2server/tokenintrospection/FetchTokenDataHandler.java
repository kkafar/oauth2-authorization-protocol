package pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

public class FetchTokenDataHandler extends BaseHandler<HttpPostRequestDecoder, JSONObject> {

    // todo: change the way the secret for HS256 is stored
    private static final String SECRET = "ultra-secret-key-that-is-at-least-32-bits-long-for-hs256-algorithm-top-secret";

    private String tokenString;
    private String tokenHint;

    private DecodedJWT tokenFromString;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        // token must be in the request body as checked in the TokenIntrospectionRequestValidator
        tokenString = bodyDecoder.fetchToken().orElse("invalid_token");
        tokenHint = bodyDecoder.fetchTokenHint().orElse("no_token_hint");

        decodeTokenString();

        Database dbInstance = AuthorizationDatabaseProvider.getInstance();

        JSONObject tokenDataJSON = dbInstance.fetchTokenData().orElse(new JSONObject().put("active", false));
        return next.handle(tokenDataJSON);
    }

    private void decodeTokenString() {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            tokenFromString = verifier.verify(tokenString);
        } catch (JWTVerificationException e) {
            e.printStackTrace();
        }
    }
}
