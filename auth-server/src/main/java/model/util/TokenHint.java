package model.util;

public enum TokenHint {
    ACCESS_TOKEN,
    REFRESH_TOKEN,
    NO_TOKEN_HINT;

    public String toString() {
        return switch (this) {
            case ACCESS_TOKEN -> "access_token";
            case REFRESH_TOKEN -> "refresh_token";
            case NO_TOKEN_HINT -> "no_token_hint";
        };
    }

    public static TokenHint value(String value) {
        return switch (value) {
            case "access_token" -> ACCESS_TOKEN;
            case "refresh_token" -> REFRESH_TOKEN;
            default -> NO_TOKEN_HINT;
        };
    }
}
