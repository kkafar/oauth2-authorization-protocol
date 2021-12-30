package pl.edu.agh.dp.tkgk.oauth2server.database;

import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.time.Instant;
import java.util.*;

import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.Session;


class MockAuthorizationDatabaseFacade implements Database{

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
    public Optional<Client> getClient(String clientId) {
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
    public boolean revokeAccessToken() {
        // RFC7009 says that we MAY revoke all refresh tokens assigned to the same authorization grant
        return true;
    }

    @Override
    public boolean revokeRefreshToken() {
        // after revoking refresh token revoke all access tokens with the same authorization grant
        revokeAllAccessTokensWithGivenGrant();
        return true;
    }

    @Override
    public Optional<JSONObject> fetchTokenData() {
        return Optional.empty();
    }

    @Override
    public boolean isSessionIdValid(String sessionId) {
        if(!sessionHashMap.containsKey(sessionId)) return false;
        Session session = sessionHashMap.get(sessionId);
        return session.expireTimeInSeconds > Instant.now().getEpochSecond();
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
    public String generateCode(AuthorizationRequest request) {
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        String code = new String(Base64.getUrlEncoder().encode(randomBytes));
        long expireTime = Instant.now().getEpochSecond() + CODE_LIFE_TIME_IN_SECONDS;
        AuthCode authCode =
                new AuthCode(code, request.redirectUri, request.codeChallenge, request.codeChallengeMethod, expireTime);
        authCodeHashMap.put(code, authCode);
        return code;
    }

    public void revokeAllAccessTokensWithGivenGrant() {}
}
