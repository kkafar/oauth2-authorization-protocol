package pl.edu.agh.dp.tkgk.oauth2server.database.queries;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Token;

public class TokenQueries {

    public void addToken(Token token, MongoCollection<Token> tokens) {
        tokens.insertOne(token);
    }

    public void deleteTokens(MongoCollection<Token> tokens, Bson filter) {
        tokens.deleteMany(filter);
    }

    public FindIterable<Token> getTokens(MongoCollection<Token> tokens, Bson filter) {
        return tokens.find(filter);
    }
}
