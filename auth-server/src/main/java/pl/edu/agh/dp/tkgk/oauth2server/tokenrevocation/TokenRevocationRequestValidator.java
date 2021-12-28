package pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TokenRevocationRequestValidator extends BaseHandler {

    private static final String CORRECT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String TOKEN_TYPE_HINT = "token_type_hint";

    // todo: change the way the secret for HS256 is stored
    private static final String SECRET = "ultra-secret-key-that-is-at-least-32-bits-long-for-hs256-algorithm-top-secret";

    private String tokenString;
    private String tokenHint;

    private DecodedJWT tokenFromString;

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if (!requestValid(request)) {
            return badRequestHttpResponse();
        }

        // todo: check if the token is assigned to the user that sent it to be revoked -> if it is not the user's token
        // then the revocation request should be refused
        decodeTokenString();
        revokeToken();

        // RFC7009 says that the error response with code 503 with error : unsupported_token_type is used only if the
        // authorization server can't handle the token revocation for a specific token type (refresh or access token)
        // or if this operation is unavailable for some time

        return tokenRevokedSuccessfully();
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

    private FullHttpResponse tokenRevokedSuccessfully() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    private boolean requestValid(FullHttpRequest request) {
        return validRequestMethod(request)
                && validContentType(request)
                && hasToken(request);
    }

    private boolean validRequestMethod(FullHttpRequest request) {
        HttpMethod method = request.method();
        return method.equals(HttpMethod.POST);
    }

    private boolean validContentType(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return Objects.equals(contentType, CORRECT_CONTENT_TYPE);
    }

    private boolean hasToken(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        InterfaceHttpData tokenData = decoder.getBodyHttpData("token");
        if (tokenData == null) return false;

        // if there is a token in the request's body - take it and check if there is a token type hint
        fetchToken(tokenData);
        checkForTokenHint(decoder);

        return true;
    }

    private void checkForTokenHint(HttpPostRequestDecoder decoder) {
        InterfaceHttpData tokenHintData = decoder.getBodyHttpData(TOKEN_TYPE_HINT);
        if (tokenHintData != null) {
            if (tokenHintData.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute tokenHintAttribute = (Attribute) tokenHintData;
                try {
                    tokenHint = tokenHintAttribute.getString(StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fetchToken(InterfaceHttpData tokenData) {
        if (tokenData.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute tokenAttribute = (Attribute) tokenData;
            try {
                tokenString = tokenAttribute.getString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private FullHttpResponse badRequestHttpResponse() {
        JSONObject json = new JSONObject();
        json.put("error", INVALID_REQUEST);

        ByteBuf content = Unpooled.copiedBuffer(json.toString(), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
