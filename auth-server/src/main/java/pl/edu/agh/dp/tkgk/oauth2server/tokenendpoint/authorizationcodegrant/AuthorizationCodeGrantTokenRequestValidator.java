package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Checks if there is a valid authorization code, code_verifier (that is compared to code_challenge sent during authorization)
 * and redirection uri (compared to the one sent during authorization)
 */
public class AuthorizationCodeGrantTokenRequestValidator extends BaseHandler<HttpPostRequestDecoder, AuthCode> {

    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CLIENT_ID = "client_id";
    private static final String INVALID_GRANT = "invalid_grant";
    private static final String INVALID_REQUEST = "invalid_request";
    private static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    private static final String CODE = "code";
    private static final String CODE_VERIFIER = "code_verifier";

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

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return AuthorizationServerUtil.serverErrorHttpResponse(e.getMessage());
        }

        return next.handle(authorizationCode);
    }

    private boolean authorizationCodeValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> authorizationCodeOptional = bodyDecoder.fetchAttribute(CODE);

        if (authorizationCodeOptional.isPresent()) {
            String authorizationCodeString = authorizationCodeOptional.get();
            Optional<AuthCode> authCodeOptional = database.fetchAuthorizationCode(authorizationCodeString);

            if (authCodeOptional.isEmpty()) return false;

            authorizationCode = authCodeOptional.get();

            return authorizationCode.isActive() && !authorizationCode.isUsed();
        }

        return false;
    }

    private boolean codeVerifierValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException, NoSuchAlgorithmException {
        Optional<String> codeVerifierOptional = bodyDecoder.fetchAttribute(CODE_VERIFIER);

        if (codeVerifierOptional.isPresent()) {
            String codeVerifier = codeVerifierOptional.get();

            CodeChallengeMethod codeChallengeMethod = authorizationCode.getCodeChallengeMethod();
            String codeChallenge = authorizationCode.getCodeChallenge();

            if (codeChallengeMethod.equals(CodeChallengeMethod.PLAIN)) {
                return codeChallenge.equals(codeVerifier);
            } else if (codeChallengeMethod.equals(CodeChallengeMethod.S256)) {
                String codeVerifierHashed = hashSHA256InBase64Url(codeVerifier);
                return codeChallenge.equals(codeVerifierHashed);
            }

            return true;
        }

        return false;
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

    public static String hashSHA256InBase64Url(String stringToHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
