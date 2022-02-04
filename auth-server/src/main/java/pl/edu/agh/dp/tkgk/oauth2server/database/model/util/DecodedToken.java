package pl.edu.agh.dp.tkgk.oauth2server.database.model.util;



import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONPropertyIgnore;
import org.json.JSONPropertyName;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DecodedToken {
    private final String jwtId;
    private final long issuedAt;
    private final long expiresAt;
    private final List<String> scopeList;
    private final String tokenType;
    private final boolean isAccessToken;
    private final String clientId;
    private final String authCode;

    public DecodedToken(Token token)
    {
        this.jwtId = token.getJwtId();
        this.clientId = token.getClientId();
        this.authCode = token.getAuthCode();

        DecodedJWT decodedToken = TokenUtil.decodeToken(token.getToken());

        this.issuedAt = decodedToken.getIssuedAt().toInstant().getEpochSecond();
        this.expiresAt = decodedToken.getExpiresAt().toInstant().getEpochSecond();
        this.scopeList = decodedToken.getClaim(Claims.SCOPE).asList(String.class);
        this.tokenType = decodedToken.getClaim(Claims.TOKEN_TYPE).asString();
        this.isAccessToken = decodedToken.getClaim(Claims.IS_ACCESS_TOKEN).asBoolean();
    }

    public static class Claims {

        public static final String SCOPE = "scope";

        public static final String TOKEN_TYPE = "token_type";

        public static final String IS_ACCESS_TOKEN = "is_access_token";

        public static final String AUTH_CODE = "auth_code";

    }

    @JSONPropertyName(value = "scope")
    public String getScopeItems() {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = scopeList.iterator();
        while (iterator.hasNext()) {
            result.append(iterator.next());
            if (iterator.hasNext()) result.append(" ");
        }
        return result.toString();
    }

    @JSONPropertyIgnore
    public String getJwtId() {
        return jwtId;
    }

    @JSONPropertyName("iat")
    public long getIssuedAt() {
        return issuedAt;
    }

    @JSONPropertyName("exp")
    public long getExpiresAt() {
        return expiresAt;
    }

    @JSONPropertyIgnore
    public List<String> getScopeList() {
        return scopeList;
    }

    @JSONPropertyName(value = "token_type")
    public String getTokenType() {
        return tokenType;
    }

    @JSONPropertyName(value = "is_access_token")
    public boolean isAccessToken() {
        return isAccessToken;
    }

    @JSONPropertyName(value = "client_id")
    public String getClientId() {
        return clientId;
    }

    public String getAuthCode() {
        return authCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecodedToken that = (DecodedToken) o;
        return issuedAt == that.issuedAt && expiresAt == that.expiresAt && isAccessToken == that.isAccessToken
                && Objects.equals(jwtId, that.jwtId) && Objects.equals(scopeList, that.scopeList)
                && Objects.equals(tokenType, that.tokenType) && Objects.equals(clientId, that.clientId)
                && Objects.equals(authCode, that.authCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jwtId, issuedAt, expiresAt, scopeList, tokenType, isAccessToken, clientId, authCode);
    }
}
