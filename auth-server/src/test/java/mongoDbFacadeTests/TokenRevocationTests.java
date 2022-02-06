package mongoDbFacadeTests;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.*;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.database.MongoDBFacade;
import model.Token;
import model.util.DecodedToken;
import model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenRevocationTests {
    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    MongoCollection<Token> accessTokens =
            db.getCollection(MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString(), Token.class);

    MongoCollection<Token> refreshTokens =
            db.getCollection(MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString(), Token.class);

    Queries queries = new Queries();

    String properToken = generateToken();
    DecodedJWT decodedProperToken = TokenUtil.decodeToken(properToken);
    String properTokenAuthCode = decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE).asString();

    Token accessToken1 = new Token("1", "token1", properTokenAuthCode, "client1");
    Token accessToken2 = new Token("2", "token2", properTokenAuthCode, "client1");
    Token accessToken3 = new Token("3", "token3", "authcode2", "client2");
    Token accessToken4 = new Token("4", "token4", "authcode2", "client2");
    Token refreshToken1 = new Token("5", "token5", properTokenAuthCode, "client1");
    Token refreshToken2 = new Token("6", "token6", properTokenAuthCode, "client1");
    Token refreshToken3 = new Token("7", "token7", "authcode2", "client2");
    Token refreshToken4 = new Token("8", "token8", "authcode2", "client2");

    private String generateToken() {
        Algorithm algorithm = Algorithm.HMAC256(AuthorizationServerUtil.SECRET);
        return JWT.create()
                .withClaim(DecodedToken.CustomClaims.AUTH_CODE, "authcode1")
                .withClaim(DecodedToken.CustomClaims.TOKEN_TYPE, "Bearer")
                .withClaim(DecodedToken.CustomClaims.IS_ACCESS_TOKEN, true)
                .withClaim(DecodedToken.CustomClaims.SCOPE, "all")
                .withJWTId("someJWTID")
                .withExpiresAt(Date.valueOf(LocalDate.now().plusDays(356)))
                .withIssuedAt(Date.valueOf(LocalDate.now()))
                .sign(algorithm);
    }

    @BeforeAll
    public void beforeAll() {
        mongoDBFacade.setDatabase(db);
    }

    @AfterAll
    public void afterAll() {
        accessTokens.drop();
        refreshTokens.drop();
    }

    @BeforeEach
    public void beforeEach() {
        accessTokens.drop();
        refreshTokens.drop();
        queries.addObjectsToCollection(List.of(accessToken1, accessToken2, accessToken3, accessToken4), accessTokens);
        queries.addObjectsToCollection(List.of(refreshToken1, refreshToken2, refreshToken3, refreshToken4), refreshTokens);
    }

    @Test
    public void tokenRevocationWithPresentAccessTokenTest() {
        // given
        DecodedJWT decodedToken = mock(DecodedJWT.class);
        when(decodedToken.getId()).thenReturn(accessToken1.getJwtId());
        when(decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE))
                .thenReturn(decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE));

        // when
        mongoDBFacade.tokenRevocation(decodedToken, TokenHint.ACCESS_TOKEN);

        ArrayList<Token> accessTokensAfterRevocation = accessTokens.find().into(new ArrayList<>());
        ArrayList<Token> refreshTokensAfterRevocation = refreshTokens.find().into(new ArrayList<>());

        // then
        assertEquals(3, accessTokensAfterRevocation.size());
        assertEquals(2, refreshTokensAfterRevocation.size());
        assertTrue(accessTokensAfterRevocation.containsAll(List.of(accessToken2, accessToken3, accessToken4)));
        assertTrue(refreshTokensAfterRevocation.containsAll(List.of(refreshToken3, refreshToken4)));
    }

    @Test
    public void tokenRevocationWithPresentRefreshTokenTest() {
        // given
        DecodedJWT decodedToken = mock(DecodedJWT.class);
        when(decodedToken.getId()).thenReturn(refreshToken1.getJwtId());
        when(decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE))
                .thenReturn(decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE));

        // when
        mongoDBFacade.tokenRevocation(decodedToken, TokenHint.REFRESH_TOKEN);

        ArrayList<Token> accessTokensAfterRevocation = accessTokens.find().into(new ArrayList<>());
        ArrayList<Token> refreshTokensAfterRevocation = refreshTokens.find().into(new ArrayList<>());

        // then
        assertEquals(2, accessTokensAfterRevocation.size());
        assertEquals(3, refreshTokensAfterRevocation.size());
        assertTrue(accessTokensAfterRevocation.containsAll(List.of(accessToken3, accessToken4)));
        assertTrue(refreshTokensAfterRevocation.containsAll(List.of(refreshToken2, refreshToken3, refreshToken4)));
    }

    @Test
    public void tokenRevocationWithMissingAccessTokenTest() {
        // given
        DecodedJWT decodedToken = mock(DecodedJWT.class);
        when(decodedToken.getId()).thenReturn("-1");
        when(decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE))
                .thenReturn(decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE));

        // to make sure that there is no refresh token using the same authentication code as the access token we're testing:
        queries.updateObjectFromCollection(refreshTokens,
                Token.JsonFields.ID, refreshToken1.getJwtId(),
                Token.JsonFields.AUTH_CODE, "authcode3");

        queries.updateObjectFromCollection(refreshTokens,
                Token.JsonFields.ID, refreshToken2.getJwtId(),
                Token.JsonFields.AUTH_CODE, "authcode3");

        // when
        mongoDBFacade.tokenRevocation(decodedToken, TokenHint.ACCESS_TOKEN);

        ArrayList<Token> accessTokensAfterRevocation = accessTokens.find().into(new ArrayList<>());
        ArrayList<Token> refreshTokensAfterRevocation = refreshTokens.find().into(new ArrayList<>());

        // then
        assertEquals(4, accessTokensAfterRevocation.size());
        assertEquals(4, refreshTokensAfterRevocation.size());
    }

    @Test
    public void tokenRevocationWithMissingRefreshTokenTest() {
        // given
        DecodedJWT decodedToken = mock(DecodedJWT.class);
        when(decodedToken.getId()).thenReturn("-1");
        when(decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE))
                .thenReturn(decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE));

        // to make sure that there is no access token using the same authentication code as the refresh token we're testing:
        queries.updateObjectFromCollection(accessTokens,
                Token.JsonFields.ID, accessToken1.getJwtId(),
                Token.JsonFields.AUTH_CODE, "authcode3");

        queries.updateObjectFromCollection(accessTokens,
                Token.JsonFields.ID, accessToken2.getJwtId(),
                Token.JsonFields.AUTH_CODE, "authcode3");

        // when
        mongoDBFacade.tokenRevocation(decodedToken, TokenHint.REFRESH_TOKEN);

        ArrayList<Token> accessTokensAfterRevocation = accessTokens.find().into(new ArrayList<>());
        ArrayList<Token> refreshTokensAfterRevocation = refreshTokens.find().into(new ArrayList<>());

        // then
        assertEquals(4, accessTokensAfterRevocation.size());
        assertEquals(4, refreshTokensAfterRevocation.size());
    }

    @Test
    public void tokenRevocationOfActiveAccessTokenWithMissingTokenHintTest() {
        // given
        DecodedJWT decodedToken = mock(DecodedJWT.class);
        when(decodedToken.getId()).thenReturn(accessToken1.getJwtId());
        when(decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE))
                .thenReturn(decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE));

        // when
        mongoDBFacade.tokenRevocation(decodedToken, TokenHint.NO_TOKEN_HINT);

        ArrayList<Token> accessTokensAfterRevocation = accessTokens.find().into(new ArrayList<>());
        ArrayList<Token> refreshTokensAfterRevocation = refreshTokens.find().into(new ArrayList<>());

        // then
        assertEquals(3, accessTokensAfterRevocation.size());
        assertEquals(2, refreshTokensAfterRevocation.size());
        assertTrue(accessTokensAfterRevocation.containsAll(List.of(accessToken2, accessToken3, accessToken4)));
        assertTrue(refreshTokensAfterRevocation.containsAll(List.of(refreshToken3, refreshToken4)));
    }

    @Test
    public void tokenRevocationOfActiveAccessTokenWithWrongTokenHintTest() {
        // given
        DecodedJWT decodedToken = mock(DecodedJWT.class);
        when(decodedToken.getId()).thenReturn(accessToken1.getJwtId());
        when(decodedToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE))
                .thenReturn(decodedProperToken.getClaim(DecodedToken.CustomClaims.AUTH_CODE));

        // when
        mongoDBFacade.tokenRevocation(decodedToken, TokenHint.REFRESH_TOKEN);

        ArrayList<Token> accessTokensAfterRevocation = accessTokens.find().into(new ArrayList<>());
        ArrayList<Token> refreshTokensAfterRevocation = refreshTokens.find().into(new ArrayList<>());

        // then
        assertEquals(3, accessTokensAfterRevocation.size());
        assertEquals(2, refreshTokensAfterRevocation.size());
        assertTrue(accessTokensAfterRevocation.containsAll(List.of(accessToken2, accessToken3, accessToken4)));
        assertTrue(refreshTokensAfterRevocation.containsAll(List.of(refreshToken3, refreshToken4)));
    }
}
