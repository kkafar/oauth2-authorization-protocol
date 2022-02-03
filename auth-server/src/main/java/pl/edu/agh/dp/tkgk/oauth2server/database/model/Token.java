package pl.edu.agh.dp.tkgk.oauth2server.database.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.DecodedToken;

public class Token {
    @BsonId
    private final String jwtId;
    private final String token;
    @BsonProperty(value = "auth_code")
    private final String authCode;
    @BsonProperty(value = "client_id")
    private final String clientId;

    @BsonCreator
    public Token(@BsonProperty("jwtId") String jwtId,
                 @BsonProperty("token") String token,
                 @BsonProperty("authCode") String authCode,
                 @BsonProperty("clientId") String clientId)
    {
        this.jwtId = jwtId;
        this.token = token;
        this.authCode = authCode;
        this.clientId = clientId;
    }

    public String getJwtId() {
        return jwtId;
    }

    public String getToken() {
        return token;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getClientId() {
        return clientId;
    }

    @BsonIgnore
    public DecodedToken getDecodedToken() {
        return new DecodedToken(this);
    }
}
