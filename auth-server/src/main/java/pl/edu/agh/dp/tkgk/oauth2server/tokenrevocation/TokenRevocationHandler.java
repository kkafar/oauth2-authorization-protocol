package pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Objects;

public class TokenRevocationHandler extends BaseHandler<HttpPostRequestDecoder, FullHttpRequest> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String NO_TOKEN_HINT = "no_token_hint";

    private String tokenString;
    private String tokenHint;

    private DecodedJWT tokenFromString;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            // token must be in the request body as checked in the TokenRevocationRequestValidator
            tokenString = bodyDecoder.fetchToken().orElse(INVALID_TOKEN);
            tokenHint = bodyDecoder.fetchTokenHint().orElse(NO_TOKEN_HINT);

            // todo: check if the token is assigned to the user that sent it to be revoked -> if it is not the user's token
            // then the revocation request should be refused
            decodeTokenString();
        } catch (IOException | JWTVerificationException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        revokeToken();

        // RFC7009 says that the error response with code 503 with error : unsupported_token_type is used only if the
        // authorization server can't handle the token revocation for a specific token type (refresh or access token)
        // or if this operation is unavailable for some time

        return responseWith200StatusCode();
    }

    private FullHttpResponse responseWith200StatusCode() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    private void decodeTokenString() throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(AuthorizationServerUtil.SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        tokenFromString = verifier.verify(tokenString);
    }

    // todo: how to store tokens in the db?
    // even if the token was invalid (but not of the type that is not handled by this server) and finally we dont
    // invalidate any of the tokens stored in the db it's no problem and we return response with 200 status code
    private void revokeToken() {
        Database database = AuthorizationDatabaseProvider.getInstance();

        // if token hint tells that token is an access token, then try to remove it from the access token collection
        // otherwise try to remove it from refresh token collection as the hint was invalid
        // this method may look bad, but it saves some time on db operations if the hint was correct
        if (Objects.equals(tokenHint, ACCESS_TOKEN)) {
            if (!database.revokeAccessToken()) {
                database.revokeRefreshToken();
            }
        }

        // if tokenHint == refresh_token or tokenHint was not present in the request's body
        else {
            if (!database.revokeRefreshToken()) {
                database.revokeAccessToken();
            }
        }
    }
}
