package pl.edu.agh.dp.tkgk.oauth2server;

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
import java.util.Objects;

public class AuthorizationServerUtil {

    public static final String HTML_PAGE_404 = "html/page_not_found_404.html";

    private static SslContext serverSSLContext = null;

    public static SslContext createSSLContext() {
        return serverSSLContext;
    }

    public static void initSSLContext() throws CertificateException, SSLException {
        if(serverSSLContext != null) return; // TODO: trow exception

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
}
