package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;

import java.nio.charset.StandardCharsets;

public class ResponseWithCustomHtmlBuilder extends ResponseBuilder<String> {

    public ResponseWithCustomHtmlBuilder() {
        this.reset();

        headers.put(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML);
    }

    @Override
    public void setMessage(String message) {
        content = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
        headers.put(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
    }

}
