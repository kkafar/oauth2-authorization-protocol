package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.interfaces.DecodedJWT;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Session;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;

import java.time.Instant;
import java.util.*;

public class RAMDBFacade implements Database{

    private final HashMap<String, Session> sessionHashMap;
    private final HashMap<String, AuthCode> authCodeHashMap;
    private final HashMap<String, Client> clientId2ClientMap;
    private final Random random;
    private final static int SESSION_LIFE_TIME_IN_SECONDS = 1200;
    private final static long CODE_LIFE_TIME_IN_SECONDS = 120;
    private final Credentials validCredentials = new Credentials("ala", "1234");

    private RAMDBFacade() {
        sessionHashMap = new HashMap<>();
        authCodeHashMap = new HashMap<>();
        random = new Random();
        clientId2ClientMap = new HashMap<>();
        insertTestData();
    }

    private void insertTestData() {
        Client client = new Client("super_app", "localhost:1111/ala", List.of("all"));
        clientId2ClientMap.put("super_app", client);
    }

    private static class RAMDBFacadeHolder {
        private static final RAMDBFacade facade = new RAMDBFacade();
    }

    public static Database getInstance() {
        return RAMDBFacadeHolder.facade;
    }

    @Override
    public void tokenRevocation(DecodedJWT decodedToken, TokenHint tokenHint) {

    }

    @Override
    public Optional<Token> fetchToken(String tokenId, TokenHint tokenHint) {
        return Optional.empty();
    }

    @Override
    public Optional<Client> fetchClient(String clientId) {
        return Optional.ofNullable(clientId2ClientMap.get(clientId));
    }

    @Override
    public Optional<AuthCode> fetchAuthorizationCode(String authorizationCode) {
        AuthCode code = authCodeHashMap.get(authorizationCode);
        if(code == null || !code.isActive()) return Optional.empty();
        return Optional.of(code);
    }

    @Override
    public Token getNewTokenFromAuthCode(int expiresIn, AuthCode authorizationCode, boolean isAccessToken, String tokenType) {
        return null;
    }

    @Override
    public Token getNewToken(int expiresIn, List<String> scope, String authorizationCode, boolean isAccessToken, String tokenType, String clientId) {
        return null;
    }

    private String getUniqueTokenId() {
        Optional<Token> token;
        String tokenId;

        do {
            tokenId = TokenUtil.generateTokenId();
            token = fetchToken(tokenId, TokenHint.NO_TOKEN_HINT);
        } while (token.isPresent());

        return tokenId;
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
    public String generateCode(AuthorizationRequest request) {
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        String code = new String(Base64.getUrlEncoder().encode(randomBytes));
        long expireTime = Instant.now().getEpochSecond() + CODE_LIFE_TIME_IN_SECONDS;
        AuthCode authCode =
                new AuthCode(code, request.codeChallenge, request.codeChallengeMethod, expireTime,
                        "client", false, List.of("all")); // AuthCode needs clientId and used parameters, so I added them here
        authCodeHashMap.put(code, authCode);
        return code;
    }
}
