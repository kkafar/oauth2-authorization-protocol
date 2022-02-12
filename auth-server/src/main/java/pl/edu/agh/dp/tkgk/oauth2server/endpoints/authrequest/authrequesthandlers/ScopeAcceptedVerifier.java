package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.authrequesthandlers;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.AuthorizationServerUtil;

import java.io.FileNotFoundException;

public class ScopeAcceptedVerifier extends BaseHandler<AuthorizationRequest, AuthorizationRequest> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(AuthorizationRequest request) {
        if(!request.isScopeAccepted){
            try {
                return buildAcceptScopePage(request.uri, request.scope.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return director.constructHtmlServerErrorResponse(responseBuilder,
                        "Couldn't find file html/accept_scope_page.html");
            }
        }

        return next.handle(request);
    }

    private FullHttpResponse buildAcceptScopePage(String uri, String scope) throws FileNotFoundException {
        String acceptScopePage = AuthorizationServerUtil.loadTextResource("html/accept_scope_page.html");
        acceptScopePage = acceptScopePage.replace("$SUBMIT_URL", uri);
        acceptScopePage = acceptScopePage.replace("$SCOPE", scope);
        return director.constructHtmlResponse(responseBuilder, acceptScopePage, HttpResponseStatus.OK);
    }
}
