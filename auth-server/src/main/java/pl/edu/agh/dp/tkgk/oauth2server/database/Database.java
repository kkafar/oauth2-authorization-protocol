package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.TokenHint;

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

    boolean isSessionIdValid(String sessionId);

    boolean areCredentialsValid(Credentials credentials);

    String createNewSession(String login);

    String generateCode(AuthorizationRequest request);
}
