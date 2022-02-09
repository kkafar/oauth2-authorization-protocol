package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.validator.HttpRequestValidator;


/**
 * Proceeds initial token request validation and sends the request for the further validation to the correct handler.
 * Returns invalid request response with status code 400 if the initial validation failed
 */
public class TokenRequestValidator extends BaseHandler<FullHttpRequest, HttpPostRequestDecoder> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        if (!requestValid(request, decoder)) {
            return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, true);
        }

        return next.handle(decoder);
    }

    /**
     * Checks only HTTP method, Content-Type and if token request contains any grant_type parameter (which will be
     * validated later)
     * @param request - request to check
     * @param decoder - provided request decoder
     * @return true if request has valid content type, valid request method and has grant_type parameter in the request
     * body
     */
    private boolean requestValid(FullHttpRequest request, HttpPostRequestDecoder decoder) {
        HttpRequestValidator validator = new HttpRequestValidator(request, decoder);
        return validator.validRequestMethod(HttpMethod.POST)
                && validator.validContentType(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED)
                && validator.hasGrantTypeInRequestBody();
    }
}
