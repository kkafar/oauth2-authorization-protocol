package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class AuthEndpointUtil {
    private static final String AUTH_ENDPOINT_ERRORS_URL = "https://student.agh.edu.pl/~karczyk/dp/auth_endpoint_errors";

    @Contract(pure = true)
    public static @NotNull FullHttpResponse buildAuthErrorResponse(String error, String fragmentIdentifierOfError, String redirectUrl){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        String errorUri = URLEncoder.encode(AUTH_ENDPOINT_ERRORS_URL + "#" + fragmentIdentifierOfError, StandardCharsets.UTF_8);
        String fullRedirectUrl = redirectUrl + "?error=" + error + "&error_uri=" + errorUri;
        response.headers().set(HttpHeaderNames.LOCATION, fullRedirectUrl);
        return response;
    }

    @Contract(pure = true)
    public static @NotNull FullHttpResponse buildAuthErrorResponse(String error, String fragmentIdentifierOfError, String redirectUrl, String state){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        String errorUri = URLEncoder.encode(AUTH_ENDPOINT_ERRORS_URL + "#" + fragmentIdentifierOfError, StandardCharsets.UTF_8);
        String fullRedirectUrl = redirectUrl + "?error=" + error + "&error_uri=" + errorUri + "&state=" + state;
        response.headers().set(HttpHeaderNames.LOCATION, fullRedirectUrl);
        return response;
    }

    @Contract(pure = true)
    public static @NotNull FullHttpResponse buildRedirectResponseToErrorPage(String fragmentIdentifierOfError){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, AUTH_ENDPOINT_ERRORS_URL + "#" + fragmentIdentifierOfError);
        return response;
    }
}
