package pl.edu.agh.dp.tkgk.oauth2server.endpoints.adminpanel;

import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

import javax.xml.crypto.Data;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class InvalidateTokenHandler extends BaseHandler<FullHttpRequest, FullHttpRequest> implements DatabaseInjectable {

    private Database database;

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(request.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, false)){
            String content = request.content().toString(StandardCharsets.UTF_8);
            QueryStringDecoder decoder = new QueryStringDecoder(content, false);
            invalidate(decoder.parameters().keySet());
        }
        return next.handle(request);
    }

    private void invalidate(Set<String> usernames){
        for(String user : usernames){
            database.logOutUser(user);
        }
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
