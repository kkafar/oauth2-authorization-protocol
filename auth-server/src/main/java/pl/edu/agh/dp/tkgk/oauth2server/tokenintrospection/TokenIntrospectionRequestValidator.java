package pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.validator.HttpRequestValidator;

public class TokenIntrospectionRequestValidator extends BaseHandler<FullHttpRequest, HttpPostRequestDecoder> {

    private static final String CORRECT_CONTENT_TYPE = "application/x-www-form-urlencoded";

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        if (!requestValid(request, decoder)) {
            return AuthorizationServerUtil.badRequestHttpResponse(false);
        }

        ResourceServerAuthenticator authenticator = new ResourceServerAuthenticator(request);
        if (!authenticator.authenticate()) {
            return authenticator.failedBearerTokenAuthenticationResponse();
        }

        return next.handle(decoder);
    }

    private boolean requestValid(FullHttpRequest request, HttpPostRequestDecoder decoder) {
        HttpRequestValidator validator = new HttpRequestValidator(request, decoder);
        return validator.validRequestMethod(HttpMethod.POST)
                && validator.validContentType(CORRECT_CONTENT_TYPE)
                && validator.hasTokenInRequestBody()
                && validator.hasAuthorizationHeader();
    }
}
