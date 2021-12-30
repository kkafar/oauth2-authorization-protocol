package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.database.records.Client;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

/**
 * Verifies if client_id and redirect_uri are present, if client_id is known
 * and if given redirect_uri matches that of client redirect_uri
 */

public class RedirectionUriVerifier extends BaseHandler {

    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECTION_URI = "redirect_uri";

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HashMap<String,String> parameters = AuthorizationServerUtil.extractParameters(request);

        if(!parameters.containsKey(CLIENT_ID)){
            return buildErrorResponse("client_id is missing");
        }
        String clientId = parameters.get(CLIENT_ID);

        if(!parameters.containsKey(REDIRECTION_URI)){
            return buildErrorResponse("redirect_uri is missing");
        }
        String redirectionUri = parameters.get(REDIRECTION_URI);

        Database database = AuthorizationDatabaseProvider.getInstance();
        Optional<Client> optionalClient = database.getClient(clientId);

        if(optionalClient.isEmpty()){
            return buildErrorResponse("Unknown client_id");
        }

        Client client = optionalClient.get();
        if(!client.redirectionUri.equals(redirectionUri)){
            return buildErrorResponse("Given redirect_id does not match client redirect_uri");
        }

        return next.handle(request);
    }

    private FullHttpResponse buildErrorResponse(String msg){
        ByteBuf content = Unpooled.copiedBuffer(AuthorizationServerUtil.buildSimpleHtml("Redirection uri error",msg), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}