package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.UrlEncodedResponseBuilder;

import java.io.FileNotFoundException;

public class IdentityVerifier extends BaseHandler<AuthorizationRequest, AuthorizationRequest> {
    private final Database database = AuthorizationDatabaseProvider.getInstance();

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> htmlResponseBuilder = new ResponseWithCustomHtmlBuilder();
    private final ResponseBuilder<String> urlResponseBuilder = new UrlEncodedResponseBuilder();

    @Override
    public FullHttpResponse handle(AuthorizationRequest request) {
        if(request.getOptionalSessionId().isPresent()){
            String sessionId  = request.getOptionalSessionId().get();
            if(isSessionIdValid(sessionId)){
                return next.handle(request);
            }
        }


        if(request.getOptionalCredentials().isPresent()){
            Credentials credentials = request.getOptionalCredentials().get();
            if(areCredentialsValid(credentials)){
                String sessionId = createNewSession(credentials.login());
                return buildSetSessionIdResponse(request, sessionId);
            }else {
                return buildWrongCredentialsResponse(request);
            }
        }

        try {
            return buildLoginPageResponse(request);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return director.constructHtmlServerErrorResponse(htmlResponseBuilder,
                    "Couldn't open html/login_page.html");
        }
    }

    private FullHttpResponse buildLoginPageResponse(AuthorizationRequest request) throws FileNotFoundException {
        String loginPage = AuthorizationServerUtil.loadTextResource("html/login_page.html");
        loginPage = loginPage.replace("$SUBMIT_URL", request.uri);
        return director.constructHtmlResponse(htmlResponseBuilder, loginPage, HttpResponseStatus.OK);
    }

    private FullHttpResponse buildWrongCredentialsResponse(AuthorizationRequest request) {
        return director.constructUrlEncodedErrorResponse(urlResponseBuilder, request.redirectUri,
                "Invalid credentials", "#invalid_credentials", request.state);
    }

    private FullHttpResponse buildSetSessionIdResponse(AuthorizationRequest request, String sessionId) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("session_id", sessionId));
        response.headers().set(HttpHeaderNames.LOCATION, request.uri);
        return response;
    }

    private String createNewSession(String login) {
        return database.createNewSession(login);
    }

    private boolean areCredentialsValid(Credentials credentials) {
        return database.areCredentialsValid(credentials);
    }

    private boolean isSessionIdValid(String sessionId) {
        return database.isSessionIdValid(sessionId);
    }
}
