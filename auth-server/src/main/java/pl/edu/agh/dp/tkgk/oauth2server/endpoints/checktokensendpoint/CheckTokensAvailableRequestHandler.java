package pl.edu.agh.dp.tkgk.oauth2server.endpoints.checktokensendpoint;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckTokensAvailableRequestHandler extends BaseHandler<HttpPostRequestDecoder, Object> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private final Logger logger = Logger.getGlobal();

    private String refreshToken;
    private String accessToken;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!getTokensFromBody(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, false);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage());
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }

        Database database = AuthorizationDatabaseProvider.getInstance();

        boolean accessTokenAvailable = database.fetchToken(accessToken, TokenHint.ACCESS_TOKEN).isPresent();
        boolean refreshTokenAvailable = database.fetchToken(refreshToken, TokenHint.REFRESH_TOKEN).isPresent();

        JSONObject content = new JSONObject()
                .put(TokenHint.ACCESS_TOKEN.toString(), accessTokenAvailable)
                .put(TokenHint.REFRESH_TOKEN.toString(), refreshTokenAvailable);

        return director.constructJsonResponse(responseBuilder, content, HttpResponseStatus.OK, false);
    }

    private boolean getTokensFromBody(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> optionalAccessToken = bodyDecoder.fetchAttribute(TokenHint.ACCESS_TOKEN.toString());
        Optional<String> optionalRefreshToken = bodyDecoder.fetchAttribute(TokenHint.REFRESH_TOKEN.toString());

        if (optionalAccessToken.isEmpty() || optionalRefreshToken.isEmpty()) return false;

        accessToken = optionalAccessToken.get();
        refreshToken = optionalRefreshToken.get();
        return true;
    }
}
