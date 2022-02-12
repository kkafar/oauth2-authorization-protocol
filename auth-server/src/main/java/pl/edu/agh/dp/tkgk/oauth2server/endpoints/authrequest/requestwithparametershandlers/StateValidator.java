package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthEndpointUtil;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthErrorFragments;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.HttpRequestWithParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;

public class StateValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        if(!request.urlParameters.containsKey(HttpParameters.STATE)){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.STATE_IS_MISSING_FRAGMENT);
        }

        String state = request.urlParameters.get(HttpParameters.STATE).get(0);
        if(!isStateValid(state)){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.STATE_IS_MALFORMED_FRAGMENT);
        }

        return next.handle(request);
    }

    private boolean isStateValid(String state){
        return state.matches("[ -~]+");
    }

}
