package pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TokenRevocationRequestValidator extends BaseHandler<FullHttpRequest, HttpPostRequestDecoder> {

    private static final String CORRECT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String INVALID_REQUEST = "invalid_request";

    private HttpPostRequestDecoder decoder;

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        decoder = new HttpPostRequestDecoder(request);
        if (!requestValid(request)) {
            return badRequestHttpResponse();
        }

        return next.handle(decoder);
    }

    private boolean requestValid(FullHttpRequest request) {
        return validRequestMethod(request)
                && validContentType(request)
                && hasToken();
    }

    private boolean validRequestMethod(FullHttpRequest request) {
        HttpMethod method = request.method();
        return method.equals(HttpMethod.POST);
    }

    private boolean validContentType(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return Objects.equals(contentType, CORRECT_CONTENT_TYPE);
    }

    private boolean hasToken() {
        InterfaceHttpData tokenData = decoder.getBodyHttpData("token");
        return tokenData != null;
    }

    private FullHttpResponse badRequestHttpResponse() {
        JSONObject json = new JSONObject();
        json.put("error", INVALID_REQUEST);

        ByteBuf content = Unpooled.copiedBuffer(json.toString(), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
