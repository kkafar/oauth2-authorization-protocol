package pl.edu.agh.dp.tkgk.oauth2server.model.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

public class TokenUtil {
    private final static int TOKEN_ID_LENGTH = 32;
    private final static String JWT_ID_CLAIM_NAME = "jti";
    public static final String SECRET = "ultra-secret-key-that-is-at-least-32-bits-long-for-hs256-algorithm-top-secret";

    public static String generateTokenId() {
        String alphaNumericCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                        + "0123456789"
                                        + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(TOKEN_ID_LENGTH);

        for (int i = 0; i < TOKEN_ID_LENGTH; i++) {
            int index = (int) (alphaNumericCharacters.length() * Math.random());

            sb.append(alphaNumericCharacters.charAt(index));
        }

        return sb.toString();
    }

    public static String generateToken(int expiresIn, List<String> scope, String authorizationCode, String userLogin,
                                       boolean isAccessToken, String tokenType, String tokenId) throws JWTCreationException {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        long now = Instant.now().getEpochSecond();

        return JWT.create()
                .withClaim(DecodedToken.CustomClaims.AUTH_CODE, authorizationCode)
                .withClaim(DecodedToken.CustomClaims.TOKEN_TYPE, tokenType)
                .withClaim(DecodedToken.CustomClaims.IS_ACCESS_TOKEN, isAccessToken)
                .withClaim(DecodedToken.CustomClaims.SCOPE, scope)
                .withClaim(DecodedToken.CustomClaims.USER_LOGIN, userLogin)
                .withJWTId(tokenId)
                .withExpiresAt(new Date((now + expiresIn) * 1000))
                .withIssuedAt(new Date(now * 1000))
                .sign(algorithm);
    }

    public static void main(String[] args) {
        System.out.println(TokenUtil.generateToken(10000000, List.of("introspection"), "auth-server",
                "auth-server", true, "Bearer", TokenUtil.generateTokenId()));
    }

    public static DecodedJWT decodeToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        JWTVerifier verifier = JWT.require(algorithm)
                .withClaimPresence(DecodedToken.CustomClaims.AUTH_CODE)
                .withClaimPresence(DecodedToken.CustomClaims.TOKEN_TYPE)
                .withClaimPresence(DecodedToken.CustomClaims.IS_ACCESS_TOKEN)
                .withClaimPresence(DecodedToken.CustomClaims.SCOPE)
                .withClaimPresence(DecodedToken.CustomClaims.USER_LOGIN)
                .withClaimPresence(JWT_ID_CLAIM_NAME)
                .build();

        return verifier.verify(token);
    }

    public static String getScopeAsString(List<String> scope) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = scope.iterator();
        while (iterator.hasNext()) {
            result.append(iterator.next());
            if (iterator.hasNext()) result.append(" ");
        }
        return result.toString();
    }

}
