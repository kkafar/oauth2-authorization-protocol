package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.converters;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.HttpRequestWithParameters;

public class FullHttpRequest2HttpRequestWithParameters extends BaseHandler<FullHttpRequest, HttpRequestWithParameters> {
    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HttpRequestWithParameters httpRequestWithParameters = new HttpRequestWithParameters(request);
        return next.handle(httpRequestWithParameters);
    }
}
