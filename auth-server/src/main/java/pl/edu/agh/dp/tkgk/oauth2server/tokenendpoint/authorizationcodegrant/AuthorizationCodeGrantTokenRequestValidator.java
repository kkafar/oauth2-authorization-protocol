package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.database.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Checks if there is a valid authorization code, code_verifier (that is compared to code_challenge sent during authorization)
 * and redirection uri (compared to the one sent during authorization)
 */
public class AuthorizationCodeGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, String> {

    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CLIENT_ID = "client_id";
    private static final String INVALID_GRANT = "invalid_grant";
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    private static final String CODE = "code";
    private static final String CODE_VERIFIER = "code_verifier";

    private String authorizationCodeString;

    private AuthCode authorizationCode;

    private final Database database = AuthorizationDatabaseProvider.getInstance();

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {
            if (!authorizationCodeValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        INVALID_GRANT);
            }

            if (!codeVerifierValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        UNAUTHORIZED_CLIENT);
            }

            if (!redirectUriValid(bodyDecoder)) {
                return AuthorizationServerUtil.badRequestHttpResponseWithCustomError(true,
                        INVALID_REQUEST);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        return next.handle(authorizationCodeString);
    }

    private boolean authorizationCodeValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> authorizationCodeOptional = bodyDecoder.fetchAttribute(CODE);

        if (authorizationCodeOptional.isPresent()) {
            this.authorizationCodeString = authorizationCodeOptional.get();
            Optional<AuthCode> authCodeOptional = database.fetchAuthorizationCode(authorizationCodeString);

            if (authCodeOptional.isEmpty()) return false;

            authorizationCode = authCodeOptional.get();

            return authorizationCode.isActive() && !authorizationCode.isUsed();
        }

        return false;
    }

    private boolean codeVerifierValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> codeVerifierString = bodyDecoder.fetchAttribute(CODE_VERIFIER);

        if (codeVerifierString.isPresent()) {
            // check if codeVerifier matches with codeChallenge sent during authorization of this client
            return true;
        } else return false;
    }

    private boolean redirectUriValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> redirectUriOptional = bodyDecoder.fetchAttribute(REDIRECT_URI);
        Optional<String> clientIdOptional = bodyDecoder.fetchAttribute(CLIENT_ID);

        if (redirectUriOptional.isPresent() && clientIdOptional.isPresent()) {
            Optional<Client> clientOptional = database.fetchClient(clientIdOptional.get());

            if (clientOptional.isEmpty()) return false;

            Client client = clientOptional.get();

            return Objects.equals(client.getRedirectUri(), redirectUriOptional.get());
        }

        return false;
    }
}
