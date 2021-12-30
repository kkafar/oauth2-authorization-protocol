package pl.edu.agh.dp.tkgk.oauth2server.validator;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.AsciiString;

public record HttpRequestValidator(FullHttpRequest request, HttpPostRequestDecoder decoder) {

    public boolean validRequestMethod(HttpMethod validHttpMethod) {
        HttpMethod method = request.method();
        return method.equals(validHttpMethod);
    }

    public boolean validContentType(AsciiString validContentType) {
        HttpHeaders headers = request.headers();
        return headers.contains(HttpHeaderNames.CONTENT_TYPE, validContentType, true);
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
    
    public boolean hasGrantTypeInRequestBody() {
        InterfaceHttpData grantTypeData = decoder.getBodyHttpData("grant_type");
        return grantTypeData != null;
    }
}
