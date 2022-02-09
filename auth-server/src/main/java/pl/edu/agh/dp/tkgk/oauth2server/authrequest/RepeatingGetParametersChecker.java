package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

public class RepeatingGetParametersChecker extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    public static final String REPEATING_GET_PARAMETERS_ERROR_FRAGMENT = "repeating_get_parameters_error";

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(areThereRepeatingParameters(request.uri())){
            return buildRepeatingParametersResponse();
        }

        return next.handle(request);
    }

    private FullHttpResponse buildRepeatingParametersResponse(){
        return AuthEndpointUtil.buildRedirectResponseToErrorPage(REPEATING_GET_PARAMETERS_ERROR_FRAGMENT);
    }

    private boolean areThereRepeatingParameters(String uri){
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        int hookIndex = uri.lastIndexOf('?');
        if(hookIndex == -1) return false;
        String params = uri.substring(hookIndex);
        return queryStringDecoder.parameters().size() != params.split("&").length;
    }
}
