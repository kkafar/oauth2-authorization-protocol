package pl.edu.agh.dp.tkgk.oauth2server.database.mongodb;

public class MongoDBInfo {
    public static final String DATABASE_NAME = "auth_server";

    public enum Collections {
        ACCESS_TOKENS_COLLECTION,
        REFRESH_TOKENS_COLLECTION,
        AUTH_CODES_COLLECTION,
        CLIENTS_COLLECTION,
        SESSIONS_COLLECTION,
        CREDENTIALS_COLLECTION;

        @Override
        public String toString() {
            return switch (this) {
                case ACCESS_TOKENS_COLLECTION -> "access-tokens";
                case REFRESH_TOKENS_COLLECTION -> "refresh-tokens";
                case AUTH_CODES_COLLECTION -> "auth-codes";
                case CLIENTS_COLLECTION -> "clients";
                case SESSIONS_COLLECTION -> "sessions";
                case CREDENTIALS_COLLECTION -> "credentials";
            };
        }
    }
}
