package pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.nio.charset.StandardCharsets;

public class TokenDataResponseBuilder extends BaseHandler<JSONObject, Object> {

    @Override
    public FullHttpResponse handle(JSONObject tokenDataJSON) {
        return responseWithTokenDataAndStatus200(tokenDataJSON);
    }

    private FullHttpResponse responseWithTokenDataAndStatus200(JSONObject tokenDataJSON) {
        ByteBuf content = Unpooled.copiedBuffer(tokenDataJSON.toString(), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
