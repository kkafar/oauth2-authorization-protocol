package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import org.jetbrains.annotations.Nullable;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.util.List;
import java.util.Set;

public class HttpRequestWithParameters2AuthorizationRequest extends BaseHandler<HttpRequestWithParameters, AuthorizationRequest> {
    String[] urlParameters = new String[]{"redirect_uri", "client_id", "response_type", "state", "code_challenge", "code_challenge_method"};
    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        String[] urlParametersValue = getUrlParametersValue(request);
        Set<String> scope = Set.copyOf(request.urlParameters.get("scope"));
        boolean isScopeAccepted = Boolean.parseBoolean(request.bodyParameters.getOrDefault("scope_accepted", List.of("false")).get(0));
        Credentials credentials = getCredentials(request);
        String sessionIdOptional = request.cookies.get("session_id");
        int i = 0;
        AuthorizationRequest authorizationRequest = new AuthorizationRequest(
                request.fullHttpRequest.uri(),
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i++],
                urlParametersValue[i],
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