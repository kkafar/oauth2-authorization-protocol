package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;

public class StateValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        if(!request.urlParameters.containsKey("state")){
            String message = "state not preset";
            return director.constructHtmlResponse(responseBuilder, director.buildSimpleHtml("State error", message),
                    HttpResponseStatus.OK);
        }

        String state = request.urlParameters.get("state").get(0);
        if(!isStateValid(state)){
            String message = "state format is invalid";
            return director.constructHtmlResponse(responseBuilder, director.buildSimpleHtml("State error", message),
                    HttpResponseStatus.OK);
        }

        return next.handle(request);
    }

    private boolean isStateValid(String state){
        return state.matches("[ -~]+");
    }

}
