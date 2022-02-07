package pl.edu.agh.dp.oauth2server;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;

public class SSLContextManager {
    private static SslContext resourceServerSslContext = null;

    public static SslContext getSslContext(){
        if (resourceServerSslContext == null) {
            try {
                createSslContext();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        }
        return resourceServerSslContext;
    }

    private static void createSslContext() throws SSLException {
        //String certPath = Objects.requireNonNull(SSLContextManager.class.getResource("D:\\Projects\\Design Patterns\\!Project\\oauth2-authorization-protocol\\res-server\\src\\main\\resources\\pl.edu.agh.dp.oauth2server\\cert.pem")).getPath();
        File cert = new File("D:\\Projects\\Design Patterns\\!Project\\oauth2-authorization-protocol\\res-server\\src\\main\\resources\\pl.edu.agh.dp.oauth2server\\cert.pem");

        //String keyPath = Objects.requireNonNull(SSLContextManager.class.getResource("D:\\Projects\\Design Patterns\\!Project\\oauth2-authorization-protocol\\res-server\\src\\main\\resources\\pl.edu.agh.dp.oauth2server\\key.pem")).getPath();
        File key = new File("D:\\Projects\\Design Patterns\\!Project\\oauth2-authorization-protocol\\res-server\\src\\main\\resources\\pl.edu.agh.dp.oauth2server\\key.pem");

        resourceServerSslContext = SslContextBuilder.forServer(cert, key).build();
    }
}
