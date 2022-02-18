package pl.edu.agh.dp.tkgk.oauth2server.endpoints.adminpanel;

import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

import javax.xml.crypto.Data;
import java.util.Set;

public class InvalidateTokenHandler extends BaseHandler<FullHttpRequest, FullHttpRequest> implements DatabaseInjectable {

    private Database database;

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(request.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, false)){
            QueryStringDecoder decoder = new QueryStringDecoder(new String(request.content().array()));
            invalidate(decoder.parameters().keySet());
        }
        return next.handle(request);
    }

    private void invalidate(Set<String> usernames){

    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
