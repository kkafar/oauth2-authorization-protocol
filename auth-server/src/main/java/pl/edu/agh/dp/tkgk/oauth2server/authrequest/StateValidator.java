package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.util.HtmlWithTitleAndContent;

public class StateValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {

    public static final String STATE_IS_MISSING_FRAGMENT = "state_is_missing";
    public static final String STATE_IS_MALFORMED_FRAGMENT = "state_is_malformed";

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        if(!request.urlParameters.containsKey("state")){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(STATE_IS_MISSING_FRAGMENT);
        }

        String state = request.urlParameters.get("state").get(0);
        if(!isStateValid(state)){
            return AuthEndpointUtil.buildRedirectResponseToErrorPage(STATE_IS_MALFORMED_FRAGMENT);
        }

        return next.handle(request);
    }

    private boolean isStateValid(String state){
        return state.matches("[ -~]+");
    }

}
