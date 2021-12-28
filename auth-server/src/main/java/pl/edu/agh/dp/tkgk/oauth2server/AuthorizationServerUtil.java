package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Objects;

public class AuthorizationServerUtil {

    public static final String HTML_PAGE_404 = "html/page_not_found_404.html";

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

    public static String loadTextResource(String resourcePath) throws FileNotFoundException {
        URL resourceURL = AuthorizationServerUtil.class.getResource(resourcePath);
        if (resourceURL == null){
            throw new FileNotFoundException();
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

    public static HashMap<String,String> extractParameters(FullHttpRequest request){
        HashMap<String,String> parameters = new HashMap<>();
        int questionMarkPosition = request.uri().lastIndexOf('?');
        if(questionMarkPosition == -1) return parameters;

        String parametersString = request.uri().substring(questionMarkPosition+1);
        String[] keyValuePairs = parametersString.split("&");
        for(String keyValuePair: keyValuePairs){
            String[] keyThenValue = keyValuePair.split("=");
            parameters.put(keyThenValue[0],keyThenValue[1]);
        }

        return parameters;
    }
}
