package pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.dp.tkgk.oauth2server.common.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.database.MongoDBFacade;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoClientInstance;
import pl.edu.agh.dp.tkgk.oauth2server.database.mongodb.MongoDBInfo;
import pl.edu.agh.dp.tkgk.oauth2server.database.queries.Queries;
import pl.edu.agh.dp.tkgk.oauth2server.model.AuthCode;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.CodeChallengeMethod;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.ServerEndpointsBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenEndpointTests {
    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    Queries queries = new Queries();

    Handler<FullHttpRequest, ?> tokenRequestHandler;

    MongoCollection<Token> accessTokens;
    MongoCollection<Token> refreshTokens;
    MongoCollection<Client> clients;
    MongoCollection<AuthCode> authCodes;


    Client client = new Client("client1", "client1_redirect_uri", List.of("image", "birth_date", "name"));

    AuthCode authCode = new AuthCode("code1", "abc", CodeChallengeMethod.PLAIN,
            Instant.now().plusSeconds(1000).getEpochSecond(), client.getClientId(), "user", false, client.getScope());

    AuthCode notActiveAuthCode = new AuthCode("code2", "abc", CodeChallengeMethod.PLAIN,
            Instant.now().minusSeconds(1000).getEpochSecond(), client.getClientId(), "user", false, client.getScope());

    AuthCode usedAuthCode = new AuthCode("code3", "abc", CodeChallengeMethod.PLAIN,
            Instant.now().plusSeconds(1000).getEpochSecond(), client.getClientId(), "user", true, client.getScope());

    private final String abcToSHA256InBase64Url = "ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0";
    AuthCode s256AuthCode = new AuthCode("code4", abcToSHA256InBase64Url, CodeChallengeMethod.S256,
            Instant.now().plusSeconds(1000).getEpochSecond(), client.getClientId(), "user", false, client.getScope());

    Token refreshTokenObj;

    private String getParametersForAuthCodeGrantType(String grantType, String code, String redirectUri,
                                                     String clientId, String codeVerifier)
    {
        String params = "";
        if (!Objects.equals(grantType, "none")) params += "grant_type=" + grantType + "&";
        if (!Objects.equals(code, "none")) params += "code=" + code + "&";
        if (!Objects.equals(redirectUri, "none")) params += "redirect_uri=" + redirectUri + "&";
        if (!Objects.equals(clientId, "none")) params += "client_id=" + clientId + "&";
        if (!Objects.equals(codeVerifier, "none")) params += "code_verifier=" + codeVerifier + "&";

        if (params.endsWith("&")) params = params.substring(0, params.length() - 1);
        return params;
    }

    private String getParametersForRefreshTokenGrantType(String grantType, String refreshToken, String scope)
    {
        String params = "";
        if (!Objects.equals(grantType, "none")) params += "grant_type=" + grantType + "&";
        if (!Objects.equals(refreshToken, "none")) params += "refresh_token=" + refreshToken + "&";
        if (!Objects.equals(scope, "none")) params += "scope=" + scope + "&";

        if (params.endsWith("&")) params = params.substring(0, params.length() - 1);
        return params;
    }

    @BeforeAll
    public void beforeAll() {
        mongoDBFacade.setDatabase(db);

        ServerEndpointsBuilder serverEndpointsBuilder = new ServerEndpointsBuilder();
        tokenRequestHandler = serverEndpointsBuilder.getEndpointHandlerMap().get("/token");

        accessTokens = db.getCollection(MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString(), Token.class);
        refreshTokens = db.getCollection(MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString(), Token.class);
        clients = db.getCollection(MongoDBInfo.Collections.CLIENTS_COLLECTION.toString(), Client.class);
        authCodes = db.getCollection(MongoDBInfo.Collections.AUTH_CODES_COLLECTION.toString(), AuthCode.class);

        String refreshTokenId = TokenUtil.generateTokenId();
        String refreshToken = TokenUtil.generateToken(1000, authCode.getScope(), authCode.getCode(), authCode.getUserLogin(),
                false, "Bearer", refreshTokenId);

        refreshTokenObj = new Token(refreshTokenId, refreshToken, authCode.getCode(), client.getClientId());

        clients.drop();
        queries.addObjectToCollection(client, clients);
    }

    @BeforeEach
    public void beforeEach() {
        accessTokens.drop();
        refreshTokens.drop();
        authCodes.drop();
        queries.addObjectsToCollection(List.of(s256AuthCode, authCode, notActiveAuthCode, usedAuthCode), authCodes);
        queries.addObjectToCollection(refreshTokenObj, refreshTokens);
    }

    @AfterAll
    public void afterAll() {
        accessTokens.drop();
        refreshTokens.drop();
        clients.drop();
        authCodes.drop();
    }

    @Test
    public void getResponseWithNewAccessAndRefreshTokenFromAuthCodeWithSHA256Test() {
        // given
        String params = getParametersForAuthCodeGrantType("authorization_code",
                s256AuthCode.getCode(), client.getRedirectUri(), client.getClientId(), "abc");

        FullHttpRequest request = getUrlEncodedPostRequestToTokenEndpoint(params);

        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();
        AuthCode usedAuthCode = queries.getObjectFromCollection(authCodes, "_id", s256AuthCode.getCode());

        // then
        checkResponseHeaders(response);

        assertTrue(usedAuthCode.isUsed());

        assertEquals(5, responseBody.size());
        assertTrue(responseBody.containsKey("access_token"));
        assertTrue(responseBody.containsKey("refresh_token"));
        assertTrue(responseBody.containsKey("expires_in"));
        assertEquals(authCode.getScopeItems(), responseBody.get("scope"));
        assertEquals("Bearer", responseBody.get("token_type"));
    }

    @ParameterizedTest(name = "given scope={0} | received scope={1}")
    @MethodSource("getCustomScopes")
    public void getResponseWithNewAccessTokenWithCustomScopeFromRefreshTokenTest(String scope, String correctScope, boolean invalidScope) {
        // given
        String params = getParametersForRefreshTokenGrantType("refresh_token", refreshTokenObj.getToken(), scope);

        FullHttpRequest request = getUrlEncodedPostRequestToTokenEndpoint(params);

        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);

        if (invalidScope) {
            assertEquals(HttpRequestError.INVALID_SCOPE, responseBody.get("error"));
        } else {
            assertEquals(4, responseBody.size());
            assertTrue(responseBody.containsKey("access_token"));
            assertTrue(responseBody.containsKey("expires_in"));
            assertEquals(correctScope, responseBody.get("scope"));
            assertEquals("Bearer", responseBody.get("token_type"));
        }
    }

    private Stream<Arguments> getCustomScopes() {
        return Stream.of(
                Arguments.of("name birth_date", "name birth_date", false),
                Arguments.of("name birth_date birth_date", "none", true),
                Arguments.of("name something_else", "none", true),
                Arguments.of("none", authCode.getScopeItems(), false),
                Arguments.of("name birth_date image", "name birth_date image", false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidRequests")
    public void invalidTokenRequestTest(String requestDescription, FullHttpRequest request) {
        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);
        assertEquals(HttpRequestError.INVALID_REQUEST, responseBody.get("error"));
    }

    private Stream<Arguments> getInvalidRequests() {
        String validParameters = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), client.getRedirectUri(), client.getClientId(), authCode.getCodeChallenge());

        String noGrantTypeParams = getParametersForAuthCodeGrantType("none",
                authCode.getCode(), client.getRedirectUri(), client.getClientId(), authCode.getCodeChallenge());

        String noCodeParams = getParametersForAuthCodeGrantType("authorization_code",
                "none", client.getRedirectUri(), client.getClientId(), authCode.getCodeChallenge());

        String noRedirectUriParams = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), "none", client.getClientId(), authCode.getCodeChallenge());

        String wrongRedirectUriParams = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), "wrong_redirect_uri", client.getClientId(), authCode.getCodeChallenge());

        String noClientIdParams = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), client.getRedirectUri(), "none", authCode.getCodeChallenge());

        String noCodeVerifierParams = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), client.getRedirectUri(), client.getClientId(), "none");

        FullHttpRequest getRequest = getRequestToTokenEndpoint(validParameters,
                HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpMethod.GET);

        FullHttpRequest invalidContentTypeRequest = getRequestToTokenEndpoint(validParameters,
                HttpHeaderValues.APPLICATION_JSON, HttpMethod.POST);

        return Stream.of(
                Arguments.of("GET request instead of POST", getRequest),
                Arguments.of("invalid content type", invalidContentTypeRequest),
                Arguments.of("without grant type", getUrlEncodedPostRequestToTokenEndpoint(noGrantTypeParams)),
                Arguments.of("without code", getUrlEncodedPostRequestToTokenEndpoint(noCodeParams)),
                Arguments.of("without client_id", getUrlEncodedPostRequestToTokenEndpoint(noClientIdParams)),
                Arguments.of("without code_verifier", getUrlEncodedPostRequestToTokenEndpoint(noCodeVerifierParams)),
                Arguments.of("without redirect_uri", getUrlEncodedPostRequestToTokenEndpoint(noRedirectUriParams)),
                Arguments.of("with wrong redirect_uri", getUrlEncodedPostRequestToTokenEndpoint(wrongRedirectUriParams)),
                Arguments.of("duplicate parameters", getUrlEncodedPostRequestToTokenEndpoint(validParameters + "&redirect_uri=" + client.getRedirectUri()))
        );
    }

    @Test
    public void unsupportedGrantTypeTest() {
        // given
        String paramsWithUnknownGrantType = getParametersForAuthCodeGrantType("something_wrong",
                authCode.getCode(), client.getRedirectUri(), client.getClientId(), authCode.getCodeChallenge());

        FullHttpRequest request = getUrlEncodedPostRequestToTokenEndpoint(paramsWithUnknownGrantType);

        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);
        assertEquals(HttpRequestError.UNSUPPORTED_GRANT_TYPE, responseBody.get("error"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidAuthorizationCode")
    public void authorizationCodeInvalidTest(String authCodeDescription, AuthCode authCode, String codeVerifier) {
        // given
        String params = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), client.getRedirectUri(), client.getClientId(), codeVerifier);

        FullHttpRequest request = getUrlEncodedPostRequestToTokenEndpoint(params);

        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);
        assertEquals(HttpRequestError.INVALID_GRANT, responseBody.get("error"));
    }

    private Stream<Arguments> getInvalidAuthorizationCode() {
        AuthCode notAvailableInDbAuthCode = new AuthCode("x", "x", CodeChallengeMethod.PLAIN,
                Instant.now().getEpochSecond(), "client1", "user", false, List.of("image"));

        return Stream.of(
                Arguments.of("not available in database", notAvailableInDbAuthCode, "x"),
                Arguments.of("used auth code", usedAuthCode, usedAuthCode.getCodeChallenge()),
                Arguments.of("not active auth code", notActiveAuthCode, notActiveAuthCode.getCodeChallenge())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidCodeVerifiers")
    public void codeVerifierInvalidTest(String codeVerifierDescription, AuthCode authCode, String codeVerifier) {
        // given
        String params = getParametersForAuthCodeGrantType("authorization_code",
                authCode.getCode(), client.getRedirectUri(), client.getClientId(), codeVerifier);

        FullHttpRequest request = getUrlEncodedPostRequestToTokenEndpoint(params);

        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);
        assertEquals(HttpRequestError.UNAUTHORIZED_CLIENT, responseBody.get("error"));
    }

    private Stream<Arguments> getInvalidCodeVerifiers() {
        return Stream.of(
                Arguments.of("code verifier doesn't match code challenge with plain method", authCode, "abcd"),
                Arguments.of("code verifier doesn't match code challenge with SHA256 method", s256AuthCode, "something")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidRefreshTokens")
    public void refreshTokenInvalidTest(String codeVerifierDescription, String refreshToken) {
        // given
        String validParams = getParametersForRefreshTokenGrantType("refresh_token", refreshToken, "image");

        FullHttpRequest request = getUrlEncodedPostRequestToTokenEndpoint(validParams);

        // when
        FullHttpResponse response = tokenRequestHandler.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);
        assertEquals(HttpRequestError.INVALID_GRANT, responseBody.get("error"));
    }

    private Stream<Arguments> getInvalidRefreshTokens() {
        String refreshToken1 = TokenUtil.generateToken(1000, List.of("all"), authCode.getCode(),
                authCode.getUserLogin(), false, "Bearer", "x");
        Token notAvailableInDatabaseRefreshToken = new Token("x", refreshToken1, authCode.getCode(), authCode.getClientId());

        String tokenId2 = TokenUtil.generateTokenId();
        String refreshToken2 = TokenUtil.generateToken(0, List.of("all"), authCode.getCode(),
                authCode.getUserLogin(), false, "Bearer", tokenId2);
        Token notActiveRefreshToken = new Token(tokenId2, refreshToken2, authCode.getCode(), authCode.getClientId());

        String tokenId3 = TokenUtil.generateTokenId();
        String accessToken = TokenUtil.generateToken(1000, List.of("all"), authCode.getCode(),
                authCode.getUserLogin(), true, "Bearer", tokenId3);
        Token accessTokenObj = new Token(tokenId3, accessToken, authCode.getCode(), authCode.getClientId());

        queries.addObjectToCollection(accessTokenObj, accessTokens);
        queries.addObjectToCollection(notActiveRefreshToken, refreshTokens);

        return Stream.of(
                Arguments.of("refresh token not available in db", refreshToken1),
                Arguments.of("refresh token not active", refreshToken2),
                Arguments.of("active token instead of refresh token", accessToken)
        );
    }

    private void checkResponseHeaders(FullHttpResponse response) {
        assertEquals(4, response.headers().size());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON, false));
        assertTrue(response.headers().contains(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.NO_STORE, false));
        assertTrue(response.headers().contains(HttpHeaderNames.PRAGMA, HttpHeaderValues.NO_CACHE, false));
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_LENGTH));
    }

    private FullHttpRequest getUrlEncodedPostRequestToTokenEndpoint(String params) {
        return getRequestToTokenEndpoint(params, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpMethod.POST);
    }

    private FullHttpRequest getRequestToTokenEndpoint(String params, AsciiString contentType, HttpMethod method) {
        ByteBuf content = Unpooled.copiedBuffer(params, Charset.defaultCharset());

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/token", content);

        request.headers().set(HttpHeaderNames.HOST, "127.0.0.1:5000");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return request;
    }
}
