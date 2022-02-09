package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;

import java.util.Set;

/**
Checks if needed http headers are present and valid
 */
public class HttpHeadersValidator extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    private static final Set<AsciiString> ALLOWED_CONTENT_TYPES = Set.of(
            HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpHeaderValues.TEXT_HTML
    );
    private static final String INVALID_CONTENT_TYPE = "invalid_content_type";

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
        if(!request.headers().contains(HttpHeaderNames.CONTENT_TYPE)) return false;
        final String contentType = request.headers()
                .get(HttpHeaderNames.CONTENT_TYPE);
        return ALLOWED_CONTENT_TYPES.stream().anyMatch(s -> s.contentEqualsIgnoreCase(contentType));
    }

    private FullHttpResponse buildInvalidContentTypeResponse(){
        String pageContent = director.buildSimpleHtml("Invalid content type",
                "Allowed content type: " + ALLOWED_CONTENT_TYPES);
        return director.constructHtmlResponse(responseBuilder, pageContent, HttpResponseStatus.OK);
    }
}
