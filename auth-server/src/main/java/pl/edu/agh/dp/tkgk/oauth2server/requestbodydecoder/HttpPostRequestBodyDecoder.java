package pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder;

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public record HttpPostRequestBodyDecoder(HttpPostRequestDecoder decoder) {

    public TokenHint fetchTokenHint() throws IOException {
        Optional<String> tokenHintData = fetchAttribute("token_type_hint");
        return TokenHint.value(tokenHintData.orElse("no token hint"));
    }

    public Optional<String> fetchToken() throws IOException {
        return fetchAttribute("token");
    }

    public Optional<String> fetchAttribute(String attributeName) throws IOException {
        InterfaceHttpData attributeData = decoder.getBodyHttpData(attributeName);
        return getStringFromData(attributeData);
    }

    public Optional<String> getStringFromData(InterfaceHttpData data) throws IOException {
        Optional<String> dataString = Optional.empty();

        if (data != null) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                Attribute dataAttribute = (Attribute) data;
                dataString = Optional.of(dataAttribute.getString(StandardCharsets.UTF_8));
            }
        }

        return dataString;
    }
}
