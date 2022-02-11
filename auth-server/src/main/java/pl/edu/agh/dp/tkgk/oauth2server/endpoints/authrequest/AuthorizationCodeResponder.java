package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.UrlEncodedResponseBuilder;

public class AuthorizationCodeResponder extends BaseHandler<AuthorizationRequest, Void> implements DatabaseInjectable {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new UrlEncodedResponseBuilder();
    private Database database;

    @Override
    public FullHttpResponse handle(AuthorizationRequest request) {
        String authorizationCode = generateAuthorizationCodeForRequest(request);
        return buildCodeResponse(request, authorizationCode);
    }

    private FullHttpResponse buildCodeResponse(AuthorizationRequest request, String authorizationCode) {
        return director.constructUrlEncodedAuthCodeResponse(responseBuilder, request.redirectUri, request.state, authorizationCode);
    }

    private String generateAuthorizationCodeForRequest(AuthorizationRequest request) {
        return database.generateCode(request);
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
