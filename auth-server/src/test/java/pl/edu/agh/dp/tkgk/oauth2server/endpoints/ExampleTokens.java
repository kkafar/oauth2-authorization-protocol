package pl.edu.agh.dp.tkgk.oauth2server.endpoints;

import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;

import java.util.List;

public class ExampleTokens {

    Token activeAccessTokenObj;
    Token activeRefreshTokenObj;
    Token expiredAccessTokenObj;
    Token expiredRefreshTokenObj;
    Token notInDbTokenObj;

    public ExampleTokens() {
        generateTokens();
    }

    private void generateTokens() {
        String notInDbTokenId = TokenUtil.generateTokenId();
        String notInDbToken = TokenUtil.generateToken(5, List.of("something"), "some_code",
                true, "Bearer", notInDbTokenId);
        notInDbTokenObj = new Token(notInDbTokenId, notInDbToken, "some_code", "some_client");

        String activeRefreshTokenId = TokenUtil.generateTokenId();
        String activeRefreshToken = TokenUtil.generateToken(7, List.of("some_scope"), "some_code",
                false, "Bearer", activeRefreshTokenId);
        activeRefreshTokenObj = new Token(activeRefreshTokenId, activeRefreshToken, "some_code", "client1");

        String expiredRefreshTokenId = TokenUtil.generateTokenId();
        String expiredRefreshToken = TokenUtil.generateToken(0, List.of("some_scope"), "some_code",
                false, "Bearer", expiredRefreshTokenId);
        expiredRefreshTokenObj = new Token(expiredRefreshTokenId, expiredRefreshToken, "some_code", "client1");

        String activeAccessTokenId = TokenUtil.generateTokenId();
        String activeAccessToken = TokenUtil.generateToken(7, List.of("some_scope"), "some_code",
                true, "Bearer", activeAccessTokenId);
        activeAccessTokenObj = new Token(activeAccessTokenId, activeAccessToken, "some_code", "client1");

        String expiredAccessTokenId = TokenUtil.generateTokenId();
        String expiredAccessToken = TokenUtil.generateToken(0, List.of("some_scope"), "some_code",
                true, "Bearer", expiredAccessTokenId);
        expiredAccessTokenObj = new Token(expiredAccessTokenId, expiredAccessToken, "some_code", "client1");
    }

    public Token getActiveAccessToken() {
        return activeAccessTokenObj;
    }

    public Token getActiveRefreshToken() {
        return activeRefreshTokenObj;
    }

    public Token getExpiredAccessToken() {
        return expiredAccessTokenObj;
    }

    public Token getExpiredRefreshToken() {
        return expiredRefreshTokenObj;
    }

    public Token getNotInDbToken() {
        return notInDbTokenObj;
    }
}
