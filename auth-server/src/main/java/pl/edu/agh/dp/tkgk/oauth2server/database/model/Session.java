package pl.edu.agh.dp.tkgk.oauth2server.database.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public record Session(@BsonId() String sessionId, String login,
                      @BsonProperty(value = "expire_time_in_seconds") long expireTimeInSeconds) {

    @BsonCreator
    public Session(@BsonProperty("sessionId") String sessionId, @BsonProperty("login") String login,
                   @BsonProperty("expireTimeInSeconds") long expireTimeInSeconds) {
        this.sessionId = sessionId;
        this.login = login;
        this.expireTimeInSeconds = expireTimeInSeconds;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getLogin() {
        return login;
    }

    public long getExpireTimeInSeconds() {
        return expireTimeInSeconds;
    }
}
