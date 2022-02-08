package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

public class HttpHeadersValidator extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    private static final AsciiString ALLOWED_CONTENT_TYPE = HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED;



    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(!isContentTypeValid(request)){
            return buildInvalidContentTypeResponse();
        }
        FullHttpResponse nextHandlerResponse = next.handle(request);
        return nextHandlerResponse;
    }

    private boolean isContentTypeValid(FullHttpRequest request){
        return request.headers().contains(HttpHeaderNames.CONTENT_TYPE, ALLOWED_CONTENT_TYPE, true);
    }

    private FullHttpResponse buildInvalidContentTypeResponse(){
        String pageContent = AuthorizationServerUtil.buildSimpleHtml("Invalid content type",
                "Allowed content type: " + ALLOWED_CONTENT_TYPE);
        FullHttpResponse invalidContentResponse =
                AuthorizationServerUtil.buildSimpleHttpResponse(HttpResponseStatus.OK, pageContent);
        return invalidContentResponse;
    }
}
