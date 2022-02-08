package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;

public class AuthorizationCodeResponder extends BaseHandler<AuthorizationRequest, Void> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(AuthorizationRequest request) {
        String authorizationCode = generateAuthorizationCodeForRequest(request);
        return buildCodeResponse(request, authorizationCode);
    }

    private FullHttpResponse buildCodeResponse(AuthorizationRequest request, String authorizationCode) {
        return director.constructUrlEncodedAuthCodeResponse(responseBuilder, request.redirectUri, request.state, authorizationCode);
    }

    private String generateAuthorizationCodeForRequest(AuthorizationRequest request) {
        return AuthorizationDatabaseProvider.getInstance().generateCode(request);
    }
}
