package mongoDbFacadeTests;

import static org.mockito.Mockito.mock;

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

import java.time.Instant;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetNewTokenWithAuthCodeTests {

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

    AuthCode authCode = new AuthCode("code", "abc",
            CodeChallengeMethod.PLAIN, Instant.now().plusSeconds(1000).getEpochSecond(), "client", false,
            List.of("image", "password", "data"));

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
        queries.addObjectToCollection(authCode, authCodes);
    }

    @Test
    public void getNewAccessTokenWithValidAuthCode() {
//        // when
//        mongoDBFacade.getNewTokenFromAuthCode(2, authCode, true, "Bearer");
//
//        Token generatedAccessToken = queries.getObjectFromCollection(accessTokens, "auth_code", authCode.getCode());
//
//        // then
//        assertEquals(1, accessTokens.countDocuments());
//        assertNotNull(generatedAccessToken);
    }
}
