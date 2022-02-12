package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.converters;

import io.netty.handler.codec.http.FullHttpResponse;
import org.jetbrains.annotations.Nullable;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.AuthorizationRequest;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.HttpRequestWithParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;

import java.util.List;
import java.util.Set;

public class HttpRequestWithParameters2AuthorizationRequest extends BaseHandler<HttpRequestWithParameters, AuthorizationRequest> {
    String[] urlParameters = new String[]{HttpParameters.REDIRECT_URI, HttpParameters.CLIENT_ID, HttpParameters.RESPONSE_TYPE, HttpParameters.STATE, HttpParameters.CODE_CHALLENGE, HttpParameters.CODE_CHALLENGE_METHOD};
    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        String[] urlParametersValue = getUrlParametersValue(request);
        Set<String> scope = Set.copyOf(request.urlParameters.get(HttpParameters.SCOPE));
        boolean isScopeAccepted = Boolean.parseBoolean(request.bodyParameters.getOrDefault("scope_accepted", List.of("false")).get(0));
        Credentials credentials = getCredentials(request);
        String sessionIdOptional = request.cookies.get(HttpParameters.SESSION_ID);
        int i = 0;
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(
                request.fullHttpRequest.uri(),
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i++],
                CodeChallengeMethod.value(urlParametersValue[i]).orElse(CodeChallengeMethod.PLAIN),
                scope,
                credentials,
                sessionIdOptional,
                isScopeAccepted);
        return next.handle(authorizationRequest);
    }

    @Nullable
    private Credentials getCredentials(HttpRequestWithParameters request) {
        if(!request.bodyParameters.containsKey("login") || !request.bodyParameters.containsKey("password"))
            return null;

        String login = request.bodyParameters.get("login").get(0);
        String password = request.bodyParameters.get("password").get(0);

        return new Credentials(login,password);
    }


    private String[] getUrlParametersValue(HttpRequestWithParameters request) {
        String[] urlParametersValue = new String[urlParameters.length];
        for (int i = 0; i < urlParametersValue.length; i++) {
            urlParametersValue[i] = request.urlParameters
                    .get(urlParameters[i])
                    .get(0);
        }
        return urlParametersValue;
    }
}