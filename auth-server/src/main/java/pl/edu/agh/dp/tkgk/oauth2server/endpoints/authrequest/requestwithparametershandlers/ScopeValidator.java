package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthEndpointUtil;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthErrorFragments;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.HttpRequestWithParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScopeValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> implements DatabaseInjectable {
    public static final String INVALID_SCOPE = "invalid_scope";

    private Database database;

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get(HttpParameters.REDIRECT_URI).get(0);
        String state = parameters.get(HttpParameters.STATE).get(0);

        if(!parameters.containsKey(HttpParameters.SCOPE)){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_SCOPE, AuthErrorFragments.SCOPE_IS_MISSING_FRAGMENT, redirect_uri, state);
        }

        Optional<String> invalidScopeEntries = getInvalidScopeEntries(parameters.get(HttpParameters.SCOPE).get(0), parameters.get(HttpParameters.CLIENT_ID).get(0));
        if(invalidScopeEntries.isPresent()){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_SCOPE, AuthErrorFragments.UNKNOWN_SCOPE_FRAGMENT, redirect_uri);
        }

        return next.handle(request);
    }

    private Optional<String> getInvalidScopeEntries(String scope, String clientId){
        String[] scopeEntries = scope.trim().split(" ");
        Client client = database.fetchClient(clientId).orElseThrow();
        StringBuilder invalidEntries = new StringBuilder();
        for(String s : scopeEntries){
            if(client.getScope().contains(s)) continue;
            invalidEntries.append(s).append(" ");
        }

        if(invalidEntries.length() == 0){
            return Optional.empty();
        }

        invalidEntries.deleteCharAt(invalidEntries.length() - 1);
        return Optional.of(invalidEntries.toString());
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
