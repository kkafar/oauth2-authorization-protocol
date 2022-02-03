package pl.edu.agh.dp.tkgk.oauth2server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class TokenUtil {

    private final static int TOKEN_ID_LENGTH = 32;

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

    public static DecodedJWT decodeToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(AuthorizationServerUtil.SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        return verifier.verify(token);
    }

}
