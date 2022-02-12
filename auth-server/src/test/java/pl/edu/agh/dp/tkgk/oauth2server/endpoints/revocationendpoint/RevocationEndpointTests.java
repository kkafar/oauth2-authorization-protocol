package pl.edu.agh.dp.tkgk.oauth2server.endpoints.revocationendpoint;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.bson.conversions.Bson;
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
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.ExampleTokens;
import pl.edu.agh.dp.tkgk.oauth2server.model.Token;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.TokenHint;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.ServerEndpointsBuilder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RevocationEndpointTests {
    MongoClient mongoClient = MongoClientInstance.get();
    MongoDatabase db = mongoClient.getDatabase("test");

    MongoDBFacade mongoDBFacade = MongoDBFacade.getInstance();

    Queries queries = new Queries();

    Handler<FullHttpRequest, ?> tokenRevocationRequestValidator;

    MongoCollection<Token> accessTokens;
    MongoCollection<Token> refreshTokens;

    ExampleTokens exampleTokens = new ExampleTokens();

    Token activeAccessTokenObj = exampleTokens.getActiveAccessToken();
    Token activeRefreshTokenObj = exampleTokens.getActiveRefreshToken();
    Token expiredAccessTokenObj = exampleTokens.getExpiredAccessToken();
    Token expiredRefreshTokenObj = exampleTokens.getExpiredRefreshToken();
    Token notInDbTokenObj = exampleTokens.getNotInDbToken();

    @BeforeAll
    public void beforeAll() {
        mongoDBFacade.setDatabase(db);

        ServerEndpointsBuilder serverEndpointsBuilder = new ServerEndpointsBuilder();
        tokenRevocationRequestValidator = serverEndpointsBuilder.getEndpointHandlerMap().get("/revoke");

        accessTokens = db.getCollection(MongoDBInfo.Collections.ACCESS_TOKENS_COLLECTION.toString(), Token.class);
        refreshTokens = db.getCollection(MongoDBInfo.Collections.REFRESH_TOKENS_COLLECTION.toString(), Token.class);
    }

    @BeforeEach
    public void beforeEach() {
        accessTokens.drop();
        refreshTokens.drop();
        queries.addObjectsToCollection(List.of(activeAccessTokenObj, expiredAccessTokenObj), accessTokens);
        queries.addObjectsToCollection(List.of(activeRefreshTokenObj, expiredRefreshTokenObj), refreshTokens);
    }

    @AfterAll
    public void afterAll() {
        accessTokens.drop();
        refreshTokens.drop();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getInvalidRequests")
    public void invalidRevocationRequestTest(String requestDescription, FullHttpRequest request) {
        // when
        FullHttpResponse response = tokenRevocationRequestValidator.handle(request);
        Map<String, Object> responseBody = new JSONObject(response.content().toString(StandardCharsets.UTF_8)).toMap();

        // then
        assertEquals(HttpResponseStatus.BAD_REQUEST, response.status());
        assertEquals(2, response.headers().size());
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON, false));
        assertTrue(response.headers().contains(HttpHeaderNames.CONTENT_LENGTH));
        assertEquals(HttpResponseStatus.BAD_REQUEST, response.status());
        assertEquals(HttpRequestError.INVALID_REQUEST, responseBody.get("error"));
        assertEquals(1, responseBody.size());
    }

    private Stream<Arguments> getInvalidRequests() {
        String noTokenParams = getParametersForTokenRevocation("none", "access_token");
        String validParams = getParametersForTokenRevocation(activeAccessTokenObj.getToken(), "refresh_token");

        FullHttpRequest getRequest = getRequestToRevocationEndpoint(validParams,
                HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpMethod.GET);

        FullHttpRequest invalidContentTypeRequest = getRequestToRevocationEndpoint(validParams,
                HttpHeaderValues.APPLICATION_JSON, HttpMethod.POST);

        return Stream.of(
                Arguments.of("no token in request", getUrlEncodedPostRequestToRevocationEndpoint(noTokenParams)),
                Arguments.of("GET method instead of POST", getRequest),
                Arguments.of("content type != url encoded", invalidContentTypeRequest),
                Arguments.of("duplicated parameter",
                        getUrlEncodedPostRequestToRevocationEndpoint(validParams + "&token_type_hint=access_token"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getCustomTokenAndTokenHintType")
    public void revocationWithCustomTokenAndTokenHintTypeTest(String description, Token token, String tokenTypeHint,
                                                              TokenHint validTokenTypeHint, String tokenId,
                                                              boolean revocationValid, boolean tokenInDb, boolean addTokenTypeHint)
    {
        // given
        String params = getParametersForTokenRevocation(token.getToken(), tokenTypeHint);
        if (!addTokenTypeHint) params = "token=" + token.getToken();
        FullHttpRequest request = getUrlEncodedPostRequestToRevocationEndpoint(params);

        // when
        FullHttpResponse response = tokenRevocationRequestValidator.handle(request);

        // then
        assertEquals(HttpResponseStatus.OK, response.status());
        assertEquals(0, response.headers().size());

        MongoCollection<?> collection, otherCollection;

        if (validTokenTypeHint == TokenHint.ACCESS_TOKEN) {
            collection = accessTokens;
            otherCollection = refreshTokens;
        }
        else {
            otherCollection = accessTokens;
            collection = refreshTokens;
        }

        if (revocationValid) {
            assertNull(queries.getObjectFromCollection(collection, "_id", tokenId));
            assertNull(queries.getObjectFromCollection(otherCollection, Token.JsonFields.AUTH_CODE, token.getAuthCode()));
        } else {
            if (tokenInDb) {
                assertNotNull(queries.getObjectFromCollection(collection, "_id", tokenId));
            }
            assertNotNull(queries.getObjectFromCollection(otherCollection, Token.JsonFields.AUTH_CODE, token.getAuthCode()));
        }
    }

    private Stream<Arguments> getCustomTokenAndTokenHintType() {
        return Stream.of(
                Arguments.of("valid access token and token_type_hint", activeAccessTokenObj, TokenHint.ACCESS_TOKEN.toString(),
                        TokenHint.ACCESS_TOKEN, activeAccessTokenObj.getJwtId(), true, true, true),
                Arguments.of("valid access token and no token_type_hint", activeAccessTokenObj, TokenHint.ACCESS_TOKEN.toString(),
                        TokenHint.ACCESS_TOKEN, activeAccessTokenObj.getJwtId(), true, true, false),
                Arguments.of("valid access token and wrong token_type_hint", activeAccessTokenObj, TokenHint.REFRESH_TOKEN.toString(),
                        TokenHint.ACCESS_TOKEN, activeAccessTokenObj.getJwtId(), true, true, true),
                Arguments.of("valid access token and invalid token_type_hint", activeAccessTokenObj, "invalid_token_type_hint",
                        TokenHint.ACCESS_TOKEN, activeAccessTokenObj.getJwtId(), true, true, true),
                Arguments.of("valid refresh token and token_type_hint", activeRefreshTokenObj, TokenHint.REFRESH_TOKEN.toString(),
                        TokenHint.REFRESH_TOKEN, activeRefreshTokenObj.getJwtId(), true, true, true),
                Arguments.of("valid refresh token and wrong token_type_hint", activeRefreshTokenObj, TokenHint.ACCESS_TOKEN.toString(),
                        TokenHint.REFRESH_TOKEN, activeRefreshTokenObj.getJwtId(), true, true, true),
                Arguments.of("valid refresh token and invalid token_type_hint", activeRefreshTokenObj, "invalid_token_type_hint",
                        TokenHint.REFRESH_TOKEN, activeRefreshTokenObj.getJwtId(), true, true, true),
                Arguments.of("access token not in database", notInDbTokenObj, TokenHint.ACCESS_TOKEN.toString(),
                        TokenHint.ACCESS_TOKEN, notInDbTokenObj.getJwtId(), false, false, true),
                Arguments.of("expired access token", expiredAccessTokenObj, TokenHint.ACCESS_TOKEN.toString(),
                        TokenHint.ACCESS_TOKEN, expiredAccessTokenObj.getJwtId(), false, true, true)
                );
    }

    private String getParametersForTokenRevocation(String token, String tokenTypeHint)
    {
        String params = "";
        if (!Objects.equals(token, "none")) params += "token=" + token + "&";
        if (!Objects.equals(tokenTypeHint, "none")) params += "token_type_hint=" + tokenTypeHint + "&";
        if (params.endsWith("&")) params = params.substring(0, params.length() - 1);
        return params;
    }

    private FullHttpRequest getUrlEncodedPostRequestToRevocationEndpoint(String params) {
        return getRequestToRevocationEndpoint(params, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED, HttpMethod.POST);
    }

    private FullHttpRequest getRequestToRevocationEndpoint(String params, AsciiString contentType, HttpMethod method) {
        ByteBuf content = Unpooled.copiedBuffer(params, Charset.defaultCharset());

        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, "/revoke", content);

        request.headers().set(HttpHeaderNames.HOST, "127.0.0.1:5000");
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return request;
    }
}
