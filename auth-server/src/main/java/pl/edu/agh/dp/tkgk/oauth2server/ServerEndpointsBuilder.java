package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.*;
import pl.edu.agh.dp.tkgk.oauth2server.errorsendpoint.ErrorsPageHandler;
import pl.edu.agh.dp.tkgk.oauth2server.pong.PingHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.TokenGrantTypeDispatcher;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.TokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.FetchTokenDataHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.TokenIntrospectionRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationRequestValidator;

import java.util.HashMap;

public class ServerEndpointsBuilder {

    HashMap<String, Handler<FullHttpRequest, ?>> endpointHandlerMap;

    public ServerEndpointsBuilder() {
        this.endpointHandlerMap = new HashMap<>();
        buildServerEndpoints();
    }

    public HashMap<String, Handler<FullHttpRequest, ?>> getEndpointHandlerMap() {
        return endpointHandlerMap;
    }

    private void buildServerEndpoints() {
        buildRevocationEndpoint();
        buildIntrospectionEndpoint();
        buildTokenEndpoint();
        buildPingEndpoint();
        buildAuthorizationEndpoint();
        buildErrorsPageEndpoint();
    }

    private void buildRevocationEndpoint() {
        Handler<FullHttpRequest, HttpPostRequestDecoder> tokenRevocationRequestValidator =
                new TokenRevocationRequestValidator();

        tokenRevocationRequestValidator.setNextAndGet(new TokenRevocationHandler());

        endpointHandlerMap.put("/revoke", tokenRevocationRequestValidator);
    }

    private void buildIntrospectionEndpoint() {
        Handler<FullHttpRequest, HttpPostRequestDecoder> tokenIntrospectionRequestValidator =
                new TokenIntrospectionRequestValidator();

        tokenIntrospectionRequestValidator
                .setNextAndGet(new FetchTokenDataHandler());

        endpointHandlerMap.put("/introspect", tokenIntrospectionRequestValidator);
    }

    private void buildTokenEndpoint() {
        Handler<FullHttpRequest, HttpPostRequestDecoder> tokenRequestValidator = new TokenRequestValidator();

        tokenRequestValidator.setNextAndGet(new TokenGrantTypeDispatcher());

        endpointHandlerMap.put("/token", tokenRequestValidator);
    }

    private void buildAuthorizationEndpoint() {
        Handler<FullHttpRequest, FullHttpRequest> authFirstHandler = new HttpHeadersValidator();

        authFirstHandler
                .setNextAndGet(new RepeatingGetParametersChecker())
                .setNextAndGet(new FullHttpRequest2HttpRequestWithParameters())
                .setNextAndGet(new RedirectionUriVerifier())
                .setNextAndGet(new StateValidator())
                .setNextAndGet(new ResponseTypeVerifier())
                .setNextAndGet(new ScopeValidator())
                .setNextAndGet(new CodeChallengeValidator())
                .setNextAndGet(new HttpRequestWithParameters2AuthorizationRequest())
                .setNextAndGet(new IdentityVerifier())
                .setNextAndGet(new ScopeAcceptedVerifier())
                .setNextAndGet(new AuthorizationCodeResponder());

        endpointHandlerMap.put("/authorize", authFirstHandler);
    }

    private void buildErrorsPageEndpoint() {
        Handler<FullHttpRequest, Void> errorsPageHandler = new ErrorsPageHandler();
        endpointHandlerMap.put("/errors_page", errorsPageHandler);
    }

    private void buildPingEndpoint() {
        Handler<FullHttpRequest, ?> pingHandler = new PingHandler();
        endpointHandlerMap.put("/ping", pingHandler);
    }
}
