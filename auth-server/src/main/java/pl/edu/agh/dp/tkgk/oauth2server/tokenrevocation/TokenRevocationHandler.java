package pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;

public class TokenRevocationHandler extends BaseHandler<HttpPostRequestDecoder, FullHttpRequest> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String INVALID_TOKEN = "invalid_token";
    private static final String NO_TOKEN_HINT = "no_token_hint";

    private TokenHint tokenHint;

    private DecodedJWT decodedToken;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            String token = bodyDecoder.fetchToken().orElse(INVALID_TOKEN);
            tokenHint = bodyDecoder.fetchTokenHint();
            decodedToken = TokenUtil.decodeToken(token);
        } catch (IOException | JWTVerificationException e) {
            e.printStackTrace();
            // todo: should probably just send back HTTP 200 instead of the one with error referring to the 7009 RFC,
            // todo: will leave it for the debug now
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        revokeToken();
        return responseWith200StatusCode();
    }

    private FullHttpResponse responseWith200StatusCode() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    private void revokeToken() {
        Database database = AuthorizationDatabaseProvider.getInstance();
        database.tokenRevocation(decodedToken, tokenHint);
    }
}
