package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.nio.charset.StandardCharsets;

public class TokenResponseBuilder extends BaseHandler<JSONObject, Object> {

    @Override
    public FullHttpResponse handle(JSONObject responseBody) {
        return responseWithTokenAndStatusCode200(responseBody);
    }

    private FullHttpResponse responseWithTokenAndStatusCode200(JSONObject responseBody) {
        ByteBuf content = Unpooled.copiedBuffer(responseBody.toString(), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE);
        response.headers().set(HttpHeaderNames.PRAGMA, HttpHeaderValues.NO_CACHE);
        return response;
    }
}
