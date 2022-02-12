package pl.edu.agh.dp.tkgk.oauth2server.database;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.model.*;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RAMDBFacade implements Database{

    private final Map<String, Session> sessionHashMap;
    private final Map<String, AuthCode> authCodeHashMap;
    private final Map<String, Client> clientId2ClientMap;
    private final Map<String, Token> accessTokens;
    private final Map<String, Token> refreshTokens;
    private final Random random;
    private final static int SESSION_LIFE_TIME_IN_SECONDS = 1200;
    private final static long CODE_LIFE_TIME_IN_SECONDS = 120;
    private final Credentials validCredentials = new Credentials("ala", "makota");

    private RAMDBFacade() {
        sessionHashMap = new ConcurrentHashMap<>();
        authCodeHashMap = new ConcurrentHashMap<>();
        random = new Random();
        clientId2ClientMap = new HashMap<>();
        insertTestData();
        refreshTokens = new ConcurrentHashMap<>();
        accessTokens = new ConcurrentHashMap<>();
    }

    private void insertTestData() {
        Client client = new Client("super_app", "localhost:1111/ret", List.of("all"));
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
        String authCode = decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE).asString();
        List<Token> tokens = new LinkedList<>(accessTokens.values());
        tokens.addAll(refreshTokens.values());
        tokens.stream()
                .filter(t -> t.getAuthCode().equals(authCode))
                .forEach(t -> {accessTokens.remove(t.getToken()); refreshTokens.remove(t.getToken());});
    }

    @Override
    public Optional<Token> fetchToken(String tokenId, TokenHint tokenHint) {
        if(accessTokens.containsKey(tokenId)) return Optional.of(accessTokens.get(tokenId));
        return Optional.ofNullable(refreshTokens.get(tokenId));
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
    public Token getNewTokenFromAuthCode(int expiresIn, AuthCode authorizationCode,
                                         boolean isAccessToken, String tokenType) throws JWTCreationException {
        Token token = getNewToken(expiresIn, authorizationCode.getScope(), authorizationCode.getCode(),
                isAccessToken, tokenType, authorizationCode.getClientId());
        authCodeHashMap.get(authorizationCode.getCode()).setUsed(true);
        return token;
    }

    @Override
    public Token getNewToken(int expiresIn, List<String> scope, String authorizationCode,
                             boolean isAccessToken, String tokenType, String clientId) throws JWTCreationException {
        String tokenId = getUniqueTokenId();
        String token = TokenUtil.generateToken(expiresIn, scope, authorizationCode, isAccessToken, tokenType, tokenId);
        return new Token(tokenId, token, authorizationCode, clientId);
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
        if(session.getExpireTimeInSeconds() > Instant.now().getEpochSecond()){
            return true;
        }
        sessionHashMap.remove(sessionId);
        return false;
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
                        request.clientId, false, request.scope.stream().toList()); // AuthCode needs clientId and used parameters, so I added them here
        authCodeHashMap.put(code, authCode);
        return code;
    }
}
