package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.fullrequesthandlers;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthEndpointUtil;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthErrorFragments;

public class RepeatingGetParametersChecker extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(areThereRepeatingParameters(request.uri())){
            return buildRepeatingParametersResponse();
        }

        return next.handle(request);
    }

    private FullHttpResponse buildRepeatingParametersResponse(){
        return AuthEndpointUtil.buildRedirectResponseToErrorPage(AuthErrorFragments.REPEATING_GET_PARAMETERS_ERROR_FRAGMENT);
    }

    private boolean areThereRepeatingParameters(String uri){
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        int hookIndex = uri.lastIndexOf('?');
        if(hookIndex == -1) return false;
        String params = uri.substring(hookIndex);
        return queryStringDecoder.parameters().size() != params.split("&").length;
    }
}
