package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantAccessTokenGenerator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant.AuthorizationCodeGrantTokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantAccessTokenGenerator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant.RefreshTokenGrantTokenRequestValidator;

/**
 * Builds handler chains for each grant_type that this authorization server can handle and provides getters for every
 * chain's first handler
 */
public final class TokenGrantTypesHandlerChainsBuilder {

    private Handler<HttpPostRequestDecoder, AuthCode> authorizationCodeGrantTokenRequestValidator;
    private Handler<HttpPostRequestDecoder, AuthCode> refreshTokenGrantTokenRequestValidator;

    private TokenGrantTypesHandlerChainsBuilder() { }

    public void buildChains() {
        buildAuthorizationCodeGrantHandlersChain();
        buildRefreshTokenGrantHandlersChain();
    }

    private void buildRefreshTokenGrantHandlersChain() {
        refreshTokenGrantTokenRequestValidator = new RefreshTokenGrantTokenRequestValidator();
        refreshTokenGrantTokenRequestValidator.setNextAndGet(new RefreshTokenGrantAccessTokenGenerator());
    }

    private void buildAuthorizationCodeGrantHandlersChain() {
        authorizationCodeGrantTokenRequestValidator = new AuthorizationCodeGrantTokenRequestValidator();
        authorizationCodeGrantTokenRequestValidator.setNextAndGet(new AuthorizationCodeGrantAccessTokenGenerator());
    }

    public Handler<HttpPostRequestDecoder, ?> getRefreshTokenGrantHandler() {
        if (refreshTokenGrantTokenRequestValidator == null) {
            buildRefreshTokenGrantHandlersChain();
        }
        return refreshTokenGrantTokenRequestValidator;
    }

    public Handler<HttpPostRequestDecoder, ?> getAuthorizationCodeGrantHandler() {
        if (authorizationCodeGrantTokenRequestValidator == null) {
            buildAuthorizationCodeGrantHandlersChain();
        }
        return authorizationCodeGrantTokenRequestValidator;
    }

    private static class SingletonHelper {
        private static final TokenGrantTypesHandlerChainsBuilder INSTANCE = new TokenGrantTypesHandlerChainsBuilder();
    }

    public static TokenGrantTypesHandlerChainsBuilder getInstance() {
        return TokenGrantTypesHandlerChainsBuilder.SingletonHelper.INSTANCE;
    }
}
