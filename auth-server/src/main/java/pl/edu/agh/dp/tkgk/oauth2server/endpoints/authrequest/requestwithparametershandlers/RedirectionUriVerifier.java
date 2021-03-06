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

/**
 * Verifies if client_id and redirect_uri are present, if client_id is known
 * and if given redirect_uri matches that of client redirect_uri
 */

public class RedirectionUriVerifier extends BaseHandler<HttpRequestWithParameters,HttpRequestWithParameters> implements DatabaseInjectable {

    private Database database;


    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;

        if(!parameters.containsKey(HttpParameters.CLIENT_ID)){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.CLIENT_ID_IS_MISSING_FRAGMENT);
        }
        String clientId = parameters.get(HttpParameters.CLIENT_ID).get(0);

        if(!parameters.containsKey(HttpParameters.REDIRECT_URI)){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.REDIRECT_URI_IS_MISSING_FRAGMENT);
        }
        String redirectionUri = parameters.get(HttpParameters.REDIRECT_URI).get(0);

        Optional<Client> optionalClient = database.fetchClient(clientId);

        if(optionalClient.isEmpty()){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.UNKNOWN_CLIENT_ID_FRAGMENT);
        }

        Client client = optionalClient.get();
        if(!client.getRedirectUri().equals(redirectionUri)){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.CLIENT_ID_REDIRECT_URI_MISMATCH_FRAGMENT);
        }

        return next.handle(request);
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
