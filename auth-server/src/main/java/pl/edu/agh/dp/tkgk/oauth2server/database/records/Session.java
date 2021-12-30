package pl.edu.agh.dp.tkgk.oauth2server.database.records;

public final class Session {
    public final String sessionId;
    public final String login;
    public final long expireTimeInSeconds;

    public Session(String sessionId, String login, long expireTimeInSeconds) {
        this.sessionId = sessionId;
        this.login = login;
        this.expireTimeInSeconds = expireTimeInSeconds;
    }
}
