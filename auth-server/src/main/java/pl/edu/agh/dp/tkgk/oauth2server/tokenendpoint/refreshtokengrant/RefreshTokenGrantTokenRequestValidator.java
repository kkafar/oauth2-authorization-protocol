package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.refreshtokengrant;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import model.AuthCode;
import model.Token;
import model.util.DecodedToken;
import model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Checks if refresh token and scope (if attached to the request) are valid so that the token request can be served
 */
public class RefreshTokenGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, AuthCode> {

    private static final String INVALID_GRANT = "invalid_grant";
    private static final String INVALID_SCOPE = "invalid_scope";
    private static final String SCOPE = "scope";
    private static final String REFRESH_TOKEN = "refresh_token";

    Database database = AuthorizationDatabaseProvider.getInstance();

    Token refreshTokenObj;

    AuthCode authCodeObj;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {

        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!refreshTokenValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        INVALID_GRANT);
            }

            if (!scopeValidIfAdded(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        INVALID_SCOPE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        return next.handle(authCodeObj);
    }

    private boolean scopeValidIfAdded(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> scopeStringOptional = bodyDecoder.fetchAttribute(SCOPE);

        if (scopeStringOptional.isPresent()) {
            String scopeString = scopeStringOptional.get();

            List<String> scopeItems = Arrays.asList(scopeString.trim().split(" "));

            if (Set.of(scopeItems).size() != scopeItems.size()) return false; // requested scope contains duplicates

            Optional<AuthCode> authCodeOptional = database.fetchAuthorizationCode(refreshTokenObj.getAuthCode());

            if (authCodeOptional.isEmpty()) return false;

            authCodeObj = authCodeOptional.get();
            List<String> authCodeScope = authCodeObj.getScope();

            for (String scopeItem : scopeItems) {
                if (!authCodeScope.contains(scopeItem)) return false; // requested scope exceeds scope requested during authorization
            }

            authCodeObj.setScope(scopeItems); // authCodeObj will be sent to next handler to generate new token using its fields, that means also its scope,
            // after this operation we will take into consideration requested scope (which could have been only narrowed in comparison to auth code's scope)

            return true;
        }

        return true; // scope was optional
    }

    private boolean refreshTokenValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> refreshTokenOptional = bodyDecoder.fetchAttribute(REFRESH_TOKEN);

        if (refreshTokenOptional.isPresent()) {
            String refreshToken = refreshTokenOptional.get();

            DecodedJWT decodedRefreshToken = TokenUtil.decodeToken(refreshToken);
            Optional<Token> refreshTokenObjOptional = database.fetchToken(decodedRefreshToken.getId(), TokenHint.REFRESH_TOKEN);

            if (refreshTokenObjOptional.isEmpty()) return false;

            refreshTokenObj = refreshTokenObjOptional.get();
            DecodedToken decodedRefreshTokenObj = refreshTokenObj.getDecodedToken();

            return decodedRefreshTokenObj.isActive() && !decodedRefreshTokenObj.isAccessToken();
        }

        return false;
    }
}
