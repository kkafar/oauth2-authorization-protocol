package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;

public class HttpHeadersValidator extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    private static final AsciiString ALLOWED_CONTENT_TYPE = HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED;

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(!isContentTypeValid(request)){
            return buildInvalidContentTypeResponse();
        }
        return next.handle(request);
    }

    private boolean isContentTypeValid(FullHttpRequest request){
        return request.headers().contains(HttpHeaderNames.CONTENT_TYPE, ALLOWED_CONTENT_TYPE, true);
    }

    private FullHttpResponse buildInvalidContentTypeResponse(){
        String pageContent = director.buildSimpleHtml("Invalid content type",
                "Allowed content type: " + ALLOWED_CONTENT_TYPE);
        return director.constructHtmlResponse(responseBuilder, pageContent, HttpResponseStatus.OK);
    }
}
