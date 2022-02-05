package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Session;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.TokenHint;

import java.time.Instant;
import java.util.*;


public class MockAuthorizationDatabaseFacade implements Database {

    private final HashMap<String, Session> sessionHashMap;
    private final HashMap<String, AuthCode> authCodeHashMap;
    private final Random random;
    private final static int SESSION_LIFE_TIME_IN_SECONDS = 1200;
    private final static long CODE_LIFE_TIME_IN_SECONDS = 120;
    private final Credentials validCredentials = new Credentials("ala", "1234");

    private MockAuthorizationDatabaseFacade(){
        sessionHashMap = new HashMap<>();
        authCodeHashMap = new HashMap<>();
        random = new Random();
    }

    @Override
    public Optional<Client> fetchClient(String clientId) {
        if("some_fake_client_id".equals(clientId)){
            Client fakeClient = new Client("some_fake_client_id", "https://www.google.pl", Collections.singletonList("all"));
            return Optional.of(fakeClient);
        }
        return Optional.empty();
    }

    private static class MockAuthorizationDatabaseFacadeHolder{
        public static  final MockAuthorizationDatabaseFacade database = new MockAuthorizationDatabaseFacade();
    }

    public static MockAuthorizationDatabaseFacade getInstance(){
        return MockAuthorizationDatabaseFacadeHolder.database;
    }

    @Override
    public void tokenRevocation(DecodedJWT decodedToken, TokenHint tokenHint) {
        // after revoking refresh token revoke all access tokens with the same authorization grant
        // RFC7009 says that we MAY revoke all refresh tokens assigned to the same authorization grant
    }

    @Override
    public Optional<Token> fetchToken(String tokenId, TokenHint tokenHint) {
        return Optional.empty();
    }

    @Override
    public boolean isSessionIdValid(String sessionId) {
        if(!sessionHashMap.containsKey(sessionId)) return false;
        Session session = sessionHashMap.get(sessionId);
        return session.getExpireTimeInSeconds() > Instant.now().getEpochSecond();
    }

    @Override
    public boolean areCredentialsValid(Credentials credentials) {
        return validCredentials.equals(credentials);
    }

    @Override
    public String createNewSession(String login) {
        byte[] randomBytes = new byte[128];
        random.nextBytes(randomBytes);
        String session_id = new String(Base64.getUrlEncoder().encode(randomBytes));
        long expireTime = Instant.now().getEpochSecond() + SESSION_LIFE_TIME_IN_SECONDS;
        Session session = new Session(session_id, login, expireTime);
        sessionHashMap.put(session_id, session);
        return session_id;
    }

    @Override
    public Optional<AuthCode> fetchAuthorizationCode(String authorizationCode) {
        return Optional.empty();
    }

    @Override
    public String generateCode(AuthorizationRequest request) {
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        String code = new String(Base64.getUrlEncoder().encode(randomBytes));
        long expireTime = Instant.now().getEpochSecond() + CODE_LIFE_TIME_IN_SECONDS;
        AuthCode authCode =
                new AuthCode(code, request.codeChallenge, request.codeChallengeMethod, expireTime,
                        "client", false); // AuthCode needs clientId and used parameters, so I added them here
        authCodeHashMap.put(code, authCode);
        return code;
    }

}
