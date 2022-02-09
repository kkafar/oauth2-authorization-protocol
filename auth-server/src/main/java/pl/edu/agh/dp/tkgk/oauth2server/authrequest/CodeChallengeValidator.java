package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.UrlEncodedResponseBuilder;

import java.util.List;
import java.util.Map;

public class CodeChallengeValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {
    private static final String CODE_CHALLENGE_NOT_PRESENT_URI = "#code_challenge_not_present";
    private static final String INVALID_CODE_CHALLENGE_URI = "#invalid_code_challenge";
    private static final String CODE_CHALLENGE_METHOD_NOT_PRESENT_URI = "#code_challenge_method_not_present";
    private static final String INVALID_CODE_CHALLENGE_METHOD_URI = "#invalid_code_challenge_method";

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new UrlEncodedResponseBuilder();

    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get("redirect_uri").get(0);
        String state = parameters.get("state").get(0);

        if(!parameters.containsKey("code_challenge")){
            return director.constructUrlEncodedErrorResponse(responseBuilder, redirect_uri, "invalid_request", CODE_CHALLENGE_NOT_PRESENT_URI, state);
        }
        if(!isCodeChallengeValid(parameters.get("code_challenge").get(0))){
            return director.constructUrlEncodedErrorResponse(responseBuilder, redirect_uri, "invalid_request", INVALID_CODE_CHALLENGE_URI, state);
        }

        if(!parameters.containsKey("code_challenge_method")){
            return director.constructUrlEncodedErrorResponse(responseBuilder, redirect_uri, "invalid_request", CODE_CHALLENGE_METHOD_NOT_PRESENT_URI, state);
        }
        if(!isCodeChallengeMethodValid(parameters.get("code_challenge_method").get(0), request)){
            return director.constructUrlEncodedErrorResponse(responseBuilder, redirect_uri, "invalid_request", INVALID_CODE_CHALLENGE_METHOD_URI, state);
        }

        return next.handle(request);
    }

    private boolean isCodeChallengeValid(String codeChallenge) {
        return codeChallenge.matches("[a-zA-Z0-9_-]+");
    }

    private boolean isCodeChallengeMethodValid(String codeChallengeMethod, HttpRequestWithParameters request) {
        try {
            request.setCodeChallengeMethod(CodeChallengeMethod.valueOf(codeChallengeMethod));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
