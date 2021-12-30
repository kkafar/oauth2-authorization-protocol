package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.util.List;
import java.util.Map;

public class ResponseTypeVerifier extends BaseHandler<HttpRequestWithParameters,HttpRequestWithParameters> {
    private final static String WRONG_RESPONSE_TYPE_URI = "#wrong_response_type";
    private final static String RESPONSE_TYPE_NOT_PRESENT_URI = "#response_type_not_present";

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get("redirect_uri").get(0);
        String state = parameters.get("state").get(0);

        if(!parameters.containsKey("response_type")){
            return AuthorizationServerUtil.buildErrorResponse("invalid_request", RESPONSE_TYPE_NOT_PRESENT_URI, redirect_uri, state);
        }
        if(!parameters.get("response_type").get(0).equals("code")){
            return AuthorizationServerUtil.buildErrorResponse("invalid_request", WRONG_RESPONSE_TYPE_URI, redirect_uri, state);
        }

        return next.handle(request);
    }
}
