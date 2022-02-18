package pl.edu.agh.dp.tkgk.oauth2server.server.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.common.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.adminpanel.AdminPageResponder;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.adminpanel.InvalidateTokenHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.autherrorpage.ErrorPageResponder;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.authrequesthandlers.AuthorizationCodeResponder;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.authrequesthandlers.IdentityVerifier;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.authrequesthandlers.ScopeAcceptedVerifier;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.converters.FullHttpRequest2HttpRequestWithParameters;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.converters.HttpRequestWithParameters2AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.fullrequesthandlers.HttpHeadersValidator;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.fullrequesthandlers.RepeatingGetParametersChecker;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers.*;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.checkloggedoutendpoint.CheckLoggedOutRequestHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.checkloggedoutendpoint.CheckLoggedOutRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.pong.PingHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint.TokenGrantTypeDispatcher;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint.TokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenintrospection.FetchTokenDataHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenintrospection.TokenIntrospectionRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenrevocation.TokenRevocationHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenrevocation.TokenRevocationRequestValidator;

import java.util.HashMap;
import java.util.List;

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
        buildAuthErrorPageEndpoint();
        buildAdminEndpoint();
        buildCheckUserLoggedOutByAdminEndpoint();
    }

    private void buildAdminEndpoint() {
        Handler<FullHttpRequest,FullHttpRequest> firstHandler = new InvalidateTokenHandler();
        firstHandler.setNextAndGet(new AdminPageResponder());
        injectDatabase(firstHandler.getChain(), AuthorizationDatabaseProvider.getInstance());

        endpointHandlerMap.put("/admin", firstHandler);
    }

    private void buildCheckUserLoggedOutByAdminEndpoint() {
        Handler<FullHttpRequest,HttpPostRequestDecoder> firstHandler = new CheckLoggedOutRequestValidator();
        firstHandler.setNextAndGet(new CheckLoggedOutRequestHandler());

        endpointHandlerMap.put("/checkLoggedOutByAdmin", firstHandler);
    }

    private void buildAuthErrorPageEndpoint() {
        Handler<FullHttpRequest, ?> firstHandler = new ErrorPageResponder();
        endpointHandlerMap.put("/auth_error", firstHandler);
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

        injectDatabase(authFirstHandler.getChain(), AuthorizationDatabaseProvider.getInstance());

        endpointHandlerMap.put("/authorize", authFirstHandler);
    }

    private void injectDatabase(List<Handler<?, ?>> handlers, Database database) {
        handlers.stream()
                .filter(handler -> handler instanceof DatabaseInjectable)
                .map(handler -> (DatabaseInjectable)handler)
                .forEach(injectable -> injectable.setDatabase(database));
    }

    private void buildPingEndpoint() {
        Handler<FullHttpRequest, ?> pingHandler = new PingHandler();
        endpointHandlerMap.put("/ping", pingHandler);
    }
}
