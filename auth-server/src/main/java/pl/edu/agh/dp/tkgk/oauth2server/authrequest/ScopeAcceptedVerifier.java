package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.io.FileNotFoundException;

public class ScopeAcceptedVerifier extends BaseHandler<AuthorizationRequest, AuthorizationRequest> {
    @Override
    public FullHttpResponse handle(AuthorizationRequest request) {
        if(!request.isScopeAccepted){
            try {
                return buildAcceptScopePage(request.uri, request.scope.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return AuthorizationServerUtil.buildServerErrorResponse("Couldn't find file html/accept_scope_page.html");
            }
        }

        return next.handle(request);
    }

    private FullHttpResponse buildAcceptScopePage(String uri, String scope) throws FileNotFoundException {
        String acceptScopePage = AuthorizationServerUtil.loadTextResource("html/accept_scope_page.html");
        acceptScopePage = acceptScopePage.replace("$SUBMIT_URL", uri);
        acceptScopePage = acceptScopePage.replace("$SCOPE", scope);
        return AuthorizationServerUtil.buildSimpleHttpResponse(HttpResponseStatus.OK, acceptScopePage);
    }
}
