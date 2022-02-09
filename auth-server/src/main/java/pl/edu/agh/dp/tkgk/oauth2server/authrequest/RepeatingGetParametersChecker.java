package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.util.HtmlWithTitleAndContent;

public class RepeatingGetParametersChecker extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();


    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(areThereRepeatingParameters(request.uri())){
            return buildRepeatingParametersResponse();
        }

        return next.handle(request);
    }

    private FullHttpResponse buildRepeatingParametersResponse(){
        String message = "One or more parameters are repeated";

        return director.constructHtmlResponse(responseBuilder,
                new HtmlWithTitleAndContent("Error", message).getHtml(), HttpResponseStatus.OK);
    }

    private boolean areThereRepeatingParameters(String uri){
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        return queryStringDecoder.parameters().size() != uri.split("&").length;
    }
}
