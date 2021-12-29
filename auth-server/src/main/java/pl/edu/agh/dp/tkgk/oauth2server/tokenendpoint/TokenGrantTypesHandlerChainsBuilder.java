package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantTokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantTokenRequestValidator;

import java.util.Objects;

/**
 * Builds handler chains for each grant_type that this authorization server can handle and provides getters for every
 * chain's first handler
 */
public class TokenGrantTypesHandlerChainsBuilder {

    private static Handler<HttpPostRequestDecoder, ?> authorizationCodeGrantTokenRequestValidator;
    private static Handler<HttpPostRequestDecoder, ?> refreshTokenGrantTokenRequestValidator;

    private static Handler<HttpPostRequestDecoder, ?> buildRefreshTokenGrantTokenRequestHandlersChain() {
        refreshTokenGrantTokenRequestValidator =
                new RefreshTokenGrantTokenRequestValidator();
        return refreshTokenGrantTokenRequestValidator;
    }

    private static Handler<HttpPostRequestDecoder, ?> buildAuthorizationCodeGrantTokenRequestHandlersChain() {
        authorizationCodeGrantTokenRequestValidator =
                new AuthorizationCodeGrantTokenRequestValidator();
        return authorizationCodeGrantTokenRequestValidator;
    }

    public static Handler<HttpPostRequestDecoder, ?> getRefreshTokenGrantTokenRequestHandler() {
        return Objects.requireNonNullElseGet(refreshTokenGrantTokenRequestValidator,
                TokenGrantTypesHandlerChainsBuilder::buildRefreshTokenGrantTokenRequestHandlersChain);
    }

    public static Handler<HttpPostRequestDecoder, ?> getAuthorizationCodeGrantTokenRequestHandler() {
        return Objects.requireNonNullElseGet(authorizationCodeGrantTokenRequestValidator,
                TokenGrantTypesHandlerChainsBuilder::buildAuthorizationCodeGrantTokenRequestHandlersChain);
    }

    public static void buildChains() {
        buildAuthorizationCodeGrantTokenRequestHandlersChain();
        buildRefreshTokenGrantTokenRequestHandlersChain();
    }
}
