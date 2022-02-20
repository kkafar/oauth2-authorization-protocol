package pl.edu.agh.dp.tkgk.oauth2server.endpoints.favicon;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.AuthorizationServerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class FavIconResponder extends BaseHandler<FullHttpRequest, Void> {

    private byte[] img;

    public FavIconResponder(){
        try {
            img = AuthorizationServerUtil.loadBytesFromFile("favicon.ico");
        } catch (IOException e) {
            img = new byte[0];
            e.printStackTrace();
        }
    }

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(img));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, img.length);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "image/x-icon");
        return response;
    }
}
