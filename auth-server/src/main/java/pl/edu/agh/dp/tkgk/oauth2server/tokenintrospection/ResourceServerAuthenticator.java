package pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

public record ResourceServerAuthenticator(FullHttpRequest request) {

    private static final String WWW_AUTHENTICATE_STRING = "Bearer realm=\"auth_server\", error=\"invalid_token\"";

    public boolean authenticate() throws JWTVerificationException {
        HttpHeaders headers = request.headers();
        String authorizationString = headers.get(HttpHeaderNames.AUTHORIZATION);

        if (!authorizationString.startsWith("Bearer ")) return false;

        String tokenString = authorizationString.split(" ")[1];

        DecodedJWT decodedToken = TokenUtil.decodeToken(tokenString);

        Database database = AuthorizationDatabaseProvider.getInstance();

        return database.fetchToken(decodedToken.getId(), TokenHint.NO_TOKEN_HINT).isPresent();
    }

    public FullHttpResponse failedBearerTokenAuthenticationResponse() {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, WWW_AUTHENTICATE_STRING);
        return response;
    }
}
