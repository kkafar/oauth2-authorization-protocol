package pl.edu.agh.dp.tkgk.oauth2server.database.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;

public class Client {
    @BsonId
    private final String clientId;
    @BsonProperty(value = "redirect_uri")
    private String redirectUri;
    private final List<String> scope;

    @BsonCreator
    public Client(@BsonProperty("clientId") String clientId, @BsonProperty("redirectUri") String redirectUri, List<String> scope) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    public List<String> getScope() {
        return scope;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
