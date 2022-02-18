package pl.edu.agh.dp.tkgk.oauth2server.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Objects;

public class Credentials {
    @BsonId
    private final String login;
    @BsonProperty(JsonFields.PASSWORD)
    private final String password;
    @BsonProperty(JsonFields.LOGGED_OUT_BY_ADMIN)
    private boolean loggedOutByAdmin;

    @BsonCreator
    public Credentials(@BsonProperty("login") String login, @BsonProperty("password") String password,
                       @BsonProperty("loggedOutByAdmin") boolean loggedOutByAdmin) {
        this.login = login;
        this.password = password;
        this.loggedOutByAdmin = loggedOutByAdmin;
    }

    public static class JsonFields {

        public static final String LOGIN = "_id";

        public static final String PASSWORD = "password";

        public static final String LOGGED_OUT_BY_ADMIN = "logged_out_by_admin";

    }

    public boolean getLoggedOutByAdmin() {
        return loggedOutByAdmin;
    }

    public void setLoggedOutByAdmin(boolean loggedOutByAdmin) {
        this.loggedOutByAdmin = loggedOutByAdmin;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Credentials that = (Credentials) o;

        if (!Objects.equals(login, that.login)) return false;
        return Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
