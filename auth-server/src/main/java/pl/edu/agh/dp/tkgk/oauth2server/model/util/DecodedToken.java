package pl.edu.agh.dp.tkgk.oauth2server.model.util;


import com.auth0.jwt.interfaces.DecodedJWT;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import org.json.JSONPropertyIgnore;
import org.json.JSONPropertyName;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;

import java.time.Instant;
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
        this.scopeList = decodedToken.getClaim(CustomClaims.SCOPE).asList(String.class);
        this.tokenType = decodedToken.getClaim(CustomClaims.TOKEN_TYPE).asString();
        this.isAccessToken = decodedToken.getClaim(CustomClaims.IS_ACCESS_TOKEN).asBoolean();
    }

    public static class CustomClaims {

        public static final String SCOPE = "scope";

        public static final String TOKEN_TYPE = "token_type";

        public static final String IS_ACCESS_TOKEN = "is_access_token";

        public static final String AUTH_CODE = "auth_code";

    }

    @JSONPropertyName(value = "scope")
    public String getScopeItems() {
        return TokenUtil.getScopeAsString(scopeList);
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

    @JSONPropertyIgnore
    public String getTokenType() {
        return tokenType;
    }

    @JSONPropertyIgnore
    public boolean isAccessToken() {
        return isAccessToken;
    }

    @JSONPropertyName(value = "client_id")
    public String getClientId() {
        return clientId;
    }

    @JSONPropertyIgnore
    public String getAuthCode() {
        return authCode;
    }

    @JSONPropertyIgnore
    public boolean isActive() { return expiresAt > Instant.now().getEpochSecond(); }

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
