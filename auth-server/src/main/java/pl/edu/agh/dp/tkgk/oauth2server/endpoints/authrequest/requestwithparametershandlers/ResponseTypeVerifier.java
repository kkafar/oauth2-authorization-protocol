package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthEndpointUtil;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthErrorFragments;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.HttpRequestWithParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;

import java.util.List;
import java.util.Map;

public class ResponseTypeVerifier extends BaseHandler<HttpRequestWithParameters,HttpRequestWithParameters> {

    public static final String INVALID_REQUEST = "invalid_request";

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get(HttpParameters.REDIRECT_URI).get(0);
        String state = parameters.get(HttpParameters.STATE).get(0);

        if(!parameters.containsKey(HttpParameters.RESPONSE_TYPE)){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, AuthErrorFragments.RESPONSE_TYPE_IS_MISSING_FRAGMENT, redirect_uri, state);
        }
        if(!parameters.get(HttpParameters.RESPONSE_TYPE).get(0).equals(HttpParameters.CODE)){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, AuthErrorFragments.UNKNOWN_RESPONSE_TYPE_FRAGMENT, redirect_uri, state);
        }

        return next.handle(request);
    }
}
