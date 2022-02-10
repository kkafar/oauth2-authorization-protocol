package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.UrlEncodedResponseBuilder;

import java.util.List;
import java.util.Map;

public class ResponseTypeVerifier extends BaseHandler<HttpRequestWithParameters,HttpRequestWithParameters> {
    public final static String UNKNOWN_RESPONSE_TYPE_FRAGMENT = "unknown_response_type";
    public final static String RESPONSE_TYPE_IS_MISSING_FRAGMENT = "response_type_is_missing";

    public static final String INVALID_REQUEST = "invalid_request";

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get("redirect_uri").get(0);
        String state = parameters.get("state").get(0);

        if(!parameters.containsKey("response_type")){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, RESPONSE_TYPE_IS_MISSING_FRAGMENT, redirect_uri, state);
        }
        if(!parameters.get("response_type").get(0).equals("code")){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, UNKNOWN_RESPONSE_TYPE_FRAGMENT, redirect_uri, state);
        }

        return next.handle(request);
    }
}
