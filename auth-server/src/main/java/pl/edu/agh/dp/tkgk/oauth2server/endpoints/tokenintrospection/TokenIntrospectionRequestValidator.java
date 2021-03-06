package pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenintrospection;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.validator.HttpRequestValidator;

import java.util.logging.Logger;

public class TokenIntrospectionRequestValidator extends BaseHandler<FullHttpRequest, HttpPostRequestDecoder> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private final Logger logger = Logger.getGlobal();

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        if (!requestValid(request, decoder)) {
            return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, false);
        }

        ResourceServerAuthenticator authenticator = new ResourceServerAuthenticator(request);

        try {
            if (!authenticator.authenticate()) {
                return authenticator.failedBearerTokenAuthenticationResponse();
            }
        } catch (JWTVerificationException e) {
            logger.info(e.getMessage());
            return authenticator.failedBearerTokenAuthenticationResponse();
        }

        return next.handle(decoder);
    }

    private boolean requestValid(FullHttpRequest request, HttpPostRequestDecoder decoder) {
        HttpRequestValidator validator = new HttpRequestValidator(request, decoder);
        return validator.validRequestMethod(HttpMethod.POST)
                && validator.validContentType(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)
                && validator.hasTokenInRequestBody()
                && validator.hasAuthorizationHeader()
                && !validator.hasDuplicateParameters();
    }
}
