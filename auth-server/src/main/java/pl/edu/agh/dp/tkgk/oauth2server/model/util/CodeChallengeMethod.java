package pl.edu.agh.dp.tkgk.oauth2server.model.util;

public enum CodeChallengeMethod {
    S256,
    PLAIN;

    public String toString() {
        return switch (this) {
            case S256 -> "S256";
            case PLAIN -> "plain";
        };
    }
}