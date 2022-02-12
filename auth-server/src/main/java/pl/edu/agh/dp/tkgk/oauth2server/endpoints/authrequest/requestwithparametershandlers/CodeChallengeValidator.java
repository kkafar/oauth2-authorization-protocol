package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers;

import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthEndpointUtil;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.HttpRequestWithParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;

import java.util.List;
import java.util.Map;

public class CodeChallengeValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {
    public static final String CODE_CHALLENGE_IS_MISSING_FRAGMENT = "code_challenge_is_missing";
    public static final String INVALID_CODE_CHALLENGE_FRAGMENT = "invalid_code_challenge";
    public static final String UNKNOWN_CODE_CHALLENGE_METHOD_FRAGMENT = "unknown_code_challenge_method";
    public static final String INVALID_REQUEST = "invalid_request";


    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        Map<String, List<String>> parameters = request.urlParameters;
        String redirect_uri = parameters.get(HttpParameters.REDIRECT_URI).get(0);
        String state = parameters.get(HttpParameters.STATE).get(0);

        if(!parameters.containsKey(HttpParameters.CODE_CHALLENGE)){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, CODE_CHALLENGE_IS_MISSING_FRAGMENT, redirect_uri, state);
        }
        if(!isCodeChallengeValid(parameters.get(HttpParameters.CODE_CHALLENGE).get(0))){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, INVALID_CODE_CHALLENGE_FRAGMENT, redirect_uri, state);
        }

        String codeChallengeMethod = parameters.getOrDefault(HttpParameters.CODE_CHALLENGE_METHOD, List.of("plain")).get(0);
        if(!isCodeChallengeMethodValid(codeChallengeMethod)){
            return AuthEndpointUtil.buildAuthErrorResponse(INVALID_REQUEST, UNKNOWN_CODE_CHALLENGE_METHOD_FRAGMENT, redirect_uri, state);
        }

        return next.handle(request);
    }

    private boolean isCodeChallengeValid(String codeChallenge) {
        return codeChallenge.matches("[a-zA-Z0-9_\\-.~]+");
    }

    private boolean isCodeChallengeMethodValid(String codeChallengeMethod) {
        return CodeChallengeMethod.value(codeChallengeMethod).isPresent();
    }
}
