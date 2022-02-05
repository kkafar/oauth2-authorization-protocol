package mongoDbFacadeTests;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.*;
import pl.edu.agh.dp.tkgk.oauth2server.database.MongoDBFacade;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FetchTokenTests {
    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    MongoCollection<Token> accessTokens =
            db.getCollection(MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString(), Token.class);

    MongoCollection<Token> refreshTokens =
            db.getCollection(MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString(), Token.class);

    Queries queries = new Queries();

    Token accessToken1 = new Token("1", "token1", "authcode1", "client1");
    Token refreshToken1 = new Token("2", "token2", "authcode2", "client2");

    @BeforeAll
    public void beforeAll() {
        accessTokens.drop();
        refreshTokens.drop();
        queries.addObjectToCollection(accessToken1, accessTokens);
        queries.addObjectToCollection(refreshToken1, refreshTokens);
        mongoDBFacade.setDatabase(db);
    }

    @AfterAll
    public void afterAll() {
        accessTokens.drop();
        refreshTokens.drop();
    }

    @Test
    public void fetchAvailableAccessTokenWithCorrectTokenHintTest() {
        //when
        Optional<Token> token = mongoDBFacade.fetchToken(accessToken1.getJwtId(), TokenHint.ACCESS_TOKEN);
        //then
        assertTrue(token.isPresent());
        assertEquals(accessToken1, token.get());
    }

    @Test
    public void fetchAvailableRefreshTokenWithCorrectTokenHintTest() {
        //when
        Optional<Token> token = mongoDBFacade.fetchToken(refreshToken1.getJwtId(), TokenHint.REFRESH_TOKEN);
        //then
        assertTrue(token.isPresent());
        assertEquals(refreshToken1, token.get());
    }

    @Test
    public void fetchUnavailableTokenTest() {
        //when
        Optional<Token> token = mongoDBFacade.fetchToken("some-fake-id", TokenHint.ACCESS_TOKEN);
        //then
        assertTrue(token.isEmpty());
    }

    @Test
    public void fetchAvailableRefreshTokenWithIncorrectTokenHintTest() {
        //when
        Optional<Token> token = mongoDBFacade.fetchToken(refreshToken1.getJwtId(), TokenHint.ACCESS_TOKEN);
        //then
        assertTrue(token.isPresent());
        assertEquals(refreshToken1, token.get());
    }

    @Test
    public void fetchAvailableAccessTokenWithIncorrectTokenHintTest() {
        //when
        Optional<Token> token = mongoDBFacade.fetchToken(accessToken1.getJwtId(), TokenHint.REFRESH_TOKEN);
        //then
        assertTrue(token.isPresent());
        assertEquals(accessToken1, token.get());
    }

    @Test
    public void fetchAvailableTokenWithoutTokenHintTest() {
        //when
        Optional<Token> token = mongoDBFacade.fetchToken(refreshToken1.getJwtId(), TokenHint.NO_TOKEN_HINT);
        //then
        assertTrue(token.isPresent());
        assertEquals(refreshToken1, token.get());
    }

}
