package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.util.HtmlWithTitleAndContent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Verifies if client_id and redirect_uri are present, if client_id is known
 * and if given redirect_uri matches that of client redirect_uri
 */

public class RedirectionUriVerifier extends BaseHandler<HttpRequestWithParameters,HttpRequestWithParameters> implements DatabaseInjectable {

    private static final String REDIRECTION_URI_ERROR = "Redirection uri error";
    private Database database;
    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String message;

        if(!parameters.containsKey(HttpParameters.CLIENT_ID)){
            message = "client_id is missing";
            return director.constructHtmlResponse(responseBuilder,
                    new HtmlWithTitleAndContent(REDIRECTION_URI_ERROR, message).getHtml(), HttpResponseStatus.OK);
        }
        String clientId = parameters.get(HttpParameters.CLIENT_ID).get(0);

        if(!parameters.containsKey(HttpParameters.REDIRECT_URI)){
            message = "redirect_uri is missing";
            return director.constructHtmlResponse(responseBuilder,
                    new HtmlWithTitleAndContent(REDIRECTION_URI_ERROR, message).getHtml(), HttpResponseStatus.OK);
        }
        String redirectionUri = parameters.get(HttpParameters.REDIRECT_URI).get(0);

        database = AuthorizationDatabaseProvider.getInstance();
        Optional<Client> optionalClient = database.fetchClient(clientId);

        if(optionalClient.isEmpty()){
            message = "Unknown client_id";
            return director.constructHtmlResponse(responseBuilder,
                    new HtmlWithTitleAndContent(REDIRECTION_URI_ERROR, message).getHtml(), HttpResponseStatus.OK);
        }

        Client client = optionalClient.get();
        if(!client.getRedirectUri().equals(redirectionUri)){
            message = "Given redirect_id does not match client redirect_uri";
            return director.constructHtmlResponse(responseBuilder,
                    new HtmlWithTitleAndContent(REDIRECTION_URI_ERROR, message).getHtml(), HttpResponseStatus.OK);
        }

        return next.handle(request);
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
