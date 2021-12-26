package pl.edu.agh.dp.tkgk.oauth2server;

public class Launcher {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        AuthorizationServer authorizationServer = new AuthorizationServer();
        authorizationServer.run(HOST, PORT);
    }
}
