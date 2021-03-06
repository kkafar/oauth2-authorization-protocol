package pl.edu.agh.dp.tkgk.oauth2server.endpoints.pong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;

import java.nio.charset.StandardCharsets;

public class PingHandler extends BaseHandler<FullHttpRequest, FullHttpRequest> {
    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        String resString = "PONG!!!";

        ByteBuf content = Unpooled.copiedBuffer(resString, StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
