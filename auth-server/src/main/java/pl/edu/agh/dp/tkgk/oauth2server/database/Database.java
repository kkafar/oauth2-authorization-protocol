package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;

import java.util.List;
import java.util.Optional;

public interface Database {

    /**
     * If given token is an access token -> revokes token and associated refresh tokens with the same authorization code
     * If given token is a refresh token -> revokes token and associated access tokens with the same authorization code
     * If given tokenHint is invalid (e.g. indicates that token is refresh token when it's an access token) ->
     *                  checks what type of token is given and handles the revocation afterwards
     * @param decodedToken token to be revoked
     * @param tokenHint token hint indicating if given token is an access or refresh token, or if the token hint was
     *                  not given at all
     */
    void tokenRevocation(DecodedJWT decodedToken, TokenHint tokenHint);

    /**
     * @param tokenId tokenId used to fetch the requested token from the database
     * @param tokenHint token hint indicating if given token is an access or refresh token, or if the token hint was
     *                  not given at all
     * @return Optional of the fetched token, Optional.empty if the requested token was not available
     */
    Optional<Token> fetchToken(String tokenId, TokenHint tokenHint);

    Optional<Client> fetchClient(String clientId);

    Optional<AuthCode> fetchAuthorizationCode(String authorizationCode);

    /**
     * Generates new token with given parameters and unique token id, adds token to database and then updates
     * <code>authorizationCode</code> as used
     * @param expiresIn validity time in seconds
     * @param authorizationCode authorization code to be marked as used
     * @param isAccessToken indicates if token is access or refresh token
     * @param tokenType e.g. "Bearer"
     * @return generated token
     */
    Token getNewTokenFromAuthCode(int expiresIn, AuthCode authorizationCode,
                                  boolean isAccessToken, String tokenType) throws JWTCreationException;

    /**
     * Generates new token with given parameters and unique token id, adds token to database
     * @param expiresIn validity time in seconds
     * @param authorizationCode authorization code to be marked as used
     * @param isAccessToken indicates if token is access or refresh token
     * @param tokenType e.g. "Bearer"
     * @return generated token
     */
    Token getNewToken(int expiresIn, List<String> scope, String authorizationCode, String userLogin,
                      boolean isAccessToken, String tokenType, String clientId) throws JWTCreationException;

    boolean isSessionIdValid(String sessionId);

    boolean areCredentialsValid(Credentials credentials);

    String createNewSession(String login);

    String generateCode(AuthorizationRequest request);

    List<String> getLoggedUsers();

    void logOutUser(String userLogin);
}
