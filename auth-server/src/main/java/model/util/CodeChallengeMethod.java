package model.util;

public enum CodeChallengeMethod {
    S256,
    PLAIN;

    public static CodeChallengeMethod value(String value) throws UnsupportedCodeChallengeMethodException {
        return switch (value) {
            case "S256" -> S256;
            case "plain" -> PLAIN;
            default -> throw new UnsupportedCodeChallengeMethodException(value + " is not a supported code challenge method");
        };
    }

    public String toString() {
        return switch (this) {
            case S256 -> "S256";
            case PLAIN -> "plain";
        };
    }
}
