package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import model.AuthCode;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantAccessTokenGenerator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantTokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantAccessTokenGenerator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantTokenRequestValidator;

import java.util.Objects;

// TODO: Ask about static
/**
 * Builds handler chains for each grant_type that this authorization server can handle and provides getters for every
 * chain's first handler
 */
public class TokenGrantTypesHandlerChainsBuilder {

    private static Handler<HttpPostRequestDecoder, AuthCode> authorizationCodeGrantTokenRequestValidator;
    private static Handler<HttpPostRequestDecoder, AuthCode> refreshTokenGrantTokenRequestValidator;
    private static final Handler<JSONObject, ?> tokenResponseBuilder = new TokenResponseBuilder();

    private static Handler<HttpPostRequestDecoder, AuthCode> buildRefreshTokenGrantTokenRequestHandlersChain() {
        refreshTokenGrantTokenRequestValidator = new RefreshTokenGrantTokenRequestValidator();
        Handler<AuthCode, JSONObject> accessTokenGenerator = new RefreshTokenGrantAccessTokenGenerator();

        refreshTokenGrantTokenRequestValidator.setNext(accessTokenGenerator);
        accessTokenGenerator.setNext(tokenResponseBuilder);

        return refreshTokenGrantTokenRequestValidator;
    }

    private static Handler<HttpPostRequestDecoder, AuthCode> buildAuthorizationCodeGrantTokenRequestHandlersChain() {
        authorizationCodeGrantTokenRequestValidator = new AuthorizationCodeGrantTokenRequestValidator();
        Handler<AuthCode, JSONObject> accessTokenGenerator = new AuthorizationCodeGrantAccessTokenGenerator();

        authorizationCodeGrantTokenRequestValidator.setNext(accessTokenGenerator);
        accessTokenGenerator.setNext(tokenResponseBuilder);

        return authorizationCodeGrantTokenRequestValidator;
    }

    public static Handler<HttpPostRequestDecoder, AuthCode> getRefreshTokenGrantTokenRequestHandler() {
        return Objects.requireNonNullElseGet(refreshTokenGrantTokenRequestValidator,
                TokenGrantTypesHandlerChainsBuilder::buildRefreshTokenGrantTokenRequestHandlersChain);
    }

    public static Handler<HttpPostRequestDecoder, AuthCode> getAuthorizationCodeGrantTokenRequestHandler() {
        return Objects.requireNonNullElseGet(authorizationCodeGrantTokenRequestValidator,
                TokenGrantTypesHandlerChainsBuilder::buildAuthorizationCodeGrantTokenRequestHandlersChain);
    }

    public static void buildChains() {
        buildAuthorizationCodeGrantTokenRequestHandlersChain();
        buildRefreshTokenGrantTokenRequestHandlersChain();
    }
}
