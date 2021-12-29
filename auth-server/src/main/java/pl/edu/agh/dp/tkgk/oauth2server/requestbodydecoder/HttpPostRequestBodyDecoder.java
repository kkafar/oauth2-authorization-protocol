package pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder;

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public record HttpPostRequestBodyDecoder(HttpPostRequestDecoder decoder) {

    public Optional<String> fetchTokenHint() {
        InterfaceHttpData tokenHintData = decoder.getBodyHttpData("token_hint_data");
        return getStringFromData(tokenHintData);
    }

    public Optional<String> fetchToken() {
        InterfaceHttpData tokenData = decoder.getBodyHttpData("token");
        return getStringFromData(tokenData);
    }

    public Optional<String> getStringFromData(InterfaceHttpData data) {
        Optional<String> dataString = Optional.empty();

        if (data != null) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute dataAttribute = (Attribute) data;
                try {
                    dataString = Optional.of(dataAttribute.getString(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return dataString;
    }
}
