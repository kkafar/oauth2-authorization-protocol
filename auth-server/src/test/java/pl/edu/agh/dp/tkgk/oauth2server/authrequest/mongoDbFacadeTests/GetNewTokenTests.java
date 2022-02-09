package pl.edu.agh.dp.tkgk.oauth2server.authrequest.mongoDbFacadeTests;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import org.junit.jupiter.api.*;
import pl.edu.agh.dp.tkgk.oauth2server.database.MongoDBFacade;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.DecodedToken;

import java.time.Instant;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetNewTokenTests {

    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    MongoCollection<Client> clients =
            db.getCollection(MongoDBInfo.Collections.CLIENTS_COLLECTION.toString(), Client.class);

    MongoCollection<AuthCode> authCodes =
            db.getCollection(MongoDBInfo.Collections.AUTH_CODES_COLLECTION.toString(), AuthCode.class);

    MongoCollection<Token> accessTokens =
            db.getCollection(MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString(), Token.class);

    MongoCollection<Token> refreshTokens =
            db.getCollection(MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString(), Token.class);

    Queries queries = new Queries();

    AuthCode validAuthCode = new AuthCode("code1", "abc",
            CodeChallengeMethod.PLAIN, Instant.now().plusSeconds(1000).getEpochSecond(), "client1", false,
            List.of("image", "password", "data"));

    private static final int TWO_DAYS_IN_SECONDS = 172800;

    @BeforeAll
    public void beforeAll() {
        clients.drop();
        mongoDBFacade.setDatabase(db);
    }

    @AfterAll
    public void afterAll() {
        clients.drop();
        authCodes.drop();
        accessTokens.drop();
        refreshTokens.drop();
    }

    @BeforeEach
    public void beforeEach() {
        authCodes.drop();
        accessTokens.drop();
        refreshTokens.drop();
        queries.addObjectToCollection(validAuthCode, authCodes);
    }

    @Test
    public void getNewAccessTokenWithAuthCodeTest() {
        // when
        mongoDBFacade.getNewTokenFromAuthCode(2, validAuthCode, true, "Bearer");

        Token generatedAccessToken = queries.getObjectFromCollection(accessTokens, "auth_code", validAuthCode.getCode());
        DecodedToken decodedGeneratedToken = generatedAccessToken.getDecodedToken();

        AuthCode authCodeUsed = queries.getObjectFromCollection(authCodes, "_id", validAuthCode.getCode());

        // then
        assertEquals(1, accessTokens.countDocuments());
        assertNotNull(generatedAccessToken);

        assertEquals(validAuthCode.getCode(), decodedGeneratedToken.getAuthCode());
        assertTrue(decodedGeneratedToken.isAccessToken());
        assertEquals("Bearer", decodedGeneratedToken.getTokenType());
        assertEquals(validAuthCode.getClientId(), decodedGeneratedToken.getClientId());
        assertEquals(decodedGeneratedToken.getExpiresAt(),
                decodedGeneratedToken.getIssuedAt()
                        + Instant.ofEpochSecond(0).plusSeconds(TWO_DAYS_IN_SECONDS).getEpochSecond());
        assertEquals(validAuthCode.getScope(), decodedGeneratedToken.getScopeList());

        assertTrue(authCodeUsed.isUsed());
    }

    @Test
    public void getNewAccessTokenTest() {
        // when
        mongoDBFacade.getNewToken(2, validAuthCode.getScope(), validAuthCode.getCode(), true,
                "Bearer", validAuthCode.getClientId());

        Token generatedAccessToken = queries.getObjectFromCollection(accessTokens, "auth_code", validAuthCode.getCode());
        DecodedToken decodedGeneratedToken = generatedAccessToken.getDecodedToken();

        // then
        assertEquals(1, accessTokens.countDocuments());
        assertNotNull(generatedAccessToken);

        assertEquals(validAuthCode.getCode(), decodedGeneratedToken.getAuthCode());
        assertTrue(decodedGeneratedToken.isAccessToken());
        assertEquals("Bearer", decodedGeneratedToken.getTokenType());
        assertEquals(validAuthCode.getClientId(), decodedGeneratedToken.getClientId());
        assertEquals(decodedGeneratedToken.getExpiresAt(),
                decodedGeneratedToken.getIssuedAt()
                        + Instant.ofEpochSecond(0).plusSeconds(TWO_DAYS_IN_SECONDS).getEpochSecond());
        assertEquals(validAuthCode.getScope(), decodedGeneratedToken.getScopeList());
    }

    @Test
    public void getNewRefreshTokenTest() {
        // when
        mongoDBFacade.getNewToken(2, validAuthCode.getScope(), validAuthCode.getCode(), false,
                "Bearer", validAuthCode.getClientId());

        Token generatedRefreshToken = queries.getObjectFromCollection(refreshTokens, "auth_code", validAuthCode.getCode());
        DecodedToken decodedGeneratedToken = generatedRefreshToken.getDecodedToken();

        // then
        assertEquals(1, refreshTokens.countDocuments());
        assertNotNull(generatedRefreshToken);

        assertEquals(validAuthCode.getCode(), decodedGeneratedToken.getAuthCode());
        assertFalse(decodedGeneratedToken.isAccessToken());
        assertEquals("Bearer", decodedGeneratedToken.getTokenType());
        assertEquals(validAuthCode.getClientId(), decodedGeneratedToken.getClientId());
        assertEquals(decodedGeneratedToken.getExpiresAt(),
                decodedGeneratedToken.getIssuedAt()
                        + Instant.ofEpochSecond(0).plusSeconds(TWO_DAYS_IN_SECONDS).getEpochSecond());
        assertEquals(validAuthCode.getScope(), decodedGeneratedToken.getScopeList());
    }

}
