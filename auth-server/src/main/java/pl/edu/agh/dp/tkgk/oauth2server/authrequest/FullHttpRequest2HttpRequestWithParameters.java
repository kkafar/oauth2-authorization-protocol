package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

public class FullHttpRequest2HttpRequestWithParameters extends BaseHandler<FullHttpRequest, HttpRequestWithParameters> {
    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HttpRequestWithParameters httpRequestWithParameters = new HttpRequestWithParameters(request);
        FullHttpResponse nextHandlerResponse = next.handle(httpRequestWithParameters);
        return nextHandlerResponse;
    }
}
