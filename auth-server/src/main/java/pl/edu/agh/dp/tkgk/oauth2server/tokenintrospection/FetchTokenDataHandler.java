package pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.NoSuchAttributeException;

import java.io.IOException;
import java.util.Optional;

public class FetchTokenDataHandler extends BaseHandler<HttpPostRequestDecoder, JSONObject> {

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        DecodedJWT decodedJWT;
        TokenHint tokenHint;
        try {
            String tokenString = bodyDecoder.fetchToken()
                    .orElseThrow(() -> new NoSuchAttributeException("No token found in the request body"));
            tokenHint = bodyDecoder.fetchTokenHint();
            decodedJWT = TokenUtil.decodeToken(tokenString);

            Database database = AuthorizationDatabaseProvider.getInstance();
            Optional<Token> optionalToken = database.fetchToken(decodedJWT.getId(), tokenHint);

            return next.handle(tokenDataToJson(optionalToken));

        } catch (IOException | JWTVerificationException | NoSuchAttributeException e) {
            e.printStackTrace();
            return next.handle(new JSONObject().put(HttpParameters.ACTIVE, false));
        }
    }

    private JSONObject tokenDataToJson(Optional<Token> optionalToken) {
        JSONObject tokenDataJson;

        if (optionalToken.isPresent()) {
            DecodedToken decodedToken = optionalToken.get().getDecodedToken();

            if (decodedToken.isActive()) {
                tokenDataJson = new JSONObject(decodedToken);
                tokenDataJson.put(HttpParameters.CLIENT_ID, optionalToken.get().getClientId());
                tokenDataJson.put(HttpParameters.ACTIVE, true);
                return tokenDataJson;
            }
        }

        tokenDataJson = new JSONObject().put(HttpParameters.ACTIVE, false);
        return tokenDataJson;
    }
}
