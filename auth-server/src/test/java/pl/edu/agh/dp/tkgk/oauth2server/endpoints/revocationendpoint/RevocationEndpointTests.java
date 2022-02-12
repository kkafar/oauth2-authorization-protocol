package pl.edu.agh.dp.tkgk.oauth2server.endpoints.revocationendpoint;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.handler.codec.http.FullHttpRequest;
import org.junit.jupiter.api.TestInstance;
import pl.edu.agh.dp.tkgk.oauth2server.common.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.database.MongoDBFacade;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RevocationEndpointTests {
    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    Queries queries = new Queries();

    Handler<FullHttpRequest, ?> tokenRequestHandler;

    MongoCollection<Token> accessTokens;
    MongoCollection<Token> refreshTokens;
    MongoCollection<Client> clients;
    MongoCollection<AuthCode> authCodes;
}
