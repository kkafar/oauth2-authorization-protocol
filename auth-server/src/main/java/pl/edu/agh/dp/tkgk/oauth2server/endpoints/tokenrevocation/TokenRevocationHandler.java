package pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenrevocation;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.logging.Logger;

public class TokenRevocationHandler extends BaseHandler<HttpPostRequestDecoder, FullHttpRequest> {

    private static final String INVALID_TOKEN = "invalid_token";

    private TokenHint tokenHint;

    private DecodedJWT decodedToken;

    private final Logger logger = Logger.getGlobal();

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            String token = bodyDecoder.fetchToken().orElse(INVALID_TOKEN);
            tokenHint = bodyDecoder.fetchTokenHint();
            decodedToken = TokenUtil.decodeToken(token);
        } catch (IOException e) {
            logger.warning(e.getMessage());
            return responseWith200StatusCode();
        } catch (JWTVerificationException e) {
            logger.info(e.getMessage());
            return responseWith200StatusCode();
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
