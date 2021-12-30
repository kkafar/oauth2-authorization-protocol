package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScopeValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {
    private static final String SCOPE_NOT_PRESENT_URI = "scope_not_present";
    private static final String UNKNOWN_SCOPE_URI = "unknown_scope";

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get("redirect_uri").get(0);
        String state = parameters.get("state").get(0);

        if(!parameters.containsKey("scope")){
            return AuthorizationServerUtil.buildErrorResponse("invalid_scope", SCOPE_NOT_PRESENT_URI, redirect_uri, state);
        }

        Optional<String> invalidScopeEntries = getInvalidScopeEntries(parameters.get("scope").get(0), parameters.get("client_id").get(0));
        if(invalidScopeEntries.isPresent()){
            return AuthorizationServerUtil.buildErrorResponse("invalid_scope", UNKNOWN_SCOPE_URI, redirect_uri, state);
        }

        return next.handle(request);
    }

    private Optional<String> getInvalidScopeEntries(String scope, String clientId){
        String[] scopeEntries = scope.split(" ");
        Database database = AuthorizationDatabaseProvider.getInstance();
        Client client = database.getClient(clientId).orElseThrow();
        StringBuilder invalidEntries = new StringBuilder();
        for(String s : scopeEntries){
            if(client.scope.contains(s)) continue;
            invalidEntries.append(s).append(" ");
        }

        if(invalidEntries.length() == 0){
            return Optional.empty();
        }

        invalidEntries.deleteCharAt(invalidEntries.length() - 1);
        return Optional.of(invalidEntries.toString());
    }
}
