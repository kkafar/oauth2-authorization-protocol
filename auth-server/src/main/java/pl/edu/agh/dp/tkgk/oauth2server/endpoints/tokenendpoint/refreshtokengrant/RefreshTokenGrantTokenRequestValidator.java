package pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint.refreshtokengrant;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.*;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Checks if refresh token and scope (if attached to the request) are valid so that the token request can be served
 */
public class RefreshTokenGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, AuthCode> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private final Database database = AuthorizationDatabaseProvider.getInstance();

    private Token refreshTokenObj;

    private AuthCode authCodeObj;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {

        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!refreshTokenValid(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_GRANT, true);
            }

            if (!scopeValidIfAdded(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_SCOPE, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }

        return next.handle(authCodeObj);
    }

    private boolean scopeValidIfAdded(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> scopeStringOptional = bodyDecoder.fetchAttribute(HttpParameters.SCOPE);

        if (scopeStringOptional.isPresent()) {
            String scopeString = scopeStringOptional.get();

            List<String> scopeItems = Arrays.asList(scopeString.trim().split(" "));

            if (new ArrayList<>(new HashSet<>(scopeItems)).size() != scopeItems.size()) return false; // requested scope contains duplicates

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
        Optional<String> refreshTokenOptional = bodyDecoder.fetchAttribute(TokenHint.REFRESH_TOKEN.toString());

        if (refreshTokenOptional.isPresent()) {
            String refreshToken = refreshTokenOptional.get();

            DecodedJWT decodedRefreshToken;
            try {
                decodedRefreshToken = TokenUtil.decodeToken(refreshToken);
            } catch (JWTVerificationException e) {
                e.printStackTrace();
                return false;
            }

            Optional<Token> refreshTokenObjOptional = database.fetchToken(decodedRefreshToken.getId(), TokenHint.REFRESH_TOKEN);

            if (refreshTokenObjOptional.isEmpty()) return false;

            refreshTokenObj = refreshTokenObjOptional.get();
            DecodedToken decodedRefreshTokenObj = refreshTokenObj.getDecodedToken();

            return decodedRefreshTokenObj.isActive() && !decodedRefreshTokenObj.isAccessToken();
        }

        return false;
    }
}
