package pl.edu.agh.dp.tkgk.oauth2server.endpoints.pagenotfound;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.AuthorizationServerUtil;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

public class PageNotFoundHandler extends BaseHandler<FullHttpRequest, FullHttpRequest> {
    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        String page = "Page not found";
        try {
            page = AuthorizationServerUtil.loadTextResource(AuthorizationServerUtil.HTML_PAGE_404);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteBuf content = Unpooled.copiedBuffer(page, StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
