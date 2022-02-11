package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.AsciiString;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;

import java.util.Set;

/**
Checks if needed http headers are present and valid
 */
public class HttpHeadersValidator extends BaseHandler<FullHttpRequest, FullHttpRequest> {

    private static final Set<AsciiString> ALLOWED_CONTENT_TYPES = Set.of(
            HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpHeaderValues.TEXT_HTML
    );
    public static final String INVALID_CONTENT_TYPE_FRAGMENT = "invalid_content_type";

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
        return AuthEndpointUtil.buildRedirectResponseToErrorPage(INVALID_CONTENT_TYPE_FRAGMENT);
    }
}
