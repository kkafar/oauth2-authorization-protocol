package pl.edu.agh.dp.tkgk.oauth2server.database.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;
import java.util.Objects;

public class Client {
    @BsonId
    private final String clientId;
    @BsonProperty(value = "redirect_uri")
    private String redirectUri;
    private final List<String> scope;

    @BsonCreator
    public Client(@BsonProperty("clientId") String clientId,
                  @BsonProperty("redirectUri") String redirectUri,
                  @BsonProperty("scope") List<String> scope)
    {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
    }

    public static class JsonFields {

        public static final String ID = "_id";

        public static final String REDIRECT_URI = "redirect_uri";

        public static final String SCOPE = "scope";

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(clientId, client.clientId) && Objects.equals(redirectUri, client.redirectUri)
                && Objects.equals(scope, client.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, redirectUri, scope);
    }
}
