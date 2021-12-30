package pl.edu.agh.dp.tkgk.oauth2server.database.records;

import java.util.List;

public class Client {
    public String clientId;
    public String redirectionUri;
    public List<String> scope;

    public Client(String clientId, String redirectionUri, List<String> scope) {
        this.clientId = clientId;
        this.redirectionUri = redirectionUri;
        this.scope = scope;
    }
}
