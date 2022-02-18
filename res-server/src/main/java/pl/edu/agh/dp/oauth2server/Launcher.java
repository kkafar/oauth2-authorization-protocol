package pl.edu.agh.dp.oauth2server;

public class Launcher {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Resource server is running.");
        System.out.println("HOST : " + HOST);
        System.out.println("PORT : " + PORT);

        ResourceServer resourceServer = new ResourceServer(PORT);
        resourceServer.run();
    }
}
