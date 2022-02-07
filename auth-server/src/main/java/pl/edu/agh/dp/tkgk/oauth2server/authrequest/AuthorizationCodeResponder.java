package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;

public class AuthorizationCodeResponder extends BaseHandler<AuthorizationRequest, Void> {
    @Override
    public FullHttpResponse handle(AuthorizationRequest request) {
        String authorizationCode = generateAuthorizationCodeForRequest(request);
        return buildCodeResponse(request, authorizationCode);
    }

    private FullHttpResponse buildCodeResponse(AuthorizationRequest request, String authorizationCode) {
        String url = request.redirectUri + "?state=" + request.state + "&code=" + authorizationCode;
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.LOCATION, url);
        return response;
    }

    private String generateAuthorizationCodeForRequest(AuthorizationRequest request) {
        return AuthorizationDatabaseProvider.getInstance().generateCode(request);
    }
}
