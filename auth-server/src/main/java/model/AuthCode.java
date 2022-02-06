package model;

import model.util.CodeChallengeMethod;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class AuthCode {
    @BsonId
    private final String code;
    @BsonProperty(value = "code_challenge")
    private final String codeChallenge;
    @BsonProperty(value = "code_challenge_method")
    private final CodeChallengeMethod codeChallengeMethod;
    @BsonProperty(value = "expire_time")
    private final long expireTime;
    @BsonProperty(value = "client_id")
    private final String clientId;
    private boolean used;
    private List<String> scope;

    @BsonCreator
    public AuthCode(@BsonProperty("code") String code,
                    @BsonProperty("codeChallenge") String codeChallenge,
                    @BsonProperty("codeChallengeMethod") CodeChallengeMethod codeChallengeMethod,
                    @BsonProperty("expireTime") long expireTime,
                    @BsonProperty("clientId") String clientId,
                    @BsonProperty("used") boolean used,
                    @BsonProperty("scope") List<String> scope)
    {
        this.code = code;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.expireTime = expireTime;
        this.clientId = clientId;
        this.used = used;
        this.scope = scope;
    }

    public static class JsonFields {

        public static final String ID = "_id";

        public static final String CODE_CHALLENGE = "code_challenge";

        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";

        public static final String EXPIRE_TIME = "expire_time";

        public static final String CLIENT_ID = "client_id";

        public static final String USED = "used";

        public static final String SCOPE = "scope";

    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getCode() {
        return code;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public CodeChallengeMethod getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isUsed() {
        return used;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    @BsonIgnore
    public String getScopeItems() {
        return TokenUtil.getScopeAsString(scope);
    }

    @BsonIgnore
    public boolean isActive() {
        return expireTime > Instant.now().getEpochSecond();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthCode authCode = (AuthCode) o;
        return expireTime == authCode.expireTime && used == authCode.used && Objects.equals(code, authCode.code)
                && Objects.equals(codeChallenge, authCode.codeChallenge)
                && Objects.equals(codeChallengeMethod, authCode.codeChallengeMethod)
                && Objects.equals(clientId, authCode.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, codeChallenge, codeChallengeMethod, expireTime, clientId, used);
    }
}
