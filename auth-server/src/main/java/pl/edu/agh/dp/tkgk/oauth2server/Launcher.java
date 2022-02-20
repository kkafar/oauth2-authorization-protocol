package pl.edu.agh.dp.tkgk.oauth2server;

import pl.edu.agh.dp.tkgk.oauth2server.server.AuthorizationServer;

public class Launcher {
    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        AuthorizationServer authorizationServer = new AuthorizationServer();
        authorizationServer.run(HOST, PORT);
    }
}
