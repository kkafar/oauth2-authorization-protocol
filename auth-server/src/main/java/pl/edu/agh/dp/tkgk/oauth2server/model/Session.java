package pl.edu.agh.dp.tkgk.oauth2server.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Objects;

public record Session(@BsonId() String sessionId, String login,
                      @BsonProperty(value = "expire_time_in_seconds") long expireTimeInSeconds) {

    @BsonCreator
    public Session(@BsonProperty("sessionId") String sessionId,
                   @BsonProperty("login") String login,
                   @BsonProperty("expireTimeInSeconds") long expireTimeInSeconds)
    {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return expireTimeInSeconds == session.expireTimeInSeconds && Objects.equals(sessionId, session.sessionId)
                && Objects.equals(login, session.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, login, expireTimeInSeconds);
    }
}
