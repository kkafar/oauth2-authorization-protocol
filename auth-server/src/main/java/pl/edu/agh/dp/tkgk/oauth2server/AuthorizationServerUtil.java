package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.json.JSONObject;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.util.Objects;

public class AuthorizationServerUtil {

    public static final String SECRET = "ultra-secret-key-that-is-at-least-32-bits-long-for-hs256-algorithm-top-secret";
    public static final String HTML_PAGE_404 = "html/page_not_found_404.html";

    private static final String INVALID_REQUEST = "invalid_request";
    private static final String ERROR = "error";

    private static SslContext serverSSLContext = null;

    public static SslContext getSSLContext() {
        if (serverSSLContext != null) return serverSSLContext;

        try {
            initSSLContext();
        } catch (CertificateException | SSLException e) {
            e.printStackTrace();
        }

        return serverSSLContext;
    }

    private static void initSSLContext() throws CertificateException, SSLException {
        String certPath = Objects.requireNonNull(AuthorizationServerUtil.class.getResource("cert.pem")).getPath();
        File cert = new File(certPath);

        String keyPath = Objects.requireNonNull(AuthorizationServerUtil.class.getResource("key.pem")).getPath();
        File key = new File(keyPath);

        serverSSLContext = SslContextBuilder.forServer(cert, key)
                .build();
    }

    public static FullHttpResponse badRequestHttpResponse(boolean includeCacheAndPragmaControl) {
        return badRequestHttpResponseWithCustomError(includeCacheAndPragmaControl, INVALID_REQUEST);
    }

    public static FullHttpResponse serverErrorHttpResponse(String errorMsg) {
        JSONObject error = new JSONObject().put(ERROR, errorMsg);
        ByteBuf content = Unpooled.copiedBuffer(error.toString(), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }

    public static FullHttpResponse badRequestHttpResponseWithCustomError(boolean includeCacheAndPragmaControl,
                                                                         String errorMsg) {
        JSONObject error = new JSONObject().put(ERROR, errorMsg);
        ByteBuf content = Unpooled.copiedBuffer(error.toString(), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        if (includeCacheAndPragmaControl) {
            response.headers().set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE);
            response.headers().set(HttpHeaderNames.PRAGMA, HttpHeaderValues.NO_CACHE);
        }
        return response;
    }

    public static String loadTextResource(String resourcePath) throws FileNotFoundException {
        URL resourceURL = AuthorizationServerUtil.class.getResource(resourcePath);
        if (resourceURL == null){
            throw new FileNotFoundException("Couldn't find resource: " + resourcePath);
        }

        String textResource = "";
        try {
            textResource = Files.readString(Path.of(resourceURL.toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return textResource;
    }

    public static String buildSimpleHtml(String title, String content){
        return "<html><body><h1>" +
                title +
                "</h1>" +
                content.replaceAll("\n", "</br>") +
                "</body></html>";
    }


    public static FullHttpResponse buildSimpleHttpResponse(HttpResponseStatus status, String content){
        ByteBuf contentByteBuf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, contentByteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentByteBuf.readableBytes());
        return response;
    }

    public static FullHttpResponse buildServerErrorResponse(String msg){
        String pageContent = buildSimpleHtml("Error 500", msg);
        return buildSimpleHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, pageContent);
    }

    public static FullHttpResponse buildErrorResponse(String error, String errorUri, String redirectUri, String state){
        String url = redirectUri + "?error=" + error + "&error_uri=" + errorUri + "&state=" + state;
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.LOCATION, url);
        return response;
    }
}
