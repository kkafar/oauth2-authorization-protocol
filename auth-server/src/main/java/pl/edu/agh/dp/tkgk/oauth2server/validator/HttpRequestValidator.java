package pl.edu.agh.dp.tkgk.oauth2server.validator;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.util.Objects;

public record HttpRequestValidator(FullHttpRequest request, HttpPostRequestDecoder decoder) {

    public boolean validRequestMethod(HttpMethod validHttpMethod) {
        HttpMethod method = request.method();
        return method.equals(validHttpMethod);
    }

    public boolean validContentType(String validContentType) {
        HttpHeaders headers = request.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return Objects.equals(contentType, validContentType);
    }

    public boolean hasTokenInRequestBody() {
        InterfaceHttpData tokenData = decoder.getBodyHttpData("token");
        return tokenData != null;
    }

    public boolean hasAuthorizationHeader() {
        HttpHeaders headers = request.headers();
        String authorizationHeader = headers.get(HttpHeaderNames.AUTHORIZATION);
        return authorizationHeader != null;
    }
}
