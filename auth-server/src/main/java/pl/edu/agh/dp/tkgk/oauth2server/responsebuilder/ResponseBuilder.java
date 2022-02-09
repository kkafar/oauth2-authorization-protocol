package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;


public abstract class ResponseBuilder<T> {

    protected ByteBuf content = null;
    protected HttpResponseStatus status;
    protected Map<CharSequence, Object> headers;

    public void reset() {
        headers = new HashMap<>();
    }

    public abstract void setMessage(T message);

    public void setHttpResponseStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public void setHeader(CharSequence header, Object value) {
        headers.put(header, value);
    }

    public void includeCacheAndPragmaControlHeaders() {
        setHeader(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE);
        setHeader(HttpHeaderNames.PRAGMA, HttpHeaderValues.NO_CACHE);
    }

    FullHttpResponse getResponse() {
        FullHttpResponse response;

        if (content != null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        }

        headers.forEach((headerName, headerValue) -> response.headers().set(headerName, headerValue));

        this.reset();
        return response;
    }
}
