package pl.edu.agh.dp.oauth2server;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;

public class SSLContextManager {
    private static SslContext resourceServerSslContext = null;
    private static SslContext clientSslContext = null;

    public static SslContext getResourceServerSslContext(){
        if (resourceServerSslContext == null) {
            try {
                createResourceServerSslContext();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
        return resourceServerSslContext;
    }

    public static SslContext getClientSslContext(){
        if (clientSslContext == null) {
            try {
                createClientSslContext();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
        return clientSslContext;
    }

    private static void createResourceServerSslContext() throws SSLException {
        String certPath = Objects.requireNonNull(SSLContextManager.class.getResource("cert.pem")).getPath().replaceAll("%20", " ");
        File cert = new File(certPath);

        String keyPath = Objects.requireNonNull(SSLContextManager.class.getResource("key.pem")).getPath().replaceAll("%20", " ");
        File key = new File(keyPath);

        resourceServerSslContext = SslContextBuilder.forServer(cert, key).build();
    }

    private static void createClientSslContext() throws SSLException {
        clientSslContext =  SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    }
}
