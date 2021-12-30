package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

import java.nio.charset.StandardCharsets;

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
