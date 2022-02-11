package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;

import java.util.Optional;
import java.util.Set;

public final class AuthorizationRequest {
    public final String uri;
    public final String redirectUri;
    public final String clientId;
    public final String responseType;
    public final String state;
    public final String codeChallenge;
    public final CodeChallengeMethod codeChallengeMethod;
    public final Set<String> scope;

    public final Credentials credentials;
    public final String sessionId;
    final boolean isScopeAccepted;

    public AuthorizationRequest(String uri, String redirectUri, String clientId, String responseType, String state, String codeChallenge,
                                CodeChallengeMethod codeChallengeMethod, Set<String> scope, Credentials credentials, String sessionId, boolean isScopeAccepted)
    {
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.responseType = responseType;
        this.state = state;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.scope = scope;
        this.credentials = credentials;
        this.sessionId = sessionId;
        this.uri = uri;
        this.isScopeAccepted = isScopeAccepted;
    }

    public Optional<String> getOptionalSessionId() {
        return Optional.ofNullable(sessionId);
    }

    public Optional<Credentials> getOptionalCredentials() {
        return Optional.ofNullable(credentials);
    }
}
