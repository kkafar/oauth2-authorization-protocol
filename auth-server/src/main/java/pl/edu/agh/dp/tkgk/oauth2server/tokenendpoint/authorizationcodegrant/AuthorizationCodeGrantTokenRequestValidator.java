package pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.authorizationcodegrant;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

import java.io.IOException;
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

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private AuthCode authorizationCode;

    private final Database database = AuthorizationDatabaseProvider.getInstance();

    private Optional<String> authorizationCodeOptional;
    private Optional<String> codeVerifierOptional;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);

        try {

            if (authorizationCodeMissing(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, true);
            }

            if (!authorizationCodeValid(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_GRANT, true);
            }

            if (codeVerifierMissing(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, true);
            }

            if (!codeVerifierValid(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.UNAUTHORIZED_CLIENT, true);
            }

            if (!redirectUriValid(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, true);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }

        return next.handle(authorizationCode);
    }

    private boolean authorizationCodeMissing(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        authorizationCodeOptional = bodyDecoder.fetchAttribute(HttpParameters.CODE);
        return authorizationCodeOptional.isEmpty();
    }

    private boolean codeVerifierMissing(HttpPostRequestBodyDecoder  bodyDecoder) throws IOException {
        codeVerifierOptional = bodyDecoder.fetchAttribute(HttpParameters.CODE_VERIFIER);
        return codeVerifierOptional.isEmpty();
    }
    private boolean authorizationCodeValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        String authorizationCodeString = authorizationCodeOptional.get();
        Optional<AuthCode> authCodeOptional = database.fetchAuthorizationCode(authorizationCodeString);

        if (authCodeOptional.isEmpty()) return false;

        authorizationCode = authCodeOptional.get();

        return authorizationCode.isActive() && !authorizationCode.isUsed();
    }

    private boolean codeVerifierValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException, NoSuchAlgorithmException {
        String codeVerifier = codeVerifierOptional.get();

        CodeChallengeMethod codeChallengeMethod = authorizationCode.getCodeChallengeMethod();
        String codeChallenge = authorizationCode.getCodeChallenge();

        if (codeChallengeMethod.equals(CodeChallengeMethod.PLAIN)) {
            return codeChallenge.equals(codeVerifier);
        } else if (codeChallengeMethod.equals(CodeChallengeMethod.S256)) {
            String codeVerifierHashed = hashSHA256InBase64Url(codeVerifier);
            return codeChallenge.equals(codeVerifierHashed);
        }

        return false;
    }

    private boolean redirectUriValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> redirectUriOptional = bodyDecoder.fetchAttribute(HttpParameters.REDIRECT_URI);
        Optional<String> clientIdOptional = bodyDecoder.fetchAttribute(HttpParameters.CLIENT_ID);

        if (redirectUriOptional.isPresent() && clientIdOptional.isPresent()) {
            Optional<Client> clientOptional = database.fetchClient(clientIdOptional.get());

            if (clientOptional.isEmpty()) return false;

            Client client = clientOptional.get();

            return Objects.equals(client.getRedirectUri(), redirectUriOptional.get());
        }

        return false;
    }

    public String hashSHA256InBase64Url(String stringToHash) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
