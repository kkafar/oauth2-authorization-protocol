package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;

import java.util.Set;

public class AuthorizationRequestMother {
    public static AuthorizationRequest getRequestWithNeitherSessionIdNorCredentials(){
     return new AuthorizationRequest(
             "http",
             "http2",
             "super_app",
             "code",
             "sunny",
             "challenge",
             CodeChallengeMethod.PLAIN,
             Set.of("all"),
             null,
             null,
             false
     );
    }

    public static AuthorizationRequest getRequestWithSessionAndNoCredentials(){
        return new AuthorizationRequest(
                "http",
                "http2",
                "super_app",
                "code",
                "sunny",
                "challenge",
                CodeChallengeMethod.PLAIN,
                Set.of("all"),
                null,
                "session_ala",
                false
        );
    }

    public static AuthorizationRequest getRequestWithSessionAndCredentials(){
        return new AuthorizationRequest(
                "http",
                "http2",
                "super_app",
                "code",
                "sunny",
                "challenge",
                CodeChallengeMethod.PLAIN,
                Set.of("all"),
                new Credentials("ala", "makota"),
                "session_ala",
                false
        );
    }

    public static AuthorizationRequest getRequestWithNoSessionButPresentCredentials(){
        return new AuthorizationRequest(
                "http",
                "http2",
                "super_app",
                "code",
                "sunny",
                "challenge",
                CodeChallengeMethod.PLAIN,
                Set.of("all"),
                new Credentials("ala", "makota"),
                null,
                false
        );
    }
}
