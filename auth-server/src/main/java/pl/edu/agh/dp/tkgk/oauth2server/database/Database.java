package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import model.AuthCode;
import model.Client;
import model.Token;
import model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;

import java.util.List;
import java.util.Optional;

public interface Database {

    /**
     * If given token is an access token -> revokes token and associated refresh tokens with the same authorization code
     * If given token is a refresh token -> revokes token and associated access tokens with the same authorization code
     * If given tokenHint is invalid (e.g. indicates that token is refresh token when it's an access token) ->
     *                  checks what type of token is given and handles the revocation afterwards
     * @param decodedToken - token to be revoked
     * @param tokenHint - token hint indicating if given token is an access or refresh token, or if the token hint was
     *                  not given at all
     */
    void tokenRevocation(DecodedJWT decodedToken, TokenHint tokenHint);

    /**
     * @param tokenId - tokenId used to fetch the requested token from the database
     * @param tokenHint - token hint indicating if given token is an access or refresh token, or if the token hint was
     *                  not given at all
     * @return Optional of the fetched token, Optional.empty if the requested token was not available
     */
    Optional<Token> fetchToken(String tokenId, TokenHint tokenHint);

    Optional<Client> fetchClient(String clientId);

    Optional<AuthCode> fetchAuthorizationCode(String authorizationCode);

    /**
     * Generates new token with given parameters and unique token id, adds token to database and then updates
     * <code>authorizationCode</code> as used
     * @param expiresIn - validity time in days
     * @param authorizationCode - authorization code to be marked as used
     * @param isAccessToken - indicates if token is access or refresh token
     * @param tokenType - e.g. "Bearer"
     * @return generated token
     */
    Token getNewTokenFromAuthCode(int expiresIn, AuthCode authorizationCode, boolean isAccessToken, String tokenType);

    /**
     * Generates new token with given parameters and unique token id, adds token to database
     * @param expiresIn - validity time in days
     * @param authorizationCode - authorization code to be marked as used
     * @param isAccessToken - indicates if token is access or refresh token
     * @param tokenType - e.g. "Bearer"
     * @return generated token
     */
    Token getNewToken(int expiresIn, List<String> scope, String authorizationCode, boolean isAccessToken, String tokenType, String clientId);

    boolean isSessionIdValid(String sessionId);

    boolean areCredentialsValid(Credentials credentials);

    String createNewSession(String login);

    String generateCode(AuthorizationRequest request);
}
