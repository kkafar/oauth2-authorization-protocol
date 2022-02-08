package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;

import java.nio.charset.StandardCharsets;

public class JsonResponseBuilder extends ResponseBuilder<JSONObject> {

    public JsonResponseBuilder() {
        this.reset();

        headers.put(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    }

    @Override
    public void setMessage(JSONObject message) {
        content = Unpooled.copiedBuffer(message.toString(), StandardCharsets.UTF_8);
        headers.put(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
    }

}
