package pl.edu.agh.dp.oauth2server.requestvalidation;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;

public class RequestValidator {
    public static boolean isRequestCorrect(FullHttpRequest request) {
        //request method
        if (!request.method().equals(HttpMethod.GET)) {
            return false;
        }
        //content type
        else if (!request.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, true)) {
            return false;
        }
        //token included
        else if (!request.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
            return false;
        }
        //possibly other stuff
        else return true;
    }
}
