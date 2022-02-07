package pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection;

import io.netty.handler.codec.http.*;

public record ResourceServerAuthenticator(FullHttpRequest request) {

    private static final String WWW_AUTHENTICATE_STRING = "Bearer realm=\"auth_server\", error=\"invalid_token\"";

    public boolean authenticate() {
        HttpHeaders headers = request.headers();
        String authorizationString = headers.get(HttpHeaderNames.AUTHORIZATION);

        if (!authorizationString.startsWith("Bearer ")) return false;

        String tokenString = authorizationString.split(" ")[1];

        // todo: Bearer token validation

        return true;
    }

    public FullHttpResponse failedBearerTokenAuthenticationResponse() {
        // todo: MAY add error_description to this response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, WWW_AUTHENTICATE_STRING);
        return response;
    }
}
