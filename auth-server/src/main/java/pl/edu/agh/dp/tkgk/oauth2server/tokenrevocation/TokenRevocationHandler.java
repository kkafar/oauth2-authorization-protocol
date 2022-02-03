package pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Objects;

public class TokenRevocationHandler extends BaseHandler<HttpPostRequestDecoder, FullHttpRequest> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String NO_TOKEN_HINT = "no_token_hint";

    private String tokenHint;

    private DecodedJWT decodedToken;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            String token = bodyDecoder.fetchToken().orElse(INVALID_TOKEN);
            tokenHint = bodyDecoder.fetchTokenHint().orElse(NO_TOKEN_HINT);
            decodedToken = TokenUtil.decodeToken(token);
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

    private void revokeToken() {
        Database database = AuthorizationDatabaseProvider.getInstance();
        database.tokenRevocation(decodedToken, Objects.equals(tokenHint, ACCESS_TOKEN));
    }
}
