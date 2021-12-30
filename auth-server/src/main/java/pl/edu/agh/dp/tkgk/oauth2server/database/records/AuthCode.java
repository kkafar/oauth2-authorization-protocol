package pl.edu.agh.dp.tkgk.oauth2server.database.records;

public final class AuthCode {
    public final String code;
    public final String redirectUri;
    public final String codeChallenge;
    public final String codeChallengeMethod;
    public final long expireTime;

    public AuthCode(String code, String redirectUri, String codeChallenge, String codeChallengeMethod, long expireTime) {
        this.code = code;
        this.redirectUri = redirectUri;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.expireTime = expireTime;
    }
}
