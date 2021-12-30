package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantAccessTokenGenerator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantTokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantAccessTokenGenerator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantTokenRequestValidator;

import java.util.Objects;

/**
 * Builds handler chains for each grant_type that this authorization server can handle and provides getters for every
 * chain's first handler
 */
public class TokenGrantTypesHandlerChainsBuilder {

    private static Handler<HttpPostRequestDecoder, String> authorizationCodeGrantTokenRequestValidator;
    private static Handler<HttpPostRequestDecoder, String> refreshTokenGrantTokenRequestValidator;
    private static final Handler<JSONObject, ?> tokenResponseBuilder = new TokenResponseBuilder();

    private static Handler<HttpPostRequestDecoder, String> buildRefreshTokenGrantTokenRequestHandlersChain() {
        refreshTokenGrantTokenRequestValidator = new RefreshTokenGrantTokenRequestValidator();
        Handler<String, JSONObject> accessTokenGenerator = new RefreshTokenGrantAccessTokenGenerator();

        refreshTokenGrantTokenRequestValidator.setNext(accessTokenGenerator);
        accessTokenGenerator.setNext(tokenResponseBuilder);

        return refreshTokenGrantTokenRequestValidator;
    }

    private static Handler<HttpPostRequestDecoder, String> buildAuthorizationCodeGrantTokenRequestHandlersChain() {
        authorizationCodeGrantTokenRequestValidator = new AuthorizationCodeGrantTokenRequestValidator();
        Handler<String, JSONObject> accessTokenGenerator = new AuthorizationCodeGrantAccessTokenGenerator();

        authorizationCodeGrantTokenRequestValidator.setNext(accessTokenGenerator);
        accessTokenGenerator.setNext(tokenResponseBuilder);

        return authorizationCodeGrantTokenRequestValidator;
    }

    public static Handler<HttpPostRequestDecoder, String> getRefreshTokenGrantTokenRequestHandler() {
        return Objects.requireNonNullElseGet(refreshTokenGrantTokenRequestValidator,
                TokenGrantTypesHandlerChainsBuilder::buildRefreshTokenGrantTokenRequestHandlersChain);
    }

    public static Handler<HttpPostRequestDecoder, String> getAuthorizationCodeGrantTokenRequestHandler() {
        return Objects.requireNonNullElseGet(authorizationCodeGrantTokenRequestValidator,
                TokenGrantTypesHandlerChainsBuilder::buildAuthorizationCodeGrantTokenRequestHandlersChain);
    }

    public static void buildChains() {
        buildAuthorizationCodeGrantTokenRequestHandlersChain();
        buildRefreshTokenGrantTokenRequestHandlersChain();
    }
}
