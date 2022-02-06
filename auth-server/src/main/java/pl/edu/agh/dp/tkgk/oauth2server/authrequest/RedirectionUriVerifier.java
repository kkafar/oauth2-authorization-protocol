package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Verifies if client_id and redirect_uri are present, if client_id is known
 * and if given redirect_uri matches that of client redirect_uri
 */

public class RedirectionUriVerifier extends BaseHandler<HttpRequestWithParameters,HttpRequestWithParameters> {

    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECTION_URI = "redirect_uri";

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;

        if(!parameters.containsKey(CLIENT_ID)){
            return buildErrorResponse("client_id is missing");
        }
        String clientId = parameters.get(CLIENT_ID).get(0);

        if(!parameters.containsKey(REDIRECTION_URI)){
            return buildErrorResponse("redirect_uri is missing");
        }
        String redirectionUri = parameters.get(REDIRECTION_URI).get(0);

        Database database = AuthorizationDatabaseProvider.getInstance();
        Optional<Client> optionalClient = database.fetchClient(clientId);

        if(optionalClient.isEmpty()){
            return buildErrorResponse("Unknown client_id");
        }

        Client client = optionalClient.get();
        if(!client.getRedirectUri().equals(redirectionUri)){
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
