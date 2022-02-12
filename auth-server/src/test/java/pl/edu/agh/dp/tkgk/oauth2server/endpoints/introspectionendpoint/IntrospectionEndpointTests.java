package pl.edu.agh.dp.tkgk.oauth2server.endpoints.introspectionendpoint;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
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
import pl.edu.agh.dp.tkgk.oauth2server.model.util.DecodedToken;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenUtil;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.ServerEndpointsBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntrospectionEndpointTests {
    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    Queries queries = new Queries();

    Handler<FullHttpRequest, ?> tokenIntrospectionRequestValidator;

    MongoCollection<Token> accessTokens;
    MongoCollection<Token> refreshTokens;
    MongoCollection<Client> clients;

    Client registeredClient = new Client("client1", "redirect_uri1", List.of("introspect"));

    Token activeAccessTokenObj;
    Token activeRefreshTokenObj;
    Token expiredAccessTokenObj;
    Token expiredRefreshTokenObj;
    Token notInDbTokenObj;

    @BeforeAll
    public void beforeAll() {
        mongoDBFacade.setDatabase(db);

        ServerEndpointsBuilder serverEndpointsBuilder = new ServerEndpointsBuilder();
        tokenIntrospectionRequestValidator = serverEndpointsBuilder.getEndpointHandlerMap().get("/introspect");

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

        accessTokens = db.getCollection(MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString(), Token.class);
        refreshTokens = db.getCollection(MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString(), Token.class);
        clients = db.getCollection(MongoDBInfo.Collections.CLIENTS_COLLECTION.toString(), Client.class);

        queries.addObjectToCollection(registeredClient, clients);
        queries.addObjectsToCollection(List.of(activeAccessTokenObj, expiredAccessTokenObj), accessTokens);
        queries.addObjectsToCollection(List.of(activeRefreshTokenObj, expiredRefreshTokenObj), refreshTokens);
    }

    @AfterAll
    public void afterAll() {
        accessTokens.drop();
        refreshTokens.drop();
        clients.drop();
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidRequests")
    public void invalidTokenRequestTest(String requestDescription, FullHttpRequest request) {
        // when
        FullHttpResponse response = tokenIntrospectionRequestValidator.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        assertEquals(HttpResponseStatus.BAD_REQUEST, response.status());
        checkResponseHeaders(response);
        assertEquals(HttpRequestError.INVALID_REQUEST, responseBody.get("error"));
    }

    private Stream<Arguments> getInvalidRequests() {
        String noTokenParams = getParametersForTokenIntrospection("none", "access_token");
        String validParams = getParametersForTokenIntrospection(activeRefreshTokenObj.getToken(), "refresh_token");

        FullHttpRequest getRequest = getRequestToIntrospectionEndpoint(validParams,
                HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpMethod.GET, activeAccessTokenObj.getToken());

        FullHttpRequest invalidContentTypeRequest = getRequestToIntrospectionEndpoint(validParams,
                HttpHeaderValues.APPLICATION_JSON, HttpMethod.GET, activeAccessTokenObj.getToken());

        FullHttpRequest withoutAuthorizationHeaderRequest = getRequestToIntrospectionEndpoint(validParams,
                HttpHeaderValues.APPLICATION_JSON, HttpMethod.GET, "none");

        return Stream.of(
                Arguments.of("no token in request", getUrlEncodedPostRequestToIntrospectionEndpoint(noTokenParams)),
                Arguments.of("GET method instead of POST", getRequest),
                Arguments.of("content type != url encoded", invalidContentTypeRequest),
                Arguments.of("no authorization header", withoutAuthorizationHeaderRequest)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getCustomAuthentication")
    public void customAuthenticationTest(String authDescription, String authToken, boolean authValid, String customAuthorization) {
        // given
        String validParams = getParametersForTokenIntrospection(activeAccessTokenObj.getToken(), "access_token");
        FullHttpRequest request = getRequestToIntrospectionEndpoint(validParams,
                HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpMethod.POST, authToken);
        String wwwAuthenticateString = "Bearer realm=\"auth_server\", error=\"invalid_token\"";

        if (!Objects.equals(customAuthorization, "none")) {
            request.headers().set(HttpHeaderNames.AUTHORIZATION, customAuthorization);
        }

        // when
        FullHttpResponse response = tokenIntrospectionRequestValidator.handle(request);

        // then
        if (authValid) {
            checkResponseHeaders(response);
            Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();
            assertEquals(HttpResponseStatus.OK, response.status());
            checkResponseActiveTokenInfo(activeAccessTokenObj, responseBody);
        } else {
            assertEquals(HttpResponseStatus.UNAUTHORIZED, response.status());
            assertEquals(wwwAuthenticateString, response.headers().get(HttpHeaderNames.WWW_AUTHENTICATE));
        }
    }

    private Stream<Arguments> getCustomAuthentication() {
        return Stream.of(
                Arguments.of("valid authentication with active auth token", activeAccessTokenObj.getToken(), true, "none"),
                Arguments.of("authentication with expired auth token", expiredAccessTokenObj.getToken(), false, "none"),
                Arguments.of("authentication with auth token not in db", notInDbTokenObj.getToken(), false, "none"),
                Arguments.of("authentication with invalid auth token", "x", false, "none"),
                Arguments.of("authentication with invalid Authorization header", "x", false, "x")
        );
    }

    @ParameterizedTest(name = "token is {0} | token_type_hint = {1}")
    @MethodSource("getCustomTokenAndTokenTypeHint")
    public void customTokenAndTokenTypeHintTest(String tokenDescription, String tokenTypeHint, String token, boolean activeResponse) {
        // given
        String params = getParametersForTokenIntrospection(token, tokenTypeHint);
        FullHttpRequest request = getUrlEncodedPostRequestToIntrospectionEndpoint(params);

        // when
        FullHttpResponse response = tokenIntrospectionRequestValidator.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        checkResponseHeaders(response);
        assertEquals(HttpResponseStatus.OK, response.status());
        if (activeResponse) {
            checkResponseActiveTokenInfo(activeAccessTokenObj, responseBody);
        } else {
            assertEquals(responseBody.get("active"), false);
        }
    }

    private Stream<Arguments> getCustomTokenAndTokenTypeHint() {
        return Stream.of(
                Arguments.of("valid token, no token_type_hint", "none", activeAccessTokenObj.getToken(), true),
                Arguments.of("valid token, correct token_type_hint", "access_token", activeAccessTokenObj.getToken(), true),
                Arguments.of("valid token, wrong token_type_hint", "refresh_token", activeAccessTokenObj.getToken(), true),
                Arguments.of("valid token, invalid token_type_hint", "gdsfgsdn", activeAccessTokenObj.getToken(), true),
                Arguments.of("expired token", "none", expiredAccessTokenObj.getToken(), false),
                Arguments.of("invalid token", "none", "x", false),
                Arguments.of("token not in database", "none", notInDbTokenObj.getToken(), false)
        );
    }

    private String getParametersForTokenIntrospection(String token, String tokenTypeHint)
    {
        String params = "";
        if (!Objects.equals(token, "none")) params += "token=" + token + "&";
        if (!Objects.equals(tokenTypeHint, "none")) params += "token_type_hint=" + tokenTypeHint + "&";
        if (params.endsWith("&")) params = params.substring(0, params.length() - 1);
        return params;
    }

    private void checkResponseActiveTokenInfo(Token tokenToCheck, Map<String, Object> responseBody) {
        DecodedToken decodedToken = tokenToCheck.getDecodedToken();
        assertEquals(responseBody.get("active"), true);
        assertEquals(responseBody.get("scope"), decodedToken.getScopeItems());
        assertEquals(responseBody.get("exp"), (int) decodedToken.getExpiresAt());
        assertEquals(responseBody.get("iat"), (int) decodedToken.getIssuedAt());
        assertEquals(responseBody.get("client_id"), decodedToken.getClientId());
        assertEquals(5, responseBody.size());
    }

    private void checkResponseHeaders(FullHttpResponse response) {
        assertEquals(2, response.headers().size());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON, false));
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_LENGTH));
    }

    private FullHttpRequest getUrlEncodedPostRequestToIntrospectionEndpoint(String params) {
        return getRequestToIntrospectionEndpoint(params, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED,
                HttpMethod.POST,  activeAccessTokenObj.getToken());
    }

    private FullHttpRequest getRequestToIntrospectionEndpoint(String params, AsciiString contentType, HttpMethod method, String authToken) {
        ByteBuf content = Unpooled.copiedBuffer(params, Charset.defaultCharset());

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/introspection", content);

        request.headers().set(HttpHeaderNames.HOST, "127.0.0.1:5000");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        if (!Objects.equals(authToken, "none")) {
            request.headers().set(HttpHeaderNames.AUTHORIZATION, "Bearer " + authToken);
        }
        return request;
    }
}
