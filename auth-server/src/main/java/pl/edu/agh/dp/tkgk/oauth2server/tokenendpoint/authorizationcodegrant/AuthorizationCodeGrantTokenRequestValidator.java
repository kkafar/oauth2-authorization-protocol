package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

public class AuthorizationCodeGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, Object> {
    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }
}
