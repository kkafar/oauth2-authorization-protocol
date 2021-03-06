package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wraps FullHttpRequest and extracts url parameters, body parameters and cookies
 */

public class HttpRequestWithParameters {

    public HttpRequestWithParameters(FullHttpRequest fullHttpRequest, Map<String, List<String>> urlParameters, Map<String, List<String>> bodyParameters, Map<String, String> cookies) {
        this.fullHttpRequest = fullHttpRequest;
        this.urlParameters = urlParameters;
        this.bodyParameters = bodyParameters;
        this.cookies = cookies;
    }

    public final FullHttpRequest fullHttpRequest;
    public final Map<String, List<String>> urlParameters;
    public final Map<String, List<String>> bodyParameters;
    public final Map<String, String> cookies;

    public HttpRequestWithParameters(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
        this.urlParameters = getParameters(fullHttpRequest.uri(),true);
        this.bodyParameters = getParameters(fullHttpRequest.content().toString(StandardCharsets.UTF_8), false);
        this.cookies = getCookies(fullHttpRequest);

    }



    private Map<String, List<String>> getParameters(String parametersString, boolean hasPath) {
        final Map<String, List<String>> parameters;
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(parametersString, hasPath);
        parameters = queryStringDecoder.parameters();
        return parameters;
    }

    private Map<String, String> getCookies(FullHttpRequest fullHttpRequest) {
        final Map<String, String> cookies;
        String cookieString = fullHttpRequest.headers().get(HttpHeaderNames.COOKIE);
        if(cookieString == null) return Map.of();
        Set<Cookie> cookiesSet = ServerCookieDecoder.STRICT.decode(cookieString);
        cookies = new HashMap<>();
        for(Cookie cookie: cookiesSet){
            cookies.put(cookie.name(), cookie.value());
        }
        return cookies;
    }

}
