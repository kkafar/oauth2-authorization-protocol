package pl.edu.agh.dp.tkgk.oauth2server.model.util;

import java.util.Objects;

public enum CodeChallengeMethod {
    S256,
    PLAIN;

    public String toString() {
        return switch (this) {
            case S256 -> "S256";
            case PLAIN -> "plain";
        };
    }

    // can be used for optional code_challenge_method parameter handling
    public static CodeChallengeMethod value(String value) {
        return Objects.equals(value, "S256") ? S256 : PLAIN;
    }
}
