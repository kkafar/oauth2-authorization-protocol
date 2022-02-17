package pl.edu.agh.dp.tkgk.oauth2server.model.util;

import java.util.Locale;
import java.util.Optional;

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
    public static Optional<CodeChallengeMethod> value(String value) {
        value = value.toUpperCase(Locale.ROOT);
        try {
            return Optional.of(valueOf(value));
        }catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }
}
